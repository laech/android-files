package l.files.fs.local;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l.files.fs.File;

import static java.lang.Integer.parseInt;

@Deprecated
public final class Files {

    private static final Pattern NAME_WITH_NUMBER_SUFFIX =
            Pattern.compile("(.*?\\s+)(\\d+)");

    private Files() {
    }

    /**
     * Returns a file at {@code dstDir} with the name of {@code source}, if such
     * file exists, append a number at the end of the file name (and before the
     * extension if it's {@link java.io.File#isFile()}) until the returned file
     * represents a nonexistent file.
     */
    public static java.io.File getNonExistentDestinationFile(
            final java.io.File source,
            final java.io.File dstDir) {
        String base;
        final String last;

        if (source.isDirectory()) {
            base = source.getName();
            last = "";
        } else {
            final File file = LocalFile.create(source);
            base = file.name().base();
            last = file.name().dotExt();
        }

        java.io.File dst;
        while ((dst = new java.io.File(dstDir, base + last)).exists()) {
            base = increment(base);
        }

        return dst;
    }

    private static String increment(final String base) {
        final Matcher matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base);
        if (matcher.matches()) {
            return matcher.group(1) + (parseInt(matcher.group(2)) + 1);
        } else if (base.equals("")) {
            return "2";
        } else {
            return base + " 2";
        }
    }

}
