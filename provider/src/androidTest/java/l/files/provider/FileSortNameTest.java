package l.files.provider;

import static java.util.Locale.SIMPLIFIED_CHINESE;

public final class FileSortNameTest extends FileSortTest {

  public void testIgnoresCase() throws Exception {
    testSortMatches(FileSort.Name.get(),
        tmp().createFile("a"),
        tmp().createDir("A"),
        tmp().createFile("b")
    );
  }

  public void testLocaleSensitive() throws Exception {
    testSortMatches(new FileSort.Name(SIMPLIFIED_CHINESE),
        tmp().createFile("爱"), // Starts with 'a'
        tmp().createFile("你好"), // Starts with 'n'
        tmp().createFile("知道") // Starts with 'z'
    );
  }
}
