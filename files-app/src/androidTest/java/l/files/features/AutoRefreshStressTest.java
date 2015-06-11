package l.files.features;

import java.io.IOException;
import java.io.Writer;
import java.util.Random;

import l.files.fs.Instant;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.test.BaseFilesActivityTest;

import static java.lang.System.currentTimeMillis;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class AutoRefreshStressTest extends BaseFilesActivityTest
{
    public void test_shows_correct_information_on_large_change_events()
            throws Exception
    {
        directory().resolve("a").createFile();
        screen().assertListViewContainsChildrenOf(directory());

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

        screen().assertListViewContainsChildrenOf(directory());
    }

    private void updateAttributes() throws IOException
    {
        for (final Resource child : directory().list(NOFOLLOW))
        {
            final Random r = new Random();
            child.setAccessTime(NOFOLLOW, Instant.of(
                    r.nextInt((int) (currentTimeMillis() / 1000)),
                    r.nextInt(999999)));
            child.setModificationTime(NOFOLLOW, Instant.of(
                    r.nextInt((int) (currentTimeMillis() / 1000)),
                    r.nextInt(999999)));
        }
    }

    private void updateDirectory(final String name) throws IOException
    {
        final Resource dir = directory().resolve(name);
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
        final Resource res = directory().resolve(name).createFiles();
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
        final Resource file = directory().resolve(name).createFiles();
        try (Writer writer = file.writer(NOFOLLOW, UTF_8))
        {
            writer.write(String.valueOf(new Random().nextLong()));
        }
    }

    private void updateDirectoryChild(final String name) throws IOException
    {
        final Resource dir = directory().resolve(name).createDirectories();
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
        final Resource link = directory().resolve(name);
        if (link.exists(NOFOLLOW))
        {
            link.delete();
        }
        link.createLink(new Random().nextInt() % 2 == 0
                ? link
                : link.parent());
    }
}
