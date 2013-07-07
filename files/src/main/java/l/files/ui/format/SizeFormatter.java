package l.files.ui.format;

import android.content.Context;
import com.google.common.base.Function;

import static android.text.format.Formatter.formatShortFileSize;
import static com.google.common.base.Preconditions.checkNotNull;

final class SizeFormatter implements Function<Long, String> {

  private final Context context;

  SizeFormatter(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public String apply(Long size) {
    return formatShortFileSize(context, size);
  }

}
