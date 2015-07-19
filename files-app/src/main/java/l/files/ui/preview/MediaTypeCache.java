package l.files.ui.preview;

import android.content.Context;

import com.google.common.net.MediaType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

final class MediaTypeCache extends PersistenceCache<MediaType> {

  @Override String cacheFileName() {
    return "media-types";
  }

  @Override MediaType read(Context context, DataInput in) throws IOException {
    return MediaType.parse(in.readUTF());
  }

  @Override void write(DataOutput out, MediaType value) throws IOException {
    out.writeUTF(value.toString());
  }

}