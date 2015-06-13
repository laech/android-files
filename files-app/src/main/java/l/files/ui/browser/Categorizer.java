package l.files.ui.browser;

import android.content.res.Resources;

import l.files.ui.browser.FileListItem.File;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer
{
    int NULL_CATEGORY = Integer.MIN_VALUE;

    /**
     * Always return null categories.
     */
    Categorizer NULL = new Categorizer()
    {
        @Override
        public int id(final File file)
        {
            return NULL_CATEGORY;
        }

        @Override
        public String label(
                final File file,
                final Resources res,
                final int id)
        {
            return null;
        }
    };

    int id(File file);

    String label(File file, Resources res, int id);

}
