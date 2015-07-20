package l.files.ui.preview;

import android.content.Context;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class NoPreviewCache extends PersistenceCache<Boolean> {

  NoPreviewCache(Context context) {
    super(context);
  }

  @Override String cacheFileName() {
    return "non-images";
  }

  @Override Boolean read(DataInput in) throws IOException {
    return in.readBoolean();
  }

  @Override void write(DataOutput out, Boolean value) throws IOException {
    out.writeBoolean(value);
  }

}
