package l.files.shared.util;

import static l.files.shared.util.Files.getFileExtension;

import java.io.File;

import junit.framework.TestCase;

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
