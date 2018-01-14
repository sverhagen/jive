package jive;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;

public class MyRobot {
    private Robot robot;

    public MyRobot() throws AWTException {
        robot = new Robot();
    }

    public void insertUsingPaste(String text) throws Exception {
        // note that clipboard use seems a lot simpler than having Robot type the text

        robot.keyPress(KeyEvent.VK_HOME);
        robot.keyRelease(KeyEvent.VK_HOME);

        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = defaultToolkit.getSystemClipboard();
        StringSelection stringSelection = new StringSelection(text);
        clipboard.setContents(stringSelection, stringSelection);
        keyPress(KeyEvent.VK_CONTROL, KeyEvent.VK_V);
    }

    public void keyPress(int modifier, int key) throws Exception {
        robot.keyPress(modifier);
        robot.keyPress(key);
        Thread.sleep(100); // just for good measure
        robot.keyRelease(key);
        robot.keyRelease(modifier);
        Thread.sleep(100); // just for good measure
    }

    public String selectAndGetText() throws Exception {
        keyPress(KeyEvent.VK_CONTROL, KeyEvent.VK_A);
        keyPress(KeyEvent.VK_CONTROL, KeyEvent.VK_C);

        Toolkit defaultToolkit = Toolkit.getDefaultToolkit();
        Clipboard clipboard = defaultToolkit.getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);
        return (String) transferable.getTransferData(DataFlavor.stringFlavor);
    }
}
