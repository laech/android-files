package l.files.operations;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l.files.fs.File;
import l.files.fs.Stat;

import static java.lang.Integer.parseInt;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Files {

    private static final Pattern NAME_WITH_NUMBER_SUFFIX =
            Pattern.compile("(.*?\\s+)(\\d+)");

    private Files() {
    }

    /**
     * Returns a file at {@code dstDir} with the name of {@code source}, if such
     * file exists, append a number at the end of the file name (and before the
     * extension if it's a regular file until the returned file
     * represents a nonexistent file.
     */
    public static File getNonExistentDestinationFile(File source, File dstDir)
            throws IOException {

        String base;
        String last;

        Stat stat = source.stat(FOLLOW);
        if (stat.isDirectory()) {
            base = source.name().toString();
            last = "";
        } else {
            base = source.name().base();
            last = source.name().dotExt();
        }

        File dst;
        while ((dst = dstDir.resolve(base + last)).exists(NOFOLLOW)) {
            base = increment(base);
        }

        return dst;
    }

    private static String increment(String base) {
        Matcher matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base);
        if (matcher.matches()) {
            return matcher.group(1) + (parseInt(matcher.group(2)) + 1);
        } else if (base.equals("")) {
            return "2";
        } else {
            return base + " 2";
        }
    }

}