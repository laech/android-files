package l.files.fs.ResourceSpec;

import org.mockito.InOrder;

import l.files.fs.Resource;
import l.files.fs.Visitor;
import l.files.fs.local.ResourceBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public final class An_empty_directory_Test extends ResourceBaseTest
{
    private Resource emptyDir;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        emptyDir = dir1();
    }

    public void test_has_no_children() throws Exception
    {
        assertTrue(emptyDir.list(NOFOLLOW).isEmpty());
    }

    public void test_can_be_deleted() throws Exception
    {
        assertTrue(emptyDir.exists(NOFOLLOW));
        emptyDir.delete();
        assertFalse(emptyDir.exists(NOFOLLOW));
    }

    public void test_traverse_on_itself_only() throws Exception
    {
        final Visitor pre = mock(Visitor.class);
        final Visitor post = mock(Visitor.class);

        emptyDir.traverse(NOFOLLOW, pre, post);

        final InOrder order = inOrder(pre, post);
        order.verify(pre).accept(emptyDir);
        order.verify(post).accept(emptyDir);
        order.verifyNoMoreInteractions();
    }

    public void test_can_be_moved() throws Exception
    {
        final Resource dst = dir2().resolve("dst");
        emptyDir.moveTo(dst);
        assertFalse(emptyDir.exists(NOFOLLOW));
        assertTrue(dst.exists(NOFOLLOW));
    }
}
