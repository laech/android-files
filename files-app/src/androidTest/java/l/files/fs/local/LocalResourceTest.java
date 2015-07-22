package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.fs.AccessDenied;
import l.files.fs.AlreadyExists;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.Instant;
import l.files.fs.InvalidOperation;
import l.files.fs.LinkOption;
import l.files.fs.NotDirectory;
import l.files.fs.NotExist;
import l.files.fs.NotFile;
import l.files.fs.NotLink;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.UnsupportedOperation;
import l.files.fs.Visitor;

import static android.test.MoreAsserts.assertNotEqual;
import static com.google.common.collect.Sets.powerSet;
import static java.lang.System.nanoTime;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableSet;
import static java.util.EnumSet.allOf;
import static l.files.fs.Instant.EPOCH;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;
import static l.files.fs.local.LocalResource.permissionsFromMode;
import static l.files.fs.local.Stat.lstat;

public final class LocalResourceTest extends ResourceBaseTest {
  public void test_isReadable_true() throws Exception {
    assertTrue(dir1().readable());
  }

  public void test_isReadable_false() throws Exception {
    dir1().removePermissions(Permission.read());
    assertFalse(dir1().readable());
  }

  public void test_isWritable_true() throws Exception {
    assertTrue(dir1().writable());
  }

  public void test_isWritable_false() throws Exception {
    dir1().removePermissions(Permission.write());
    assertFalse(dir1().writable());
  }

  public void test_isExecutable_true() throws Exception {
    assertTrue(dir1().executable());
  }

  public void test_isExecutable_false() throws Exception {
    dir1().removePermissions(Permission.execute());
    assertFalse(dir1().executable());
  }

  public void test_stat_permissions_unmodifiable() throws Exception {
    Set<Permission> perms = dir1().stat(NOFOLLOW).permissions();
    try {
      perms.clear();
      fail();
    } catch (UnsupportedOperationException e) {
      // Pass
    }
  }

