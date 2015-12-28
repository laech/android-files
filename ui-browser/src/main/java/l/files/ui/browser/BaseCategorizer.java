package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import l.files.ui.base.fs.FileInfo;

import static java.util.Collections.unmodifiableList;

abstract class BaseCategorizer implements Categorizer {
    @Override
    public List<Object> categorize(
            final Resources res,
            final List<FileInfo> items) {
        final List<Object> result = new ArrayList<>(items.size() + 10);

        Object preCategory = null;
        for (int i = 0; i < items.size(); i++) {
            final FileInfo stat = items.get(i);
            final Object category = id(stat);
            if (i == 0) {
                if (category != null) {
                    result.add(Header.of(label(stat, res, category)));
                }
            } else {
                if (category != null && !category.equals(preCategory)) {
                    result.add(Header.of(label(stat, res, category)));
                }
            }
            result.add(stat);
            preCategory = category;
        }
        return unmodifiableList(result);
    }
}
