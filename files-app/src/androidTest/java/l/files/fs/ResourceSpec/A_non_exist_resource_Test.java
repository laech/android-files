package l.files.fs.ResourceSpec;

import l.files.fs.NotExist;
import l.files.fs.Observer;
import l.files.fs.Resource;
import l.files.fs.ResourceSpec.Exceptions.Code;
import l.files.fs.local.ResourceBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ResourceSpec.Exceptions.expect;
import static org.mockito.Mockito.mock;

public final class A_non_exist_resource_Test extends ResourceBaseTest
{
    private Resource nonExist;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        nonExist = dir1().resolve("a");
        assertFalse(nonExist.exists(NOFOLLOW));
    }

    private static void expectNotExist(final Code code) throws Exception
    {
        expect(NotExist.class, code);
    }

    public void test_cannot_list_its_children() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.list(NOFOLLOW);
            }
        });
    }

    public void test_cannot_be_traversed() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.traverse(NOFOLLOW, null, null);
            }
        });
    }

    public void test_cannot_be_observed_for_change() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.observe(NOFOLLOW, mock(Observer.class));
            }
        });
    }

    public void test_cannot_read_its_status() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.stat(NOFOLLOW);
            }
        });
    }

    public void test_cannot_check_readable() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.readable();
            }
        });
    }

    public void test_cannot_check_writable() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.writable();
            }
        });
    }

    public void test_cannot_check_executable() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.executable();
            }
        });
    }

    public void test_cannot_read() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.input(NOFOLLOW);
            }
        });
    }

    public void test_cannot_write() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.output(NOFOLLOW);
            }
        });
    }

    public void test_cannot_read_as_link() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.readLink();
            }
        });
    }

    public void test_cannot_move() throws Exception
    {
        expectNotExist(new Code()
        {
            @Override
            public void run() throws Exception
            {
                nonExist.moveTo(dir2());
            }
        });
    }

    public void test_if_parent_exists_can_create_self() throws Exception
    {
        final Resource dir = dir1();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(dir.resolve("a").createFile().exists(NOFOLLOW));
        assertTrue(dir.resolve("b").createDirectory().exists(NOFOLLOW));
        assertTrue(dir.resolve("c").createLink(dir2()).exists(NOFOLLOW));
    }

    public void test_if_parents_missing_can_create_parents_and_self()
            throws Exception
    {
        final Resource dir = dir1();
        assertTrue(dir.exists(NOFOLLOW));
        assertTrue(dir.resolve("a/aa/aaa").createFiles().exists(NOFOLLOW));
        assertTrue(dir.resolve("b/bb/bbb").createDirectories().exists(NOFOLLOW));
    }
}
