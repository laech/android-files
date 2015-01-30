package l.files.ui;

import android.os.Parcel;
import android.os.Parcelable;

import l.files.fs.Path;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TabItem implements Parcelable {

  public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
    @Override public TabItem createFromParcel(Parcel source) {
      int id = source.readInt();
      Path path = source.readParcelable(null);
      String title = source.readString();
      return new TabItem(id, path, title);
    }

    @Override public TabItem[] newArray(int size) {
      return new TabItem[size];
    }
  };

  private final int id;
  private final Path path;
  private String title;

  public TabItem(int id, Path path, String title) {
    this.id = id;
    this.path = checkNotNull(path);
    this.title = checkNotNull(title);
  }

  public int getId() {
    return id;
  }

  public Path getPath() {
    return path;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dst, int flags) {
    dst.writeInt(id);
    dst.writeParcelable(path, 0);
    dst.writeString(title);
  }
}
