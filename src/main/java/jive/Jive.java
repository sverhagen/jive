package jive;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.openqa.selenium.support.ui.ExpectedConditions.elementToBeClickable;

public class Jive {
    private static final String MY_JIVE_INSTANCE = "some-name"; // as appearing in URL

    private static final String BASE_URL = format("https://%s.jiveon.com", MY_JIVE_INSTANCE);

    private static final String MY_JIVE_INSTANCE_TITLE = "Some Name"; // as appearing in title

    private WebDriver driver;

    private String pathString = "../jive/target/" + getDateForPath();

    private File path = new File(pathString);

    private String prefixPath = path.getAbsolutePath() + File.separator;

    public static void main(String[] args) throws Exception {
        new Jive().run();
    }

    private void run() throws Exception {
        mkdirs();
        initializeWebDriver();
        browseTo(BASE_URL);
        waitAndClick("Employee Login");
        waitForPageReady(format("%s - Sign In", MY_JIVE_INSTANCE_TITLE));
        fillOktaLoginForm();
        waitForPageReady("News | Jive", 300 /* wait for five minutes for user to manually enter credentials */);

        // content pages to start from, base URL (before query parameters) must end in "/content"
        Set<String> links = retrieveLinks(
                "content?filterID=contentstatus%5Bpublished%5D",
                "content?filterID=draft",
                "community/dev/SPACE1/content",
                "community/dev/SPACE2/content",
                "community/dev/SPACE3/content"
        );

        for (String link : links) {
            browseAndSaveLink(link);
        }

        driver.close();
    }

    private void browseAndSaveLink(String link) throws Exception {
        browseTo(link);
        waitForPageReady();
        Thread.sleep(800);

        MyRobot robot = new MyRobot();
        // since Jive implements Ctrl+S, as a trick we jump into the address bar (Alt+D) from where Ctrl+S is handled by
        // the browser (not by Jive)
        robot.keyPress(KeyEvent.VK_ALT, KeyEvent.VK_D);
        robot.keyPress(KeyEvent.VK_CONTROL, KeyEvent.VK_S);
        Thread.sleep(800);

        robot.insertUsingPaste(prefixPath);
        String filePath = robot.selectAndGetText();

        robot.keyPress(KeyEvent.VK_ALT, KeyEvent.VK_S);
        while (!isCompletelyWritten(filePath)) {
            Thread.sleep(100);
        }
    }

    private void browseTo(String baseUrl) {
        driver.get(baseUrl);
    }

    private void fillOktaLoginForm() {
// enable and tweak to have your user name entered:
//        driver.findElement(By.name("username")).sendKeys("MY_USER_NAME");
//        driver.findElement(By.name("password")).click();

// enable and tweak to have your password entered:
//        driver.findElement(By.name("password")).sendKeys("MY_PASSWORD");
//        driver.findElement(By.id("okta-signin-submit")).click();
    }

    private String getDateForPath() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss_Z");
        return simpleDateFormat.format(new Date());
    }

    private void initializeWebDriver() {
        ChromeDriverManager.getInstance().setup();

        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", prefixPath);

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);

        driver = new ChromeDriver(options);
    }

    private boolean isCompletelyWritten(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            try (RandomAccessFile stream = new RandomAccessFile(file, "rw")) {
                return true;
            } catch (Exception e) {
                System.out.println("not yet written");
            }
        }
        return false;
    }

    private void mkdirs() {
        if (!path.exists()) {
            path.mkdirs();
        }
    }

    private Set<String> retrieveLinks(String... paths) {
        Set<String> links = new HashSet<>();
        for (String path : paths) {
            links.addAll(retrieveLinks(path));
        }
        return links;
    }

    private Set<String> retrieveLinks(String path) {
        String baseUrl = BASE_URL + "/" + path;
        validateBaseUrl(baseUrl);
        boolean hasStart = baseUrl.endsWith("start=");
        boolean hasParameters = baseUrl.contains("?");
        String baseUrlWithStart = hasStart ? baseUrl : (hasParameters ? baseUrl + "&start=" : baseUrl + "?start=");

        int start = 0;
        Set<String> links = new HashSet<>();
        while (true) {
            browseTo(baseUrlWithStart + start);
            waitForPageReady();

            List<WebElement> elements = driver.findElements(By.cssSelector("td.j-td-title div > a"));
            if (elements.isEmpty()) {
                // we've stepped past all the pages with content
                break;
            }
            links.addAll(
                    elements.stream().map(e -> e.getAttribute("href")).collect(toList())
            );

            start += 20;
        }

        return links;
    }

    private void validateBaseUrl(String baseUrl) {
        if (baseUrl.matches(".+[?&]start=.+")) {
            throw new IllegalArgumentException("if \"start\" query parameter is included in the base URL, " +
                    "it must be at the end, like so: \"...&start=\" or \"...?start=\", but was: " + baseUrl);
        }
        if (!baseUrl.matches(".+/content(\\?.+)?")) {
            throw new IllegalArgumentException("base URL (before query parameters) must end in \"/content\", " +
                    "but was: " + baseUrl);
        }
    }

    private void waitAndClick(String partialLinkText) {
        WebElement element = new WebDriverWait(driver, 10)
                .until(elementToBeClickable(By.partialLinkText(partialLinkText)));

        element.click();
    }

    private void waitForPageReady(String title) {
        int timeOutInSeconds = 10;
        waitForPageReady(title, timeOutInSeconds);
    }

    private void waitForPageReady(String title, int timeOutInSeconds) {
        new WebDriverWait(driver, timeOutInSeconds).until(
                d -> {
                    boolean complete = ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete");
                    boolean titleAppeared = title.equals(d.getTitle());
                    return complete && titleAppeared;
                });
    }

    private void waitForPageReady() {
        new WebDriverWait(driver, 10).until(
                d -> ((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"));
    }
}
