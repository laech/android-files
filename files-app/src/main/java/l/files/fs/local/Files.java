package l.files.fs.local;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.lang.Integer.parseInt;

@Deprecated
public final class Files {

    private static final Pattern NAME_WITH_NUMBER_SUFFIX = Pattern.compile("(.*?\\s+)(\\d+)");

    private Files() {
    }

    /**
     * Returns a file at {@code dstDir} with the name of {@code source}, if such
     * file exists, append a number at the end of the file name (and before the
     * extension if it's {@link File#isFile()}) until the returned file
     * represents a nonexistent file.
     */
    public static File getNonExistentDestinationFile(File source, File dstDir) {

        String base;
        String last;

        if (source.isDirectory()) {
            base = source.getName();
            last = "";
        } else {
            String name = source.getName();
            base = getNameWithoutExtension(name);
            last = getFileExtension(name);
            if (!isNullOrEmpty(last)) {
                last = "." + last;
            }
        }

        File dst;
        while ((dst = new File(dstDir, base + last)).exists()) {
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
