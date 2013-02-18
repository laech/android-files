package com.example.files;

import static com.example.files.FileSort.BY_NAME;

import java.io.File;

import junit.framework.TestCase;

public final class FileSort_BY_NAME_Test extends TestCase {

  public void testComparesIgnoreCase() {
    File x = new File("/x");
    File y = new File("/Y");
    assertEquals(-1, BY_NAME.compare(x, y));
  }

  public void testComparesNamePartOnly() {
    File x = new File("/1/a");
    File y = new File("/0/a");
    assertEquals(0, BY_NAME.compare(x, y));
  }
}
