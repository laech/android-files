package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import l.files.ui.base.fs.FileInfo;

import static java.util.Collections.unmodifiableList;

abstract class BaseCategorizer implements Categorizer {

    @Override
    public List<Object> categorize(Resources res, List<FileInfo> items) {

        List<Object> result = new ArrayList<>(items.size() + 10);

        int previousId = 0;
        for (int i = 0; i < items.size(); i++) {
            FileInfo stat = items.get(i);
            int currentId = id(stat);
            if (i == 0) {
                result.add(Header.of(label(stat, res, currentId)));
            } else if (currentId != previousId) {
                result.add(Header.of(label(stat, res, currentId)));
            }
            result.add(stat);
            previousId = currentId;
        }
        return unmodifiableList(result);
    }

}
