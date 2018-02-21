package l.files.ui.browser;

import android.content.res.Resources;

import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;

/**
 * Categorizes by file size (descending order).
 */
final class SizeCategorizer extends BaseCategorizer {

    private static final long ZERO = 0;
    private static final long KB_1 = 1024;
    private static final long MB_1 = KB_1 * 1024;
    private static final long MB_100 = MB_1 * 100;
    private static final long GB_1 = MB_1 * 1024;
    private static final long GB_10 = GB_1 * 10;

    private static final Group[] GROUPS = {
            new Group(GB_10, R.string._10gb_or_more),
            new Group(GB_1, R.string._1gb_to_10gb),
            new Group(MB_100, R.string._100mb_to_1gb),
            new Group(MB_1, R.string._1mb_to_100mb),
            new Group(KB_1, R.string._1kb_to_1mb),
            new Group(ZERO, R.string.less_than_1kb),
    };

    @Override
    public int id(FileInfo file) {
        Stat stat = file.selfStat();
        if (stat == null || stat.isDirectory()) return R.string.__;

        long size = stat.size();
        for (Group group : GROUPS) {
            if (size >= group.minSize) {
                return group.label;
            }
        }
        return -1;
    }

    @Override
    public String label(Resources res, int id) {
        return id == -1 ? "" : res.getString(id);
    }

    private static class Group {
        final long minSize;
        final int label;

        Group(long minSize, int label) {
            this.minSize = minSize;
            this.label = label;
        }
    }
}
