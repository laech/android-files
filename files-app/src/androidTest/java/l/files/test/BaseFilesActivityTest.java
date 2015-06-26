package l.files.test;

import android.content.Intent;

import java.io.IOException;

import l.files.common.testing.BaseActivityTest;
import l.files.features.objects.UiFileActivity;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Visitor;
import l.files.fs.local.LocalResource;
import l.files.ui.browser.FilesActivity;

import static com.google.common.io.Files.createTempDir;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.ui.browser.FilesActivity.EXTRA_DIRECTORY;

public class BaseFilesActivityTest extends BaseActivityTest<FilesActivity>
{

    private Resource dir;

    public BaseFilesActivityTest()
    {
        super(FilesActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        dir = LocalResource.create(createTempDir());
        setActivityIntent(newIntent(dir));
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        dir.traverse(NOFOLLOW, setAllPermissions(), delete());
    }

    private Visitor setAllPermissions()
    {
        return new Visitor()
        {
            @Override
            public Result accept(final Resource resource) throws IOException
            {
                resource.setPermissions(Permission.all());
                return CONTINUE;
            }
        };
    }

    private Visitor delete()
    {
        return new Visitor()
        {
            @Override
            public Result accept(final Resource resource) throws IOException
            {
                resource.delete();
                return CONTINUE;
            }
        };
    }

    protected final UiFileActivity screen()
    {
        return new UiFileActivity(getInstrumentation(), getActivity());
    }

    protected final Resource dir()
    {
        return dir;
    }

    private Intent newIntent(final Resource dir)
    {
        return new Intent().putExtra(EXTRA_DIRECTORY, dir);
    }
}
