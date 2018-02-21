package l.files.ui.browser.sort;

import android.content.res.Resources;

import java.util.List;

import l.files.ui.base.fs.FileInfo;

/**
 * Provides category information for items in cursors.
 */
interface Categorizer {

    int id(FileInfo item);

    String label(Resources res, int id);

    List<Object> categorize(Resources res, List<FileInfo> items);
}
