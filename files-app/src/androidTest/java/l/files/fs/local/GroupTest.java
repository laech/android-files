package l.files.fs.local;

import junit.framework.TestCase;

public final class GroupTest extends TestCase {

    public void testGetgrgid() throws Exception {
        assertEquals("root", Group.getgrgid(0).getName());
        assertEquals(0, Group.getgrgid(0).getGid());
    }

}
