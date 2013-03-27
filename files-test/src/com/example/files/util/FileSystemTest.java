package com.example.files.util;

import junit.framework.TestCase;

import java.io.File;

import static java.io.File.createTempFile;

public final class FileSystemTest extends TestCase {

  private File files;
  private FileSystem fileSystem;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fileSystem = new FileSystem();
    files = createTempFile("abc", "def");
    assertTrue(files.delete());
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    assertTrue(files.delete());
  }

  public void testHasNoPermissionToReadUnexecutableDirectory() throws Exception {
    assertTrue(files.mkdir());
    assertTrue(files.setExecutable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(files));
  }

  public void testHasNoPermissionToReadUnreadableFile() throws Exception {
    assertTrue(files.createNewFile());
    assertTrue(files.setReadable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(files));
  }

  public void testHasNoPermissionToReadUnreadableDirectory() throws Exception {
    assertTrue(files.mkdir());
    assertTrue(files.setReadable(false, false));
    assertFalse(fileSystem.hasPermissionToRead(files));

  }

  public void testHasPermissionToReadReadableFile() throws Exception {
    assertTrue(files.createNewFile());
    assertTrue(files.setReadable(true, true));
    assertTrue(fileSystem.hasPermissionToRead(files));
  }

  public void testHasPermissionToReadReadableDirectory() {
    assertTrue(files.mkdir());
    assertTrue(files.setReadable(true, true));
    assertTrue(fileSystem.hasPermissionToRead(files));
  }
}
