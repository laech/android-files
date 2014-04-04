package l.files.fse;

import com.google.auto.value.AutoValue;

import l.files.os.Stat;

@AutoValue
public abstract class PathStat {
  PathStat() {}

  public static PathStat create(String path, Stat stat) {
    return new AutoValue_PathStat(path, stat);
  }

  public abstract String path();
  public abstract Stat stat();
}
