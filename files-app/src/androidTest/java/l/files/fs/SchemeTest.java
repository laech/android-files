package l.files.fs;

import junit.framework.TestCase;

public class SchemeTest extends TestCase {

  public void testToString_inLowerCase() throws Exception {
    assertEquals("file", Scheme.parse("FILE").toString());
    assertEquals("file", Scheme.parse("fiLE").toString());
    assertEquals("file", Scheme.parse("filE").toString());
  }

  public void testEquals_ignoreCase() throws Exception {
    assertEquals(Scheme.parse("file"), Scheme.parse("FILE"));
  }

  public void testValidation_success() throws Exception {
    Scheme.parse("http");
    Scheme.parse("svn+ssh");
    Scheme.parse("svn.ssh");
    Scheme.parse("svn-ssh");
    Scheme.parse("svn123");
  }
}
