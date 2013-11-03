package l.files.app;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

public final class TabItem implements Parcelable {

    @SuppressWarnings("UnusedDeclaration")
    public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
        @Override
        public TabItem createFromParcel(Parcel source) {
            final int id = source.readInt();
            final String path = source.readString();
            final String title = source.readString();
            return new TabItem(id, new File(path), title);
        }

        @Override
        public TabItem[] newArray(int size) {
            return new TabItem[size];
        }
    };

    private final int mId;
    private final File mDirectory;
    private String mTitle;

    public TabItem(int id, File directory, String title) {
        mId = id;
        mDirectory = directory;
        mTitle = title;
    }

    public int getId() {
        return mId;
    }

    public File getDirectory() {
        return mDirectory;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(mDirectory.getAbsolutePath());
        dest.writeString(mTitle);
    }
}
