# Jive Downloader

Quick little piece of software to download sections of a [Jive instance](https://www.jivesoftware.com/about-jive/).
There has been a lot of negative reporting about Jive's future, so it doesn't hurt to have a backup. There is probably
many better ways of backing up a Jive instance, but I chose for an approach that is true to the original look and feel
of the Jive instance and that shows the content as well as the comments.

## Approach

The chosen approach is as follows:
- You specify the base URL of your Jive instance
- You specify one or a number of paths, such as:
    - "`content?filterID=contentstatus%5Bpublished%5D`" for your authored, published content
    - "`content?filterID=draft`" for your drafts
    - "`community/dev/SPACE/content`" for every space of interest (substitute `SPACE`)
- Using Selenium WebDriver the software will retrieve the path, and find all the links in its list of content, then open
each consecutive content item
- Using Java AWT `Robot` the software will save the page of each such consecutive content item
- This happens in real time, so sit back and hang out

## Problems

Some problems that the software has to deal with:
- `Robot` requires mouse positioning, don't touch your mouse or change focus
- Also due to mouse positioning, without additional work, things are unlikely to work other than on Windows (where it
was developed on)
- Jive takes control of Ctrl+S (for its search), so to use Ctrl+S to save the page, the software will precede that with
Alt+D (to put focus on the address bar, where Ctrl+S truly means saving)
- Due to the specific key combinations, without additional work, things are unlikely to work other than on Chrome (where
it was developed on)
- While the software deduplicates links that appear in multiple lists of content, while saving it may still encounter
duplicate file names. For the moment, one just has to struggle through this
- Authentication, and in order to not enter the credentials in the software, the software will stop and give you five
minutes to manually enter your credentials. The software assumes Okta for authentication

## Disclaimer

Use at your own risk. Use responsibly.

## License

This project is licensed under [the Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
