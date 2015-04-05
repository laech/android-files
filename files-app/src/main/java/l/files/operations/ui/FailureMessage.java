package l.files.operations.ui;

import android.os.Parcelable;

import auto.parcel.AutoParcel;
import l.files.fs.Path;

@AutoParcel
abstract class FailureMessage implements Parcelable {

    FailureMessage() {
    }

    public abstract Path getPath();

    public abstract String getMessage();

    public static FailureMessage create(Path path, String message) {
        return new AutoParcel_FailureMessage(path, message);
    }

}
