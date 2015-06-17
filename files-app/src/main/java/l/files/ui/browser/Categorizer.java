package l.files.ui.browser;

import android.content.res.Resources;

import java.util.Collections;
import java.util.List;

import l.files.ui.browser.FileListItem.File;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer
{
    /**
     * Always return null categories.
     */
    Categorizer NULL = new Categorizer()
    {
        @Override
        public Object id(final File file)
        {
            return null;
        }

        @Override
        public String label(
                final File file,
                final Resources res,
                final Object id)
        {
            return null;
        }

        @Override
        public List<FileListItem> categorize(
                final Resources res,
                final List<File> items)
        {
            return Collections.<FileListItem>unmodifiableList(items);
        }
    };

    Object id(File file);

    String label(File file, Resources res, Object id);

    List<FileListItem> categorize(Resources res, List<File> items);
}
