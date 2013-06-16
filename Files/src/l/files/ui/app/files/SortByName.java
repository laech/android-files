package l.files.ui.app.files;

import com.google.common.base.Function;
import java.io.File;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.sort;

public final class SortByName implements Function<File[], List<File>> {

  private static final Comparator<File> BY_NAME = new Comparator<File>() {
    @Override public int compare(File x, File y) {
      return x.getName().compareToIgnoreCase(y.getName());
    }
  };

  @Override public List<File> apply(File... fs) {
    File[] files = fs.clone();
    sort(files, BY_NAME);
    return asList(files);
  }
}
