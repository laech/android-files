package l.files.features;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Visitor;
import l.files.fs.local.LocalResource;
import l.files.test.BaseFilesActivityTest;

import static android.os.Environment.getExternalStorageDirectory;
import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

public final class AutoRefreshStressTest extends BaseFilesActivityTest
{

    public void _ignored_test_large_directory() throws Exception
    {
        final Resource dir = LocalResource.create(
                new File(getExternalStorageDirectory(), "test-large-dir"))
                .createDirectories();

        int count = childCount(dir);
        while (count < 10000)
        {
            dir.resolve(String.valueOf(Math.random())).createFiles();
            count++;
        }
    }

    private int childCount(final Resource dir) throws IOException
    {
        final int[] count = {0};
        dir.list(NOFOLLOW, new Visitor()
        {
            @Override
            public Result accept(final Resource resource) throws IOException
            {
                count[0]++;
                return CONTINUE;
            }
        });
        return count[0];
    }

    public void test_shows_correct_information_on_large_change_events()
            throws Exception
    {
        dir().resolve("a").createFile();
        screen().assertListViewContainsChildrenOf(dir());

        final long end = currentTimeMillis() + SECONDS.toMillis(10);
        while (currentTimeMillis() < end)
        {
            updatePermissions("a");
            updateFileContent("b");
            updateDirectoryChild("c");
            updateLink("d");
            updateDirectory("e");
            updateAttributes();
        }

        screen().assertListViewContainsChildrenOf(dir());
    }

    private void updateAttributes() throws IOException
    {
        for (final Resource child : dir().list(NOFOLLOW))
        {
            final Random r = new Random();
            child.setAccessed(NOFOLLOW, Instant.of(
                    r.nextInt((int) (currentTimeMillis() / 1000)),
                    r.nextInt(999999)));
            child.setModified(NOFOLLOW, Instant.of(
                    r.nextInt((int) (currentTimeMillis() / 1000)),
                    r.nextInt(999999)));
        }
    }

    private void updateDirectory(final String name) throws IOException
    {
        final Resource dir = dir().resolve(name);
        if (dir.exists(NOFOLLOW))
        {
            dir.delete();
        }
        else
        {
            dir.createDirectory();
        }
    }

    private void updatePermissions(final String name) throws IOException
    {
        final Resource res = dir().resolve(name).createFiles();
        if (res.readable())
        {
            res.setPermissions(Permission.read());
        }
        else
        {
            res.setPermissions(Permission.none());
        }
    }

    private void updateFileContent(final String name) throws IOException
    {
        final Resource file = dir().resolve(name).createFiles();
        try (Writer writer = file.writer(UTF_8))
        {
            writer.write(String.valueOf(new Random().nextLong()));
        }
    }

    private void updateDirectoryChild(final String name) throws IOException
    {
        final Resource dir = dir().resolve(name).createDirectories();
        final Resource child = dir.resolve("child");
        if (child.exists(NOFOLLOW))
        {
            child.delete();
        }
        else
        {
            child.createFile();
        }
    }

    private void updateLink(final String name) throws IOException
    {
        final Resource link = dir().resolve(name);
        if (link.exists(NOFOLLOW))
        {
            link.delete();
        }
        link.createLink(new Random().nextInt() % 2 == 0
                ? link
                : link.parent());
    }
}
