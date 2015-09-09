package l.files.operations.ui;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

import l.files.fs.File;

@AutoValue
abstract class FailureMessage implements Parcelable {

    FailureMessage() {
    }

    abstract File resource();

    abstract String message();

    static FailureMessage create(File file, String message) {
        return new AutoValue_FailureMessage(file, message);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(resource(), 0);
        dest.writeString(message());
    }

    public static final Creator<FailureMessage> CREATOR = new Creator<FailureMessage>() {

        @Override
        public FailureMessage createFromParcel(Parcel source) {
            File file = source.readParcelable(FailureMessage.class.getClassLoader());
            String message = source.readString();
            return create(file, message);
        }

        @Override
        public FailureMessage[] newArray(int size) {
            return new FailureMessage[size];
        }

    };

}
