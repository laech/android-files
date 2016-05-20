package l.files.ui.browser;

import android.content.res.Resources;

import java.util.Collections;
import java.util.List;

import l.files.ui.base.fs.FileInfo;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer {
    /**
     * Always return null categories.
     */
    Categorizer NULL = new Categorizer() {
        @Override
        public int id(FileInfo item) {
            return -1;
        }

        @Override
        public String label(FileInfo item, Resources res, int id) {
            return null;
        }

        @Override
        public List<Object> categorize(Resources res, List<FileInfo> items) {
            return Collections.<Object>unmodifiableList(items);
        }
    };

    int id(FileInfo item);

    String label(FileInfo item, Resources res, int id);

    List<Object> categorize(Resources res, List<FileInfo> items);
}
