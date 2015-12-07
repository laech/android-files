package l.files.ui.browser;

import android.content.res.Resources;

import java.util.Collections;
import java.util.List;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer {
    /**
     * Always return null categories.
     */
    Categorizer NULL = new Categorizer() {
        @Override
        public Object id(final FileItem item) {
            return null;
        }

        @Override
        public String label(
                final FileItem item,
                final Resources res,
                final Object id) {
            return null;
        }

        @Override
        public List<BrowserItem> categorize(
                final Resources res,
                final List<FileItem> items) {
            return Collections.<BrowserItem>unmodifiableList(items);
        }
    };

    Object id(FileItem item);

    String label(FileItem item, Resources res, Object id);

    List<BrowserItem> categorize(Resources res, List<FileItem> items);
}
