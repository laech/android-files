package l.files.fs.local;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l.files.fs.Resource;

import static java.lang.Integer.parseInt;

@Deprecated
public final class Files
{

    private static final Pattern NAME_WITH_NUMBER_SUFFIX =
            Pattern.compile("(.*?\\s+)(\\d+)");

    private Files()
    {
    }

    /**
     * Returns a file at {@code dstDir} with the name of {@code source}, if such
     * file exists, append a number at the end of the file name (and before the
     * extension if it's {@link File#isFile()}) until the returned file
     * represents a nonexistent file.
     */
    public static File getNonExistentDestinationFile(
            final File source,
            final File dstDir)
    {
        String base;
        final String last;

        if (source.isDirectory())
        {
            base = source.getName();
            last = "";
        }
        else
        {
            final Resource resource = LocalResource.create(source);
            base = resource.name().base();
            last = resource.name().dotExt();
        }

        File dst;
        while ((dst = new File(dstDir, base + last)).exists())
        {
            base = increment(base);
        }

        return dst;
    }

    private static String increment(final String base)
    {
        final Matcher matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base);
        if (matcher.matches())
        {
            return matcher.group(1) + (parseInt(matcher.group(2)) + 1);
        }
        else if (base.equals(""))
        {
            return "2";
        }
        else
        {
            return base + " 2";
        }
    }

}
