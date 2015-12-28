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
        public Object id(final FileInfo item) {
            return null;
        }

        @Override
        public String label(
                final FileInfo item,
                final Resources res,
                final Object id) {
            return null;
        }

        @Override
        public List<Object> categorize(
                final Resources res,
                final List<FileInfo> items) {
            return Collections.<Object>unmodifiableList(items);
        }
    };

    Object id(FileInfo item);

    String label(FileInfo item, Resources res, Object id);

    List<Object> categorize(Resources res, List<FileInfo> items);
}
