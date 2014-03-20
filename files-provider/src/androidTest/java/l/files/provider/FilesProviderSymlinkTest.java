package l.files.provider;

import static l.files.os.Unistd.symlink;

public final class FilesProviderSymlinkTest extends FilesProviderTestBase {

  public void testDifferentDirsPointingToSameInode() throws Exception {
    String path1 = tmp().get().getPath();
    String path2 = helper().get("test").getPath();
    symlink(path1, path2);

    tmp().createFile("a");
    assertTrue(tmp().get("a").exists());
    assertTrue(helper().get("test/a").exists());

    tester()
        .awaitCreateFile("b")
        .verify();

    helper().createFile("test/c");
    assertTrue(tmp().get("c").exists());
    tester()
        .awaitCreateDir("d")
        .verify();
  }
}
