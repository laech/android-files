package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import l.files.ui.base.fs.FileInfo;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

abstract class BaseCategorizer implements Categorizer {

    @Override
    public List<Object> categorize(Resources res, List<FileInfo> items) {

        if (items.isEmpty()) {
            return emptyList();
        }

        List<Object> result = new ArrayList<>(items.size() + 10);

        int previousId = 0;
        int previousIdStartIndex = 0;
        for (int i = 0; i < items.size(); i++) {
            FileInfo stat = items.get(i);
            int currentId = id(stat);
            if (i == 0) {
                result.add(Header.of(label(res, currentId)));
                previousId = currentId;
            } else if (currentId != previousId) {
                result.addAll(items.subList(previousIdStartIndex, i));
                result.add(Header.of(label(res, currentId)));
                previousIdStartIndex = i;
                previousId = currentId;
            }
        }
        result.addAll(items.subList(previousIdStartIndex, items.size()));
        return unmodifiableList(result);
    }

}
