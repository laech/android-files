package l.files.sort;

import android.content.res.Resources;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Arrays.sort;

final class NameSorter implements SortHelper {

    private static final Comparator<File> BY_NAME = new Comparator<File>() {
        @Override
        public int compare(File x, File y) {
            return CASE_INSENSITIVE_ORDER.compare(x.getName(), y.getName());
        }
    };

    @Override
    public List<Object> apply(Resources res, File... files) {
        sort(files, BY_NAME);
        return Arrays.<Object>asList(files);
    }

    @Override
    public String name(Resources res) {
        return res.getString(R.string.name);
    }
}
