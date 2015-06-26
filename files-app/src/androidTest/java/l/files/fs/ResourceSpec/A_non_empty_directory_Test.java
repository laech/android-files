package l.files.fs.ResourceSpec;

import java.util.List;

import l.files.fs.DirectoryNotEmpty;
import l.files.fs.Resource;
import l.files.fs.local.ResourceBaseTest;

import static java.util.Arrays.asList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class A_non_empty_directory_Test extends ResourceBaseTest
{
    private Resource dir;
    private Resource child1;
    private Resource child2;
    private Resource child3;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dir = dir1();
        child1 = dir.resolve("child1").createFile();
        child2 = dir.resolve("child2").createDirectory();
        child3 = dir.resolve("child3").createLink(child1);
    }

    public void test_has_children() throws Exception
    {
        final List<Resource> children = asList(child1, child2, child3);
        assertEquals(children, dir.list(FOLLOW));
        assertEquals(children, dir.list(NOFOLLOW));
    }

    public void test_cannot_be_deleted() throws Exception
    {
        try
        {
            dir.delete();
            fail();
        }
        catch (final DirectoryNotEmpty e)
        {
            // Pass
        }
    }
}
