package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import l.files.ui.browser.BrowserItem.FileItem;
import l.files.ui.browser.BrowserItem.HeaderItem;

import static java.util.Collections.unmodifiableList;

abstract class BaseCategorizer implements Categorizer {
    @Override
    public List<BrowserItem> categorize(
            final Resources res,
            final List<FileItem> items) {
        final List<BrowserItem> result = new ArrayList<>(items.size() + 10);

        Object preCategory = null;
        for (int i = 0; i < items.size(); i++) {
            final FileItem stat = items.get(i);
            final Object category = id(stat);
            if (i == 0) {
                if (category != null) {
                    result.add(HeaderItem.of(label(stat, res, category)));
                }
            } else {
                if (!Objects.equals(preCategory, category)) {
                    result.add(HeaderItem.of(label(stat, res, category)));
                }
            }
            result.add(stat);
            preCategory = category;
        }
        return unmodifiableList(result);
    }
}
