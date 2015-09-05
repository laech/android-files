package l.files.fs.local;

import android.system.Os;
import android.system.StructStat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.Stream;

import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.Thread.sleep;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.OWNER_READ;
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
    long actual = stat.lastModifiedTime().seconds();
    long expected = Os.stat(dir1().path()).st_atime;
    assertEquals(expected, actual);
  }

  public void test_stat_accessTime() throws Exception {
    Stat actual = dir1().stat(NOFOLLOW);
    StructStat expected = Os.stat(dir1().path());
    assertEquals(expected.st_atime, actual.lastAccessedTime().seconds());
  }

  public void test_stat_size() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    file.writeString(UTF_8, "hello world");
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

    try (Stream<Resource> actual = link.list(FOLLOW)) {
      assertEquals(expected, actual.to(new ArrayList<>()));
    }
  }

  public void test_list() throws Exception {
    Resource a = dir1().resolve("a").createFile();
    Resource b = dir1().resolve("b").createDirectory();
    List<?> expected = asList(a, b);
    try (Stream<Resource> actual = dir1().list(NOFOLLOW)) {
      assertEquals(expected, actual.to(new ArrayList<>()));
    }
  }

  public void test_output_append_defaultFalse() throws Exception {
    test_output("a", "b", "b", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output();
      }
    });
  }

  public void test_output_append_false() throws Exception {
    test_output("a", "b", "b", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output(false);
      }
    });
  }

  public void test_output_append_true() throws Exception {
    test_output("a", "b", "ab", new OutputProvider() {
      @Override
      public OutputStream open(Resource res) throws IOException {
        return res.output(true);
      }
    });
  }

  private void test_output(
      String initial,
      String write,
      String result,
      OutputProvider provider) throws Exception {

    Resource file = dir1().resolve("file").createFile();
    file.writeString(UTF_8, initial);
    try (OutputStream out = provider.open(file)) {
      out.write(write.getBytes(UTF_8));
    }
    assertEquals(result, file.readString(UTF_8));
  }

  private interface OutputProvider {
    OutputStream open(Resource resource) throws IOException;
  }

  public void test_output_createWithCorrectPermission()
      throws Exception {
    Resource expected = dir1().resolve("expected");
    Resource actual = dir1().resolve("actual");

    assertTrue(new File(expected.uri()).createNewFile());
    actual.output(false).close();

    assertEquals(
        Os.stat(expected.path()).st_mode,
        Os.stat(actual.path()).st_mode
    );
  }

  public void test_input() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    String expected = "hello\nworld\n";
    file.writeString(UTF_8, expected);
    try (InputStream in = file.input()) {

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
    link.input().close();
  }

  public void test_input_cannotUseAfterClose() throws Exception {
    Resource file = dir1().resolve("a").createFile();
    try (InputStream in = file.input()) {
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
    file.writeString(UTF_8, expected);
    assertEquals(expected, file.readString(UTF_8));
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

  public void test_createDirectories() throws Exception {
    dir1().resolve("a/b/c").createDirectories();
    assertTrue(dir1().resolve("a/b/c").stat(NOFOLLOW).isDirectory());
    assertTrue(dir1().resolve("a/b").stat(NOFOLLOW).isDirectory());
    assertTrue(dir1().resolve("a/").stat(NOFOLLOW).isDirectory());
  }

  public void test_createSymbolicLink() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());
    assertTrue(link.stat(NOFOLLOW).isSymbolicLink());
    assertEquals(dir1(), link.readLink());
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
    src.writeString(UTF_8, "src");
    src.moveTo(dst);
    assertFalse(src.exists(NOFOLLOW));
    assertTrue(dst.exists(NOFOLLOW));
    assertEquals("src", dst.readString(UTF_8));
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

  public void test_setModificationTime() throws Exception {
    Instant old = getModificationTime(dir1(), NOFOLLOW);
    Instant expect = Instant.of(old.seconds() + 101, old.nanos() - 1);
    dir1().setLastModifiedTime(NOFOLLOW, expect);
    Instant actual = getModificationTime(dir1(), NOFOLLOW);
    assertEquals(expect, actual);
  }

  public void test_setModificationTime_doesNotAffectAccessTime()
      throws Exception {
    Instant atime = getAccessTime(dir1(), NOFOLLOW);
    Instant mtime = Instant.of(1, 2);
    sleep(3);
    dir1().setLastModifiedTime(NOFOLLOW, mtime);
    assertNotEqual(atime, mtime);
    assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
    assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
  }

  public void test_setModificationTime_linkFollow() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    Resource link = dir1().resolve("link").createLink(file);

    Instant fileTime = Instant.of(123, 456);
    Instant linkTime = getModificationTime(link, NOFOLLOW);
    link.setLastModifiedTime(FOLLOW, fileTime);

    assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
    assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  public void test_setModificationTime_linkNoFollow() throws Exception {
    Resource file = dir1().resolve("file").createFile();
    Resource link = dir1().resolve("link").createLink(file);

    Instant fileTime = getModificationTime(file, NOFOLLOW);
    Instant linkTime = Instant.of(123, 456);

    link.setLastModifiedTime(NOFOLLOW, linkTime);

    assertEquals(linkTime, getModificationTime(link, NOFOLLOW));
    assertEquals(fileTime, getModificationTime(file, NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  private Instant getModificationTime(
      Resource resource,
      LinkOption option) throws IOException {
    return resource.stat(option).lastModifiedTime();
  }

  public void test_setAccessTime() throws Exception {
    Instant old = getAccessTime(dir1(), NOFOLLOW);
    Instant expect = Instant.of(old.seconds() + 101, old.nanos() - 1);
    dir1().setLastAccessedTime(NOFOLLOW, expect);
    Instant actual = getAccessTime(dir1(), NOFOLLOW);
    assertEquals(expect, actual);
  }

  public void test_setAccessTime_doesNotAffectModificationTime()
      throws Exception {
    Instant mtime = getModificationTime(dir1(), NOFOLLOW);
    Instant atime = Instant.of(1, 2);
    sleep(3);
    dir1().setLastAccessedTime(NOFOLLOW, atime);
    assertNotEqual(mtime, atime);
    assertEquals(atime, getAccessTime(dir1(), NOFOLLOW));
    assertEquals(mtime, getModificationTime(dir1(), NOFOLLOW));
  }

  public void test_setAccessTime_linkNoFollow() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());

    Instant targetTime = getAccessTime(dir1(), NOFOLLOW);
    Instant linkTime = Instant.of(123, 456);

    link.setLastAccessedTime(NOFOLLOW, linkTime);

    assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
    assertEquals(targetTime, getAccessTime(dir1(), NOFOLLOW));
    assertNotEqual(targetTime, linkTime);
  }

  public void test_setAccessTime_linkFollow() throws Exception {
    Resource link = dir1().resolve("link").createLink(dir1());

    Instant linkTime = getAccessTime(dir1(), NOFOLLOW);
    Instant fileTime = Instant.of(123, 456);

    link.setLastAccessedTime(FOLLOW, fileTime);

    assertEquals(linkTime, getAccessTime(link, NOFOLLOW));
    assertEquals(fileTime, getAccessTime(dir1(), NOFOLLOW));
    assertNotEqual(fileTime, linkTime);
  }

  private Instant getAccessTime(
      Resource resource,
      LinkOption option) throws IOException {
    return resource.stat(option).lastAccessedTime();
  }

  public void test_setPermissions() throws Exception {
    List<Set<Permission>> permissions = asList(
        Permission.all(),
        Permission.read(),
        Permission.write(),
        Permission.execute());
    for (Set<Permission> expected : permissions) {
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
    List<Set<Permission>> combinations = asList(
        Permission.all(),
        Permission.read(),
        Permission.write(),
        Permission.execute());
    for (Set<Permission> permissions : combinations) {
      dir1().setPermissions(Permission.all());
      dir1().removePermissions(permissions);

      Set<Permission> actual = dir1().stat(FOLLOW).permissions();
      Set<Permission> expected = new HashSet<>(Permission.all());
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

  private interface Code {
    void run() throws Exception;
  }

}
