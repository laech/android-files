package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;

public interface Resource {

  String name();

  Resource resolve(String other);

  FileStatus stat() throws IOException;

  boolean exists();

  DirectoryStream newDirectoryStream();

  InputStream newInputStream() throws IOException;

  void createDirectory() throws IOException;

  void move(Path dst) throws IOException;

  MediaType detectMediaType() throws IOException;

  WatchService watcher();

}
