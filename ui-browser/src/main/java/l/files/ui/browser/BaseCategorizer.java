package l.files.ui.browser;

import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l.files.ui.base.fs.FileInfo;

import static kotlin.collections.CollectionsKt.fold;
import static kotlin.collections.CollectionsKt.groupBy;

abstract class BaseCategorizer implements Categorizer {

    @Override
    public List<Object> categorize(Resources res, List<FileInfo> items) {
        Map<Integer, List<FileInfo>> groups = groupBy(items, this::id);
        return fold(groups.entrySet(), new ArrayList<>(), (result, entry) -> {
            result.add(new Header(label(res, entry.getKey())));
            result.addAll(entry.getValue());
            return result;
        });
    }

}
