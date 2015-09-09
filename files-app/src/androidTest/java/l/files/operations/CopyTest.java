package l.files.operations;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import l.files.fs.File;
import l.files.fs.Instant;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class CopyTest extends PasteTest {

  public void test_copy_reports_summary() throws Exception {
    File dstDir = dir1().resolve("dir").createDirectory();
    File srcDir = dir1().resolve("a").createDirectory();
    File srcFile = dir1().resolve("a/file").createFile();

    Copy copy = create(singleton(srcDir), dstDir);
    copy.execute();

    List<File> expected = asList(srcDir, srcFile);
    assertEquals(size(expected), copy.getCopiedByteCount());
    assertEquals(expected.size(), copy.getCopiedItemCount());
  }

  private long size(Iterable<File> resources) throws IOException {
    long size = 0;
    for (File file : resources) {
      size += file.stat(NOFOLLOW).size();
    }
    return size;
  }

  public void test_preserves_timestamps_for_file() throws Exception {
    File src = dir1().resolve("a").createFile();
    File dir = dir1().resolve("dir").createDirectory();
    testCopyPreservesTimestamp(src, dir);
  }

  public void test_preserves_timestamps_for_empty_dir() throws Exception {
    File src = dir1().resolve("dir1").createDirectory();
    File dir = dir1().resolve("dir2").createDirectory();
    testCopyPreservesTimestamp(src, dir);
  }

  public void test_preserves_timestamps_for_full_dir() throws Exception {
    File dir = dir1().resolve("dir2").createDirectory();
    File src = dir1().resolve("dir1").createDirectory();
    src.resolve("a").createFile();
    src.resolve("b").createDirectory();
    src.resolve("c").createLink(src);
    testCopyPreservesTimestamp(src, dir);
  }

  private void testCopyPreservesTimestamp(
      File src,
      File dir) throws IOException, InterruptedException {
    File dst = dir.resolve(src.name());
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

  private Instant mtime(File srcFile) throws IOException {
    return srcFile.stat(NOFOLLOW).lastModifiedTime();
  }

  private Instant atime(File res) throws IOException {
    return res.stat(NOFOLLOW).lastAccessedTime();
  }

  public void test_copies_link() throws Exception {
    File target = dir1().resolve("target").createFile();
    File link = dir1().resolve("link").createLink(target);

    copy(link, dir1().resolve("copied").createDirectory());

    File copied = dir1().resolve("copied/link");
    assertEquals(target, copied.readLink());
  }

  public void test_copies_directory() throws Exception {
    File srcDir = dir1().resolve("a").createDirectory();
    File dstDir = dir1().resolve("dst").createDirectory();
    File srcFile = srcDir.resolve("test.txt");
    File dstFile = dstDir.resolve("a/test.txt");
    try (Writer out = srcFile.writer(UTF_8)) {
      out.write("Testing");
    }

    copy(srcDir, dstDir);
    assertEquals("Testing", srcFile.readString(UTF_8));
    assertEquals("Testing", dstFile.readString(UTF_8));
  }

  public void test_copies_empty_directory() throws Exception {
    File src = dir1().resolve("empty").createDirectory();
    File dir = dir1().resolve("dst").createDirectory();
    copy(src, dir);
    assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
  }

  public void test_copies_empty_file() throws Exception {
    File srcFile = dir1().resolve("empty").createFile();
    File dstDir = dir1().resolve("dst").createDirectory();

    copy(srcFile, dstDir);
    assertTrue(dir1().resolve("dst/empty").exists(NOFOLLOW));
  }

  public void test_copies_file() throws Exception {
    File srcFile = dir1().resolve("test.txt").createFile();
    File dstDir = dir1().resolve("dst").createDirectory();
    File dstFile = dstDir.resolve("test.txt");
    try (Writer writer = srcFile.writer(UTF_8)) {
      writer.write("Testing");
    }

    copy(srcFile, dstDir);
    assertEquals("Testing", srcFile.readString(UTF_8));
    assertEquals("Testing", dstFile.readString(UTF_8));
  }

  private void copy(File src, File dstDir)
      throws IOException, InterruptedException {
    create(singleton(src), dstDir).execute();
  }

  @Override Copy create(Collection<File> sources, File dstDir) {
    return new Copy(sources, dstDir);
  }

}
