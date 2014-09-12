package l.files.operations.ui;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.auto.value.AutoValue;

/**
 * Indicates a file operation has failed.
 */
@AutoValue
abstract class FailureMessage implements Parcelable {

  public static final Creator<FailureMessage> CREATOR = new Creator<FailureMessage>() {
    @Override public FailureMessage createFromParcel(Parcel source) {
      String path = source.readString();
      String message = source.readString();
      return create(path, message);
    }

    @Override public FailureMessage[] newArray(int size) {
      return new FailureMessage[size];
    }
  };

  FailureMessage() {}

  /**
   * The path of the failed file.
   */
  public abstract String path();

  /**
   * The failure message of why the operation has failed.
   */
  public abstract String message(); // TODO make translatable

  public static FailureMessage create(String path, String message) {
    return new AutoValue_FailureMessage(path, message);
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(path());
    dest.writeString(message());
  }

  @Override public int describeContents() {
    return 0;
  }
}
