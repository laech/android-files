package com.example.files.util;

import static java.io.File.createTempFile;

import java.io.File;

import junit.framework.TestCase;

public final class FileSystemTest extends TestCase {

  private File file;
  private FileSystem fs;

  public void testHasNoPermissionToReadUnexecutableFolder() throws Exception {
    assertTrue(file.mkdir());
    file.setExecutable(false, false);
    assertFalse(fs.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableFile() throws Exception {
    assertTrue(file.createNewFile());
    file.setReadable(false, false);
    assertFalse(fs.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableFolder() throws Exception {
    assertTrue(file.mkdir());
    file.setReadable(false, false);
    assertFalse(fs.hasPermissionToRead(file));

  }

  public void testHasPermissionToReadReadableFile() throws Exception {
    assertTrue(file.createNewFile());
    file.setReadable(true, true);
    assertTrue(fs.hasPermissionToRead(file));
  }

  public void testHasPermissionToReadReadableFolder() {
    assertTrue(file.mkdir());
    file.setReadable(true, true);
    assertTrue(fs.hasPermissionToRead(file));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = new FileSystem();
    file = createTempFile("abc", "def");
    file.delete();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    file.delete();
  }
}
