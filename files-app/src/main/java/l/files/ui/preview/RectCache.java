package l.files.ui.preview;

import android.content.Context;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.common.graphics.Rect;

final class RectCache extends PersistenceCache<Rect> {

  @Override String cacheFileName() {
    return "sizes";
  }

  @Override Rect read(Context context, DataInput in) throws IOException {
    int width = in.readInt();
    int height = in.readInt();
    return Rect.of(width, height);
  }

  @Override void write(DataOutput out, Rect rect) throws IOException {
    out.writeInt(rect.width());
    out.writeInt(rect.height());
  }

}
