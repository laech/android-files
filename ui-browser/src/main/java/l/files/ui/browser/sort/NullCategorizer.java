package l.files.ui.browser.sort;

import android.content.res.Resources;

import java.util.List;

import l.files.ui.base.fs.FileInfo;

import static java.util.Collections.unmodifiableList;

enum NullCategorizer implements Categorizer {

    INSTANCE;

    @Override
    public int id(FileInfo item) {
        return -1;
    }

    @Override
    public String label(Resources res, int id) {
        return "";
    }

    @Override
    public List<Object> categorize(Resources res, List<FileInfo> items) {
        return unmodifiableList(items);
    }
};
