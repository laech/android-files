package l.files.app.format;

import android.content.Context;
import com.google.common.base.Function;

import static android.text.format.Formatter.formatShortFileSize;
import static com.google.common.base.Preconditions.checkNotNull;

final class SizeFormat implements Function<Long, String> {

  private final Context context;

  SizeFormat(Context context) {
    this.context = checkNotNull(context, "context");
  }

  @Override public String apply(Long size) {
    return formatShortFileSize(context, size);
  }
}
