package com.example.files.util;

import static java.io.File.createTempFile;

import java.io.File;

import junit.framework.TestCase;

public final class FileSystemTest extends TestCase {

  private File file;
  private FileSystem fs;

  public void testHasNoPermissionToReadUnexecutableDirectory() throws Exception {
    assertTrue(file.mkdir());
    assertTrue(file.setExecutable(false, false));
    assertFalse(fs.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableFile() throws Exception {
    assertTrue(file.createNewFile());
    assertTrue(file.setReadable(false, false));
    assertFalse(fs.hasPermissionToRead(file));
  }

  public void testHasNoPermissionToReadUnreadableDirectory() throws Exception {
    assertTrue(file.mkdir());
    assertTrue(file.setReadable(false, false));
    assertFalse(fs.hasPermissionToRead(file));

  }

  public void testHasPermissionToReadReadableFile() throws Exception {
    assertTrue(file.createNewFile());
    assertTrue(file.setReadable(true, true));
    assertTrue(fs.hasPermissionToRead(file));
  }

  public void testHasPermissionToReadReadableDirectory() {
    assertTrue(file.mkdir());
    assertTrue(file.setReadable(true, true));
    assertTrue(fs.hasPermissionToRead(file));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = new FileSystem();
    file = createTempFile("abc", "def");
    assertTrue(file.delete());
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    assertTrue(file.delete());
  }
}
