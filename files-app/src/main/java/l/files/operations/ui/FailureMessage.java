package l.files.operations.ui;

import android.os.Parcelable;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

@AutoParcel
abstract class FailureMessage implements Parcelable {

    FailureMessage() {
    }

    public abstract Resource getResource();

    public abstract String getMessage();

    public static FailureMessage create(Resource resource, String message) {
        return new AutoParcel_FailureMessage(resource, message);
    }

}
