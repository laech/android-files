package com.example.files.util;

import junit.framework.TestCase;

import java.io.File;

import static com.example.files.util.Files.getFileExtension;

public final class FilesTest extends TestCase {

  public void testGetFileExtensionReturnsEmptyStringIfNoExtension() {
    assertEquals("", getFileExtension(new File("a")));
  }

  public void testGetFileExtensionReturnsExtensionOfFileWithNoNamePart() {
    assertEquals("txt", getFileExtension(new File(".txt")));
  }

  public void testGetFileExtensionReturnsExtensionWithoutDot() {
    assertEquals("txt", getFileExtension(new File("a.txt")));
  }
}