  public void test_stat_symbolicLink() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    Resource link = dir1().resolve("link").createLink(file);
    assertFalse(file.stat(NOFOLLOW).isSymbolicLink());
    assertFalse(link.stat(FOLLOW).isSymbolicLink());
    assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
    assertEquals(file.stat(NOFOLLOW), link.stat(FOLLOW));
  }

  public void test_stat_modificationTime() throws Exception {
    Stat stat = dir1().stat(NOFOLLOW);
    long actual = stat.mtime().seconds();
    long expected = Os.stat(dir1().path()).st_atime;
    assertEquals(expected, actual);
  }

  public void test_stat_accessTime() throws Exception {
    Stat actual = dir1().stat(NOFOLLOW);
    StructStat expected = Os.stat(dir1().path());
    assertEquals(expected.st_atime, actual.atime().seconds());
  }

  public void test_stat_size() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    file.writeString(NOFOLLOW, UTF_8, "hello world");
    long expected = Os.stat(file.path()).st_size;
    long actual = file.stat(NOFOLLOW).size();
    assertEquals(expected, actual);
  }

  public void test_stat_isDirectory() throws Exception {
    assertTrue(dir1().stat(NOFOLLOW).isDirectory());
  }

  public void test_stat_isRegularFile() throws Exception {
    Resource dir = dir1().resolve("dir").createFile();
    assertTrue(dir.stat(NOFOLLOW).isRegularFile());
  }

  public void test_getHierarchy_single() throws Exception {
    Resource a = LocalResource.create(new File("/"));
    assertEquals(singletonList(a), a.hierarchy());
  }

  public void test_getHierarchy_multi() throws Exception {
    Resource a = LocalResource.create(new File("/a/b"));
    List<Resource> expected = Arrays.<Resource>asList(
        LocalResource.create(new File("/")),
        LocalResource.create(new File("/a")),
        LocalResource.create(new File("/a/b"))
    );
    assertEquals(expected, a.hierarchy());
  }

  public void test_list_earlyTermination() throws Exception {
    Resource a = dir1().resolve("a").createFile();
    final Resource b = dir1().resolve("b").createFile();
    dir1().resolve("c").createFile();
    dir1().resolve("d").createFile();
    final List<Resource> result = new ArrayList<>();
    dir1().list(NOFOLLOW, new Visitor() {
      @Override
      public Result accept(Resource resource) throws IOException {
        result.add(resource);
        return resource.equals(b) ? TERMINATE : CONTINUE;
      }
    });
    assertEquals(asList(a, b), result);
  }

  public void test_list_AccessDenied() throws Exception {
    Resource dir = dir1().resolve("dir").createDirectory();
    dir1().setPermissions(Collections.<Permission>emptySet());
    expectOnList(AccessDenied.class, dir, NOFOLLOW);
  }

  public void test_list_NotExistsException() throws Exception {
    Resource dir = dir1().resolve("dir");
    expectOnList(NotExist.class, dir, NOFOLLOW);
  }

  public void test_list_NotDirectoryException_file() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    expectOnList(NotDirectory.class, file, NOFOLLOW);
  }

  public void test_list_NotDirectoryException_link() throws Exception {
    Resource dir = dir1().resolve("dir").createDirectory();
    Resource link = dir1().resolve("link").createLink(dir);
    expectOnList(NotDirectory.class, link, NOFOLLOW);
  }

  public void test_list_linkFollowSuccess() throws Exception {
    Resource dir = dir1().resolve("dir").createDirectory();
    Resource a = dir.resolve("a").createFile();
    Resource b = dir.resolve("b").createDirectory();
    Resource c = dir.resolve("c").createLink(a);
    Resource link = dir1().resolve("link").createLink(dir);

    List<Resource> expected = asList(
        a.resolveParent(dir, link),
        b.resolveParent(dir, link),
        c.resolveParent(dir, link)
    );
    List<Resource> actual = link.list(FOLLOW);

    assertEquals(expected, actual);
  }

  public void test_list() throws Exception {
    Resource a = dir1().resolve("a").createFile();
    Resource b = dir1().resolve("b").createDirectory();
    List<?> expected = asList(a, b);
    List<?> actual = dir1().list(NOFOLLOW);
    assertEquals(expected, actual);
  }

  public void test_list_propagatesException() throws Exception {
    dir1().resolve("a").createFile();
    dir1().resolve("b").createDirectory();
    try {
      dir1().list(NOFOLLOW, new Visitor() {
        @Override
        public Result accept(Resource res) throws IOException {
          throw new IOException("TEST");
        }
      });
      fail();
    } catch (IOException e) {
      if (!"TEST".equals(e.getMessage())) {
        throw e;
      }
    }
  }

  private static void expectOnList(
      final Class<? extends Exception> clazz,
      final Resource resource,
      final LinkOption option) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.list(option);
      }
    });
  }

  public void test_output_append_defaultFalse() throws Exception {
    test_output("a", "b", "b", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output(NOFOLLOW);
      }
    });
  }

  public void test_output_append_false() throws Exception {
    test_output("a", "b", "b", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output(NOFOLLOW, false);
      }
    });
  }

  public void test_output_append_true() throws Exception {
    test_output("a", "b", "ab", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output(NOFOLLOW, true);
      }
    });
  }

  private void test_output(
      String initial,
      String write,
      String result,
      OutputProvider provider) throws Exception {

    Resource file = dir1().resolve("file").createFile();
    file.writeString(NOFOLLOW, UTF_8, initial);
    try (OutputStream out = provider.open(file)) {
      out.write(write.getBytes(UTF_8));
    }
    assertEquals(result, file.readString(NOFOLLOW, UTF_8));
  }

  private interface OutputProvider {
    OutputStream open(Resource resource) throws IOException;
  }

  public void test_output_createWithCorrectPermission()
      throws Exception {
    Resource expected = dir1().resolve("expected");
    Resource actual = dir1().resolve("actual");

    assertTrue(new File(expected.uri()).createNewFile());
    actual.output(NOFOLLOW, false).close();

    assertEquals(
        Os.stat(expected.path()).st_mode,
        Os.stat(actual.path()).st_mode
    );
  }

  public void test_output_AccessDenied() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    dir1().setPermissions(Collections.<Permission>emptySet());
    expectOnOutput(AccessDenied.class, file, NOFOLLOW, false);
  }

  public void test_output_NotExistException() throws Exception {
    Resource file = dir1().resolve("parent/file");
    expectOnOutput(NotExist.class, file, NOFOLLOW, false);
  }

  public void test_output_NotFileException_directory()
      throws Exception {
    expectOnOutput(NotFile.class, dir1(), NOFOLLOW, false);
  }

  public void test_output_NotFileException_link() throws Exception {
    Resource target = dir1().resolve("target").createFile();
    Resource link = dir1().resolve("link").createLink(target);
    expectOnOutput(NotFile.class, link, NOFOLLOW, false);
  }

  public void test_output_cannotUseAfterClose() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    try (OutputStream out = file.output(NOFOLLOW)) {
      FileDescriptor fd = ((FileOutputStream) out).getFD();

      out.write(1);
      out.close();
      try {
        new FileOutputStream(fd).write(1);
        fail();
      } catch (IOException e) {
        // Pass
      }
    }
  }

  private static void expectOnOutput(
      final Class<? extends Exception> clazz,
      final Resource resource,
      final LinkOption option,
      final boolean append) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.output(option, append).close();
      }
    });
  }

  public void test_input() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    String expected = "hello\nworld\n";
    file.writeString(NOFOLLOW, UTF_8, expected);
    try (InputStream in = file.input(NOFOLLOW)) {

      String actual = new String(toByteArray(in), UTF_8);
      assertEquals(expected, actual);
    }
  }

  private byte[] toByteArray(InputStream in) throws IOException {
    byte[] buffer = new byte[4192];
    int count;
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    while ((count = in.read(buffer)) != -1) {
      out.write(buffer, 0, count);
    }
    return out.toByteArray();
  }

  public void test_input_linkFollowSuccess() throws Exception {
    Resource target = dir1().resolve("target").createFile();
    Resource link = dir1().resolve("link").createLink(target);
    link.input(FOLLOW).close();
  }

  public void test_input_NotFileException_link() throws Exception {
    Resource target = dir1().resolve("target").createFile();
    Resource link = dir1().resolve("link").createLink(target);
    expectOnInput(NotFile.class, link, NOFOLLOW);
  }

  public void test_input_NotFileException_directory()
      throws Exception {
    expectOnInput(NotFile.class, dir1(), NOFOLLOW);
  }

  public void test_input_AccessDenied() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    dir1().setPermissions(Collections.<Permission>emptySet());
    expectOnInput(AccessDenied.class, file, NOFOLLOW);
  }

  public void test_input_NotExistException() throws Exception {
    Resource file = dir1().resolve("a");
    expectOnInput(NotExist.class, file, NOFOLLOW);
  }

  public void test_input_cannotUseAfterClose() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    try (InputStream in = file.input(NOFOLLOW)) {
      FileDescriptor fd = ((FileInputStream) in).getFD();

      in.read();
      in.close();
      try {
        new FileInputStream(fd).read();
        fail();
      } catch (IOException e) {
        // Pass
      }
    }
  }

  private static void expectOnInput(
      final Class<? extends Exception> clazz,
      final Resource resource,
      final LinkOption option) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.input(option).close();
      }
    });
  }

  public void test_exists_true() throws Exception {
    assertTrue(dir1().exists(NOFOLLOW));
  }

  public void test_exists_false() throws Exception {
    assertFalse(dir1().resolve("a").exists(NOFOLLOW));
  }

  public void test_exists_checkLinkNotTarget() throws Exception {
    Resource target = dir1().resolve("target");
    Resource link = dir1().resolve("link").createLink(target);
    assertFalse(target.exists(NOFOLLOW));
    assertFalse(link.exists(FOLLOW));
    assertTrue(link.exists(NOFOLLOW));
  }

  public void test_readString() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    String expected = "a\nb\tc";
    file.writeString(NOFOLLOW, UTF_8, expected);
    assertEquals(expected, file.readString(NOFOLLOW, UTF_8));
  }

  public void test_createFile() throws Exception {
    Resource file = dir1().resolve("a");
    file.createFile();
    assertTrue(file.stat(NOFOLLOW).isRegularFile());
  }

  public void test_createFile_correctPermissions() throws Exception {
    Resource actual = dir1().resolve("a");
    actual.createFile();

    File expected = new File(dir1().path(), "b");
    assertTrue(expected.createNewFile());

    assertEquals(expected.canRead(), actual.readable());
    assertEquals(expected.canWrite(), actual.writable());
    assertEquals(expected.canExecute(), actual.executable());
    assertEquals(
        permissionsFromMode(lstat(expected.getPath()).mode()),
        actual.stat(NOFOLLOW).permissions()
    );
  }

  public void test_createFile_AccessDenied() throws Exception {
    dir1().setPermissions(Collections.<Permission>emptySet());
    expectOnCreateFile(AccessDenied.class, dir1().resolve("a"));
  }

  public void test_createFile_AlreadyExists() throws Exception {
    Resource child = dir1().resolve("a");
    child.createFile();
    expectOnCreateFile(AlreadyExists.class, child);
  }

  public void test_createFile_NotExistException() throws Exception {
    expectOnCreateFile(NotExist.class, dir1().resolve("a/b"));
  }

  public void test_createFile_NotDirectoryException() throws Exception {
    Resource child = createChildWithNonDirectoryParent();
    expectOnCreateFile(NotDirectory.class, child);
  }

  private static void expectOnCreateFile(
      final Class<? extends Exception> clazz,
      final Resource resource) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.createFile();
      }
    });
  }

  public void test_createDirectory() throws Exception {
    Resource dir = dir1().resolve("a");
    dir.createDirectory();
    assertTrue(dir.stat(NOFOLLOW).isDirectory());
  }

  public void test_createDirectory_correctPermissions() throws Exception {
    Resource actual = dir1().resolve("a");
    actual.createDirectory();

    File expected = new File(dir1().path(), "b");
    assertTrue(expected.mkdir());

    assertEquals(expected.canRead(), actual.readable());
    assertEquals(expected.canWrite(), actual.writable());
    assertEquals(expected.canExecute(), actual.executable());
    assertEquals(
        permissionsFromMode(lstat(expected.getPath()).mode()),
        actual.stat(NOFOLLOW).permissions()
    );
  }

  public void test_createDirectory_AccessDenied() throws Exception {
    dir1().setPermissions(Collections.<Permission>emptySet());
    Resource dir = dir1().resolve("a");
    expectOnCreateDirectory(AccessDenied.class, dir);
  }

  public void test_createDirectory_AlreadyExists() throws Exception {
    expectOnCreateDirectory(AlreadyExists.class, dir1());
  }

  public void test_createDirectory_NotFoundException() throws Exception {
    Resource dir = dir1().resolve("a/b");
    expectOnCreateDirectory(NotExist.class, dir);
  }

  public void test_createDirectory_NotDirectoryException() throws Exception {
    Resource child = createChildWithNonDirectoryParent();
    expectOnCreateDirectory(NotDirectory.class, child);
  }

  private static void expectOnCreateDirectory(
      final Class<? extends Exception> clazz,
      final Resource resource) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.createDirectory();
      }
    });
  }

  public void test_createDirectories() throws Exception {
    dir1().resolve("a/b/c").createDirectories();
    assertTrue(dir1().resolve("a/b/c").stat(NOFOLLOW).isDirectory());
    assertTrue(dir1().resolve("a/b").stat(NOFOLLOW).isDirectory());
    assertTrue(dir1().resolve("a/").stat(NOFOLLOW).isDirectory());
  }

  public void test_createDirectories_NotDirectoryException() throws Exception {
    Resource parent = dir1().resolve("a");
    Resource child = parent.resolve("b");
    parent.createFile();
    expectOnCreateDirectories(NotDirectory.class, child);
  }

  private static void expectOnCreateDirectories(
      final Class<? extends Exception> clazz,
      final Resource resource) throws Exception {
    expect(clazz, new Code() {
      @Override public void run() throws Exception {
        resource.createDirectories();
      }
    });
  }

  public void test_createSymbolicLink() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());
    assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
    assertEquals(dir1(), link.readLink());
  }

  public void test_createSymbolicLink_AccessDenied() throws Exception {
    dir1().setPermissions(Collections.<Permission>emptySet());
    Resource link = dir1().resolve("a");
    expectOnCreateSymbolicLink(AccessDenied.class, link, dir1());
  }

  public void test_createSymbolicLink_AlreadyExists() throws Exception {
    Resource link = dir1().resolve("a");
    link.createFile();
    expectOnCreateSymbolicLink(AlreadyExists.class, link, dir1());
  }

  public void test_createSymbolicLink_NotExistException() throws Exception {
    Resource link = dir1().resolve("a/b");
    expectOnCreateSymbolicLink(NotExist.class, link, dir1());
  }

  public void test_createSymbolicLink_NotDirectoryException() throws Exception {
    Resource parent = dir1().resolve("parent");
    Resource link = parent.resolve("link");
    parent.createFile();
    expectOnCreateSymbolicLink(NotDirectory.class, link, dir1());
  }

  private static void expectOnCreateSymbolicLink(
      final Class<? extends Exception> clazz,
      final Resource link,
      final Resource target) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        link.createLink(target);
      }
    });
  }

  public void test_readSymbolicLink_AccessDenied() throws Exception {
    dir1().setPermissions(Collections.<Permission>emptySet());
    Resource link = dir1().resolve("a");
    expectOnReadSymbolicLink(AccessDenied.class, link);
  }

  public void test_readSymbolicLink_NotLinkException() throws Exception {
    Resource notLink = dir1().resolve("notLink");
    notLink.createFile();
    expectOnReadSymbolicLink(NotLink.class, notLink);
  }

  public void test_readSymbolicLink_NotExistException() throws Exception {
    Resource link = dir1().resolve("a");
    expectOnReadSymbolicLink(NotExist.class, link);
  }

  public void test_readSymbolicLink_NotDirectoryException() throws Exception {
    Resource parent = dir1().resolve("parent");
    Resource link = parent.resolve("link");
    parent.createFile();
    expectOnReadSymbolicLink(NotDirectory.class, link);
  }

  private static void expectOnReadSymbolicLink(
      final Class<? extends Exception> clazz,
      final Resource link) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        link.readLink();
      }
    });
  }

  public void test_stat_followLink() throws Exception {
    Resource child = dir1().resolve("a").createLink(dir1());
    Stat expected = dir1().stat(NOFOLLOW);
    Stat actual = child.stat(FOLLOW);
    assertTrue(actual.isDirectory());
    assertFalse(actual.isSymbolicLink());
    assertEquals(expected, actual);
  }

  public void test_stat_noFollowLink() throws Exception {
    Resource child = dir1().resolve("a").createLink(dir1());
    Stat actual = child.stat(NOFOLLOW);
    assertTrue(actual.isSymbolicLink());
    assertFalse(actual.isDirectory());
    assertNotEqual(dir1().stat(NOFOLLOW), actual);
  }

  public void test_stat_AccessDenied() throws Exception {
    dir1().setPermissions(Collections.<Permission>emptySet());
    Resource child = dir1().resolve("a");
    expectOnReadStatus(AccessDenied.class, child);
  }

  public void test_stat_NotExistException() throws Exception {
    Resource child = dir1().resolve("a/b");
    expectOnReadStatus(NotExist.class, child);
  }

  public void test_stat_NotDirectoryException() throws Exception {
    Resource parent = dir1().resolve("a");
    Resource child = parent.resolve("b");
    parent.createFile();
    expectOnReadStatus(NotDirectory.class, child);
  }

  private static void expectOnReadStatus(
      final Class<? extends Exception> clazz,
      final Resource resource) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.stat(NOFOLLOW);
      }
    });
  }

  public void test_moveTo_AlreadyExists() throws Exception {
    Resource src = dir1().resolve("src");
    Resource dst = dir1().resolve("dst");
    src.writeString(NOFOLLOW, UTF_8, "src");
    dst.writeString(NOFOLLOW, UTF_8, "dst");
    expectOnMoveTo(AlreadyExists.class, src, dst);
    assertTrue(src.exists(NOFOLLOW));
    assertTrue(dst.exists(NOFOLLOW));
    assertEquals("src", src.readString(NOFOLLOW, UTF_8));
    assertEquals("dst", dst.readString(NOFOLLOW, UTF_8));
  }

  public void test_moveTo_moveLinkNotTarget() throws Exception {
    Resource target = dir1().resolve("target").createFile();
    Resource src = dir1().resolve("src").createLink(target);
    Resource dst = dir1().resolve("dst");
    src.moveTo(dst);
    assertFalse(src.exists(NOFOLLOW));
    assertTrue(dst.exists(NOFOLLOW));
    assertTrue(target.exists(NOFOLLOW));
    assertEquals(target, dst.readLink());
  }

  public void test_moveTo_fileToNonExistingFile() throws Exception {
    Resource src = dir1().resolve("src");
    Resource dst = dir1().resolve("dst");
    src.writeString(NOFOLLOW, UTF_8, "src");
    src.moveTo(dst);
    assertFalse(src.exists(NOFOLLOW));
    assertTrue(dst.exists(NOFOLLOW));
    assertEquals("src", dst.readString(NOFOLLOW, UTF_8));
  }

  public void test_moveTo_directoryToNonExistingDirectory() throws Exception {
    Resource src = dir1().resolve("src");
    Resource dst = dir1().resolve("dst");
    src.resolve("a").createDirectories();
    src.moveTo(dst);
    assertFalse(src.exists(NOFOLLOW));
    assertTrue(dst.exists(NOFOLLOW));
    assertTrue(dst.resolve("a").exists(NOFOLLOW));
  }

  public void test_moveTo_AccessDenied() throws Exception {
    Resource src = dir1().resolve("src").createFile();
    Resource dst = dir1().resolve("dst").createDirectory();
    dst.setPermissions(Collections.<Permission>emptySet());
    expectOnMoveTo(AccessDenied.class, src, dst.resolve("a"));
  }

  public void test_moveTo_NotExistException() throws Exception {
    Resource src = dir1().resolve("src");
    Resource dst = dir1().resolve("dst");
    expectOnMoveTo(NotExist.class, src, dst);
  }

  public void test_moveTo_InvalidOperation() throws Exception {
    Resource parent = dir1().resolve("parent").createDirectory();
    Resource child = parent.resolve("child");
    expectOnMoveTo(InvalidOperation.class, parent, child);
  }

  public void test_moveTo_UnsupportedOperation() throws Exception {
        /*
         * This test assumes:
         *
         *  - /storage/emulated/0
         *  - /storage/emulated/legacy
         *
         * exist on the devices and are mounted on different file systems.
         *
         * Meaning this test will fail if that's no true, such as when testing
         * on the emulator.
         */
    String srcPath = "/storage/emulated/0/test-" + nanoTime();
    String dstPath = "/storage/emulated/legacy/test2-" + nanoTime();
    Resource src = LocalResource.create(new File(srcPath));
    Resource dst = LocalResource.create(new File(dstPath));
    try {
      src.createFile();
      expectOnMoveTo(UnsupportedOperation.class, src, dst);
    } finally {
      try {
        if (src.exists(NOFOLLOW)) {
          src.delete();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
      try {
        if (dst.exists(NOFOLLOW)) {
          dst.delete();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private static void expectOnMoveTo(
      final Class<? extends Exception> clazz,
      final Resource src,
      final Resource dst) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        src.moveTo(dst);
      }
    });
  }

  public void test_delete_symbolicLink() throws Exception {
    Resource link = dir1().resolve("link");
    link.createLink(dir1());
    assertTrue(link.exists(NOFOLLOW));
    link.delete();
    assertFalse(link.exists(NOFOLLOW));
  }

  public void test_delete_file() throws Exception {
    Resource file = dir1().resolve("file");
    file.createFile();
    assertTrue(file.exists(NOFOLLOW));
    file.delete();
    assertFalse(file.exists(NOFOLLOW));
  }

  public void test_delete_emptyDirectory() throws Exception {
    Resource directory = dir1().resolve("directory");
    directory.createDirectory();
    assertTrue(directory.exists(NOFOLLOW));
    directory.delete();
    assertFalse(directory.exists(NOFOLLOW));
  }

  public void test_delete_AccessDenied() throws Exception {
    Resource file = dir1().resolve("a");
    file.createFile();
    dir1().setPermissions(Collections.<Permission>emptySet());
    expectOnDelete(AccessDenied.class, file);
  }

  public void test_delete_NotExistException() throws Exception {
    expectOnDelete(NotExist.class, dir1().resolve("a"));
  }

  public void test_delete_NotDirectoryException() throws Exception {
    Resource child = createChildWithNonDirectoryParent();
    expectOnDelete(NotDirectory.class, child);
  }

  public void test_delete_NotEmptyException() throws Exception {
    dir1().resolve("a").createDirectory();
    expectOnDelete(DirectoryNotEmpty.class, dir1());
  }

  private static void expectOnDelete(
      final Class<? extends Exception> clazz,
      final Resource resource) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.delete();
      }
    });
  }

  public void test_setModificationTime() throws Exception {
    Instant old = getModificationTime(dir1(), NOFOLLOW);
    Instant expect = Instant.of(old.seconds() + 101, old.nanos() - 1);
    dir1().setModified(NOFOLLOW, expect);
    Instant actual = getModificationTime(dir1(), NOFOLLOW);
    assertEquals(expect, actual);
  }

  public void test_setModificationTime_doesNotAffectAccessTime()
      throws Exception {
    Instant atime = getAccessTime(dir1(), NOFOLLOW);
    Instant mtime = Instant.of(1, 2);
    sleep(3);
    dir1().setModified(NOFOLLOW, mtime);
    assertNotEqual(atime, mtime);
    assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
    assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
  }

  public void test_setModificationTime_linkFollow() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    Resource link = dir1().resolve("link").createLink(file);

    Instant fileTime = Instant.of(123, 456);
    Instant linkTime = getModificationTime(link, NOFOLLOW);
    link.setModified(FOLLOW, fileTime);

    assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
    assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  public void test_setModificationTime_linkNoFollow() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    Resource link = dir1().resolve("link").createLink(file);

    Instant fileTime = getModificationTime(file, NOFOLLOW);
    Instant linkTime = Instant.of(123, 456);

    link.setModified(NOFOLLOW, linkTime);

    assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
    assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  public void test_setModificationTime_NotExistException() throws Exception {
    Resource doesNotExist = dir1().resolve("doesNotExist");
    Class<NotExist> expected = NotExist.class;
    expectOnSetModificationTime(expected, doesNotExist, NOFOLLOW, EPOCH);
  }

  private Instant getModificationTime(
      Resource resource,
      LinkOption option) throws IOException {
    return resource.stat(option).modified();
  }

  private static void expectOnSetModificationTime(
      final Class<? extends Exception> clazz,
      final Resource resource,
      final LinkOption option,
      final Instant instant) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.setModified(option, instant);
      }
    });
  }

  public void test_setAccessTime() throws Exception {
    Instant old = getAccessTime(dir1(), NOFOLLOW);
    Instant expect = Instant.of(old.seconds() + 101, old.nanos() - 1);
    dir1().setAccessed(NOFOLLOW, expect);
    Instant actual = getAccessTime(dir1(), NOFOLLOW);
    assertEquals(expect, actual);
  }

  public void test_setAccessTime_doesNotAffectModificationTime()
      throws Exception {
    Instant mtime = getModificationTime(dir1(), NOFOLLOW);
    Instant atime = Instant.of(1, 2);
    sleep(3);
    dir1().setAccessed(NOFOLLOW, atime);
    assertNotEqual(mtime, atime);
    assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
    assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
  }

  public void test_setAccessTime_linkNoFollow() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());

    Instant targetTime = getAccessTime(dir1(), NOFOLLOW);
    Instant linkTime = Instant.of(123, 456);

    link.setAccessed(NOFOLLOW, linkTime);

    assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
    assertEquals(targetTime, getAccessTime(dir1(), NOFOLLOW));
    assertNotEqual(targetTime, linkTime);
  }

  public void test_setAccessTime_linkFollow() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());

    Instant linkTime = getAccessTime(dir1(), NOFOLLOW);
    Instant fileTime = Instant.of(123, 456);

    link.setAccessed(FOLLOW, fileTime);

    assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
    assertEquals(fileTime, getAccessTime(dir1(), NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  public void test_setAccessTime_NotExistException() throws Exception {
    Resource doesNotExist = dir1().resolve("doesNotExist");
    expectOnSetAccessTime(NotExist.class, doesNotExist, NOFOLLOW, EPOCH);
  }

  private Instant getAccessTime(
      Resource resource,
      LinkOption option) throws IOException {
    return resource.stat(option).accessed();
  }

  private static void expectOnSetAccessTime(
      final Class<? extends Exception> clazz,
      final Resource resource,
      final LinkOption option,
      final Instant instant) throws Exception {
    expect(clazz, new Code() {
      @Override
      public void run() throws Exception {
        resource.setAccessed(option, instant);
      }
    });
  }

  public void test_setPermissions() throws Exception {
    for (Set<Permission> expected : powerSet(allOf(Permission.class))) {
      dir1().setPermissions(expected);
      assertEquals(expected, dir1().stat(NOFOLLOW).permissions());
    }
  }

  public void test_setPermissions_rawBits() throws Exception {
    int expected = Os.stat(dir1().path()).st_mode;
    dir1().setPermissions(dir1().stat(NOFOLLOW).permissions());
    int actual = Os.stat(dir1().path()).st_mode;
    assertEquals(expected, actual);
  }

  public void test_removePermissions() throws Exception {
    Set<Permission> all = unmodifiableSet(allOf(Permission.class));
    for (Set<Permission> permissions : powerSet(all)) {
      dir1().setPermissions(all);
      dir1().removePermissions(permissions);

      Set<Permission> actual = dir1().stat(FOLLOW).permissions();
      Set<Permission> expected = new HashSet<>(all);
      expected.removeAll(permissions);
      assertEquals(expected, actual);
    }
  }

  public void test_removePermissions_changeTargetNotLink() throws Exception {
    Permission perm = OWNER_READ;
    Resource link = dir1().resolve("link").createLink(dir1());
    assertTrue(link.stat(FOLLOW).permissions().contains(perm));
    assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));

    link.removePermissions(singleton(perm));

    assertFalse(link.stat(FOLLOW).permissions().contains(perm));
    assertTrue(link.stat(NOFOLLOW).permissions().contains(perm));
  }

  private static void expect(
      Class<? extends Exception> clazz,
      Code code) throws Exception {
    try {
      code.run();
      fail();
    } catch (Exception e) {
      if (!clazz.isInstance(e)) {
        throw e;
      }
    }
  }

  private interface Code {
    void run() throws Exception;
  }

  private Resource createChildWithNonDirectoryParent() throws IOException {
    Resource parent = dir1().resolve("parent");
    Resource child = parent.resolve("child");
    parent.createFile();
    return child;
  }

}
