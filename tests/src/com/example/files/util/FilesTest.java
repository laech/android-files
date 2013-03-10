package com.example.files.util;

import static com.example.files.util.Files.getFileExtension;

import java.io.File;

import junit.framework.TestCase;

public final class FilesTest extends TestCase {

  public void testGetFileExtensionReturnsExtensionOfFileWithNoNamePart() {
    assertEquals("txt", getFileExtension(new File(".txt")));
  }

  public void testGetFileExtensionReturnsExtensionWithoutDot() {
    assertEquals("txt", getFileExtension(new File("a.txt")));
  }

  public void testGetFileExtensionReturnsNullIfNoExtension() {
    assertNull(getFileExtension(new File("a")));
  }
}
