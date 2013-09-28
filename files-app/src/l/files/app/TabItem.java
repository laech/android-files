package l.files.app;

import android.os.Parcel;
import android.os.Parcelable;

public final class TabItem implements Parcelable {

  @SuppressWarnings("UnusedDeclaration")
  public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
    @Override public TabItem createFromParcel(Parcel source) {
      int id = source.readInt();
      String title = source.readString();
      return new TabItem(id, title);
    }

    @Override public TabItem[] newArray(int size) {
      return new TabItem[size];
    }
  };

  private final int id;
  private String title;

  public TabItem(int id, String title) {
    this.id = id;
    this.title = title;
  }

  public int getId() {
    return id;
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

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(id);
    dest.writeString(title);
  }
}
