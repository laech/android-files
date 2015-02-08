package l.files.features;

import java.io.File;
import java.util.Arrays;

import l.files.test.BaseFilesActivityTest;

import static org.apache.commons.io.FileUtils.waitFor;

public final class FileOperationTest extends BaseFilesActivityTest {

  public void testCopy() throws Exception {
    File a = dir().createFile("a");
    File b = dir().createFile("b");
    File c = dir().createDir("c");
    File d = dir().createDir("d");

    screen()
        .check(a, true)
        .check(b, true)
        .check(c, true)
        .copy()
        .selectItem(d)
        .paste();

    String msg = Arrays.toString(dir().get().list()) +
        ":" + Arrays.toString(d.list());
    assertTrue(msg, waitFor(new File(dir().get(), "d/a"), 5));
    assertTrue(msg, waitFor(new File(dir().get(), "d/b"), 5));
    assertTrue(msg, waitFor(new File(dir().get(), "d/c"), 5));
  }

  public void testPasteMenuIsDisabledInsideFolderBeingCopied() throws Exception {
    File dir = dir().createDir("dir");

    screen()
        .check(dir, true)
        .copy()
        .assertCanPaste(true)
        .selectItem(dir)
        .assertCanPaste(false)
        .pressBack()
        .assertCanPaste(true);
  }

  public void testPasteMenuIsDisabledIfFilesDontExist() throws Exception {
    File dir = dir().createDir("dir");

    screen()
        .check(dir, true)
        .copy()
        .assertCanPaste(true);

    assertTrue(dir.delete());

    screen().assertCanPaste(false);
  }

  public void testPasteMenuIsEnabledIfSomeFilesDontExistSomeExist() throws Exception {
    File dir = dir().createDir("dir1");
    dir().createDir("dir2");

    screen()
        .check(dir, true)
        .copy()
        .assertCanPaste(true);

    assertTrue(dir.delete());

    screen().assertCanPaste(true);
  }

}
