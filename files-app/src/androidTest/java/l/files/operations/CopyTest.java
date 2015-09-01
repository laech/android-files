package l.files.operations;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import l.files.fs.Instant;
import l.files.fs.Resource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest {

  public void test_copy_reports_summary() throws Exception {
    Resource dstDir = dir1().resolve("dir").createDirectory();
    Resource srcDir = dir1().resolve("a").createDirectory();
    Resource srcFile = dir1().resolve("a/file").createFile();

    Copy copy = create(singleton(srcDir), dstDir);
    copy.execute();

    List<Resource> expected = asList(srcDir, srcFile);
    assertEquals(size(expected), copy.getCopiedByteCount());
    assertEquals(expected.size(), copy.getCopiedItemCount());
  }

  private long size(Iterable<Resource> resources) throws IOException {
    long size = 0;
    for (Resource resource : resources) {
      size += resource.stat(NOFOLLOW).size();
    }
    return size;
  }

  public void test_preserves_timestamps_for_file() throws Exception {
    Resource src = dir1().resolve("a").createFile();
    Resource dir = dir1().resolve("dir").createDirectory();
    testCopyPreservesTimestamp(src, dir);
  }

  public void test_preserves_timestamps_for_empty_dir() throws Exception {
    Resource src = dir1().resolve("dir1").createDirectory();
    Resource dir = dir1().resolve("dir2").createDirectory();
    testCopyPreservesTimestamp(src, dir);
  }

  public void test_preserves_timestamps_for_full_dir() throws Exception {
    Resource dir = dir1().resolve("dir2").createDirectory();
    Resource src = dir1().resolve("dir1").createDirectory();
    src.resolve("a").createFile();
    src.resolve("b").createDirectory();
    src.resolve("c").createLink(src);
    testCopyPreservesTimestamp(src, dir);
  }

  private void testCopyPreservesTimestamp(
      Resource src,
      Resource dir) throws IOException, InterruptedException {
    Resource dst = dir.resolve(src.name());
    assertFalse(dst.exists(NOFOLLOW));

    Instant atime = Instant.of(123, 456);
    Instant mtime = Instant.of(100001, 101);
    src.setLastAccessedTime(NOFOLLOW, atime);
    src.setLastModifiedTime(NOFOLLOW, mtime);

    copy(src, dir);

    assertTrue(dst.exists(NOFOLLOW));
    assertEquals(atime, atime(src));
    assertEquals(atime, atime(dst));
    assertEquals(mtime, mtime(src));
    assertEquals(mtime, mtime(dst));
  }

  private Instant mtime(Resource srcFile) throws IOException {
    return srcFile.stat(NOFOLLOW).lastModifiedTime();
  }

  private Instant atime(Resource res) throws IOException {
    return res.stat(NOFOLLOW).lastAccessedTime();
  }

  public void test_copies_link() throws Exception {
    Resource target = dir1().resolve("target").createFile();
    Resource link = dir1().resolve("link").createLink(target);

    copy(link, dir1().resolve("copied").createDirectory());

    Resource copied = dir1().resolve("copied/link");
    assertEquals(target, copied.readLink());
  }

  public void test_copies_directory() throws Exception {
    Resource srcDir = dir1().resolve("a").createDirectory();
    Resource dstDir = dir1().resolve("dst").createDirectory();
    Resource srcFile = srcDir.resolve("test.txt");
    Resource dstFile = dstDir.resolve("a/test.txt");
    try (Writer out = srcFile.writer(UTF_8)) {
      out.write("Testing");
    }

    copy(srcDir, dstDir);
    assertEquals("Testing", srcFile.readString(UTF_8));
    assertEquals("Testing", dstFile.readString(UTF_8));
  }

  public void test_copies_empty_directory() throws Exception {
    Resource src = dir1().resolve("empty").createDirectory();
    Resource dir = dir1().resolve("dst").createDirectory();
    copy(src, dir);
    assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
  }

  public void test_copies_empty_file() throws Exception {
    Resource srcFile = dir1().resolve("empty").createFile();
    Resource dstDir = dir1().resolve("dst").createDirectory();

    copy(srcFile, dstDir);
    assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
  }

  public void test_copies_file() throws Exception {
    Resource srcFile = dir1().resolve("test.txt").createFile();
    Resource dstDir = dir1().resolve("dst").createDirectory();
    Resource dstFile = dstDir.resolve("test.txt");
    try (Writer writer = srcFile.writer(UTF_8)) {
      writer.write("Testing");
    }

    copy(srcFile, dstDir);
    assertEquals("Testing", srcFile.readString(UTF_8));
    assertEquals("Testing", dstFile.readString(UTF_8));
  }

  private void copy(Resource src, Resource dstDir)
      throws IOException, InterruptedException {
    create(singleton(src), dstDir).execute();
  }

  @Override Copy create(Collection<Resource> sources, Resource dstDir) {
    return new Copy(sources, dstDir);
  }

}
