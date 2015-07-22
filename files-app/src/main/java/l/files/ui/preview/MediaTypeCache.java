package l.files.ui.preview;

import com.google.common.net.MediaType;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.fs.Resource;

final class MediaTypeCache extends PersistenceCache<MediaType> {

  MediaTypeCache(Resource cacheDir) {
    super(cacheDir);
  }

  @Override String cacheFileName() {
    return "media-types";
  }

  @Override MediaType read(DataInput in) throws IOException {
    return MediaType.parse(in.readUTF());
  }

  @Override void write(DataOutput out, MediaType value) throws IOException {
    out.writeUTF(value.toString());
  }

}
