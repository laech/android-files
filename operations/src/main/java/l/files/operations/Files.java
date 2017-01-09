package l.files.operations;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;

import static java.lang.Long.parseLong;
import static l.files.fs.Files.exists;
import static l.files.fs.Files.stat;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Files {

    // TODO match only "* (d+)" otherwise too annoying, and wrong when file is date yyyy-mm-dd or is version e.g. a-2.0
    private static final Pattern NAME_WITH_NUMBER_SUFFIX =
            Pattern.compile("(.*?\\s*)(\\d+)");

    private Files() {
    }

    /**
     * Returns a file at {@code dstDir} with the name of {@code source}, if such
     * file exists, append a number at the end of the file name (and before the
     * extension if it's a regular file until the returned file
     * represents a nonexistent file.
     */
    public static Path getNonExistentDestinationFile(Path source, Path dstDir)
            throws IOException {

        String base;
        String last;

        Name name = source.toAbsolutePath().name();
        if (name == null) {
            throw new IllegalArgumentException("source=" + source);
        }

        Stat stat = stat(source, FOLLOW);
        if (stat.isDirectory()) {
            base = name.toString();
            last = "";
        } else {
            base = name.base();
            last = name.dotExtension();
        }

        Path dst;
        while (exists((dst = dstDir.concat(base + last)), NOFOLLOW)) {
            base = increment(base);
        }

        return dst;
    }

    private static String increment(String base) {
        Matcher matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base);
        if (matcher.matches()) {

            String end = null;
            try {
                long num = parseLong(matcher.group(2));
                if (num < Long.MAX_VALUE) {
                    end = String.valueOf(num + 1);
                }
            } catch (NumberFormatException ignored) {
                // e.g. Number too big, handled below
            }

            if (end == null) {
                end = matcher.group(2) + " 2";
            }

            return matcher.group(1) + end;

        } else if (base.equals("")) {
            return "2";

        } else {
            return base + " 2";
        }

    }

}
