package l.files.fse;

import com.google.auto.value.AutoValue;

import l.files.io.Path;

@AutoValue
public abstract class WatchEvent {
  WatchEvent() {}

  public abstract Kind kind();
  public abstract Path path();

  public static WatchEvent create(Kind kind, Path path) {
    return new AutoValue_WatchEvent(kind, path);
  }

  public static enum Kind {
    CREATE, DELETE, MODIFY
  }
}
