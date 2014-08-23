package l.files.features;

import java.io.File;
import java.io.IOException;

import l.files.test.BaseFilesActivityTest;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;

public final class NavigationTest extends BaseFilesActivityTest {

  public void testPressActionBarUpIndicatorWillGoBack() {
    File dir = dir().createDir("a");
    screen()
        .selectItem(dir)
        .assertCurrentDirectory(dir)
        .pressActionBarUpIndicator()
        .assertCurrentDirectory(dir.getParentFile());
  }

  public void testActionBarTitleShowsNameOfDirectory() {
    screen()
        .selectItem(dir().createDir("a"))
        .assertActionBarTitle("a");
  }

  public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackInitially() {
    screen().assertActionBarUpIndicatorIsVisible(false);
  }

  public void testActionBarShowsUpIndicatorWhenThereIsBackStack() {
    screen()
        .selectItem(dir().createDir("a"))
        .assertActionBarUpIndicatorIsVisible(true);
  }

  public void testActionBarHidesUpIndicatorWhenThereIsNoBackStackToGoBackTo() {
    screen()
        .selectItem(dir().createDir("a"))
        .pressBack()
        .assertActionBarUpIndicatorIsVisible(false);
  }

  public void testLongPressBackWillClearBackStack() {
    screen()
        .selectItem(dir().createDir("a"))
        .selectItem(dir().createDir("a/b"))
        .selectItem(dir().createDir("a/b/c"))
        .longPressBack()
        .assertCurrentDirectory(dir().get());
  }

  public void testOpenNewDirectoryWillCloseOpenedDrawer() {
    File dir = dir().createDir("a");
    screen()
        .openDrawer()
        .selectItem(dir)
        .assertDrawerIsOpened(false);
  }

  public void testObservesOnCurrentDirectoryAndShowsNewlyAddedFiles() {
    screen().assertListViewContains(dir().createDir("a"), true);
  }

  public void testObservesOnCurrentDirectoryAndHidesDeletedFiles() {
    File file = dir().createFile("a");
    screen()
        .assertListViewContains(file, true)
        .assertListViewContains(delete(file), false);
  }

  public void testUpdatesViewOnChildDirectoryModified() throws Exception {
    testUpdatesViewOnChildModified(dir().createDir("a"));
  }

  public void testUpdatesViewOnChildFileModified() throws Exception {
    testUpdatesViewOnChildModified(dir().createFile("a"));
  }

  private void testUpdatesViewOnChildModified(File f) throws IOException {
    assertTrue(f.setLastModified(0));
    screen()
        .assertFileModifiedDateView(f)
        .assertFileSizeView(f);

    modify(f);

    screen()
        .assertFileModifiedDateView(f)
        .assertFileSizeView(f);
  }

  private File delete(File file) {
    assertTrue(file.delete());
    return file;
  }

  private File modify(File file) throws IOException {
    long lastModifiedBefore = file.lastModified();
    if (file.isDirectory()) {
      assertTrue(new File(file, String.valueOf(System.nanoTime())).mkdir());
    } else {
      write("test", file, UTF_8);
    }
    long lastModifiedAfter = file.lastModified();
    assertNotEqual(lastModifiedBefore, lastModifiedAfter);
    return file;
  }
}
