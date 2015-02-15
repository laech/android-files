package l.files.fs.local;

import junit.framework.TestCase;

public final class PasswdTest extends TestCase {

  public void testGetpwuid() throws Exception {
    assertEquals("root", Passwd.getpwuid(0).getName());
    assertEquals(0, Passwd.getpwuid(0).getUid());
  }

}
