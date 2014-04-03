package l.files.fse;

import com.google.common.base.Objects;

import l.files.os.Stat;

public final class PathStat {

  private final String path;
  private final Stat stat;

  public PathStat(String path, Stat stat) {
    this.stat = stat;
    this.path = path;
  }

  public Stat stat() {
    return stat;
  }

  public String path() {
    return path;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("path", path())
        .add("stat", stat())
        .toString();
  }
}
