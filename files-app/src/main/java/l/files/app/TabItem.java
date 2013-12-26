package l.files.app;

import android.os.Parcel;
import android.os.Parcelable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class TabItem implements Parcelable {

  public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
    @Override public TabItem createFromParcel(Parcel source) {
      final int id = source.readInt();
      final String directoryLocation = source.readString();
      final String title = source.readString();
      return new TabItem(id, directoryLocation, title);
    }

    @Override public TabItem[] newArray(int size) {
      return new TabItem[size];
    }
  };

  private final int id;
  private final String directoryLocation;
  private String title;

  public TabItem(int id, String directoryLocation, String title) {
    this.id = id;
    this.directoryLocation = checkNotNull(directoryLocation, "directoryLocation");
    this.title = checkNotNull(title, "title");
  }

  public int getId() {
    return id;
  }

  public String getDirectoryLocation() {
    return directoryLocation;
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
    dst.writeString(directoryLocation);
    dst.writeString(title);
  }
}
