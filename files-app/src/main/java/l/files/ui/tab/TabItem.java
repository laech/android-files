package l.files.ui.tab;

import android.os.Parcel;
import android.os.Parcelable;

import l.files.fs.Resource;

import static java.util.Objects.requireNonNull;

public final class TabItem implements Parcelable {

    public static final Creator<TabItem> CREATOR = new Creator<TabItem>() {
        @Override
        public TabItem createFromParcel(Parcel source) {
            int id = source.readInt();
            Resource resource = source.readParcelable(Resource.class.getClassLoader());
            String title = source.readString();
            return new TabItem(id, resource, title);
        }

        @Override
        public TabItem[] newArray(int size) {
            return new TabItem[size];
        }
    };

    private final int id;
    private final Resource resource;
    private String title;

    public TabItem(int id, Resource resource, String title) {
        this.id = id;
        this.resource = requireNonNull(resource, "resource");
        this.title = requireNonNull(title, "title");
    }

    public int getId() {
        return id;
    }

    public Resource getResource() {
        return resource;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dst, int flags) {
        dst.writeInt(id);
        dst.writeParcelable(resource, 0);
        dst.writeString(title);
    }

}
