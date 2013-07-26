package l.files.ui;

import android.content.res.Resources;
import com.google.common.base.Function;
import l.files.R;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

final class FileSummaryFunction implements Function<File, String> {

  private final Resources res;
  private final Function<Long, String> date;
  private final Function<Long, String> size;

  @SuppressWarnings("unchecked") FileSummaryFunction(
      Resources res,
      Function<? super Long, ? extends CharSequence> date,
      Function<? super Long, ? extends CharSequence> size) {
    this.res = checkNotNull(res, "res");
    this.date = (Function<Long, String>) checkNotNull(date, "date");
    this.size = (Function<Long, String>) checkNotNull(size, "size");
  }

  @Override public String apply(File file) {

    String modified = date.apply(file.lastModified());

    if (file.isDirectory()) return modified;

    return res.getString(
        R.string.file_size_updated,
        size.apply(file.length()),
        modified
    );
  }

}
