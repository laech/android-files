package l.files.features;

import java.io.File;

import l.files.test.BaseFilesActivityTest;

public final class SelectionTest extends BaseFilesActivityTest {

  public void testSelectsAll() throws Throwable {
    File a = dir().createFile("a");
    File b = dir().createFile("b");
    File c = dir().createDir("c");

    screen()
        .check(a, true)
        .selectAll()
        .assertChecked(a, true)
        .assertChecked(b, true)
        .assertChecked(c, true);
  }

  public void testFinishesActionModeOnNoSelection() throws Throwable {
    File a = dir().createFile("a");
    screen()
        .check(a, true)
        .assertActionModePresent(true)
        .check(a, false)
        .assertActionModePresent(false);
  }
}
