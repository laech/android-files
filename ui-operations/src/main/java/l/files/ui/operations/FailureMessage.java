package l.files.ui.operations;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import l.files.fs.Path;

@AutoValue
abstract class FailureMessage implements Parcelable {

    FailureMessage() {
    }

    abstract Path path();

    abstract String message();

    static FailureMessage create(Path path, String message) {
        return new AutoValue_FailureMessage(path, message);
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
