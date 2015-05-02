package l.files.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import l.files.fs.local.LocalResource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static l.files.common.testing.Tests.assertExists;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class MagicDetectorTest extends AbstractDetectorTest
{
    @Override
    protected AbstractDetector detector()
    {
        return MagicDetector.INSTANCE;
    }

    public void testDetect_returnsOctetStreamForUnreadable() throws Exception
    {
        final Resource file = dir1().resolve("a.txt").createFile();
        file.writeString(NOFOLLOW, UTF_8, "hello world");
        file.setPermissions(Collections.<Permission>emptySet());
        try
        {
            detector().detect(file);
            fail();
        }
        catch (final IOException e)
        {
            // Pass
        }
    }

    public void testDetect_returnsOctetStreamForSpecialFile() throws Exception
    {
        final File file = new File("/proc/1/maps");
        assertExists(file);
        try
        {
            detector().detect(LocalResource.create(file));
            fail();
        }
        catch (final IOException e)
        {
            // Pass
        }
    }
}
