package l.files.settings;

import android.content.Context;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.sort;
import static l.files.settings.SortSetting.Transformer;

final class SortByName implements Transformer {

  private static final Comparator<File> BY_NAME = new Comparator<File>() {
    @Override public int compare(File x, File y) {
      return x.getName().compareToIgnoreCase(y.getName());
    }
  };

  @Override public List<Object> transform(Context context, File... fs) {
    File[] files = fs.clone();
    sort(files, BY_NAME);
    return Arrays.<Object>asList(files);
  }
}
