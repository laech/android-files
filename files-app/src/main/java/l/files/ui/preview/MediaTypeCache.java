package l.files.ui.preview;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import l.files.fs.Resource;

final class MediaTypeCache extends PersistenceCache<String> {

  MediaTypeCache(Resource cacheDir) {
    super(cacheDir);
  }

  @Override String cacheFileName() {
    return "media-types";
  }

  @Override String read(DataInput in) throws IOException {
    return in.readUTF();
  }

  @Override void write(DataOutput out, String media) throws IOException {
    out.writeUTF(media);
  }

}
