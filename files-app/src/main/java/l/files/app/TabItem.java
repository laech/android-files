package l.files.app;

import android.os.Parcel;
import android.os.Parcelable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TabItem implements Parcelable {

  public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
    @Override public TabItem createFromParcel(Parcel source) {
      final int id = source.readInt();
      final String dirId = source.readString();
      final String title = source.readString();
      return new TabItem(id, dirId, title);
    }

    @Override public TabItem[] newArray(int size) {
      return new TabItem[size];
    }
  };

  private final int id;
  private final String dirId;
  private String title;

  public TabItem(int id, String dirId, String title) {
    this.id = id;
    this.dirId = checkNotNull(dirId, "dirId");
    this.title = checkNotNull(title, "title");
  }

  public int getId() {
    return id;
  }

  public String getDirectoryId() {
    return dirId;
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
    dst.writeString(dirId);
    dst.writeString(title);
  }
}
