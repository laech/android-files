package l.files.ui.operations;

import android.os.Parcel;
import android.os.Parcelable;

import javax.annotation.Nullable;

import l.files.base.Objects;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;

final class FailureMessage implements Parcelable {

    private final Path path;
    private final String message;

    private FailureMessage(Path path, String message) {
        this.path = requireNonNull(path);
        this.message = requireNonNull(message);
    }

    Path path() {
        return path;
    }

    String message() {
        return message;
    }

    static FailureMessage create(Path path, String message) {
        return new FailureMessage(path, message);
    }

    @Override
    public String toString() {
        return "FailureMessage{" +
                "path=" + path +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FailureMessage that = (FailureMessage) o;
        return Objects.equal(path, that.path) &&
                Objects.equal(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(path(), 0);
        dest.writeString(message());
    }

    public static final Creator<FailureMessage> CREATOR = new Creator<FailureMessage>() {

        @Override
        public FailureMessage createFromParcel(Parcel source) {
            Path file = source.readParcelable(FailureMessage.class.getClassLoader());
            String message = source.readString();
            return create(file, message);
        }

        @Override
        public FailureMessage[] newArray(int size) {
            return new FailureMessage[size];
        }

    };

}
