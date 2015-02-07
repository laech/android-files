package l.files.operations.ui

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import l.files.fs.Path

data class FailureMessage(val path: Path, val message: String) : Parcelable {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, 0)
        dest.writeString(message)
    }

    override fun describeContents() = 0

    class object {

        val CREATOR: Creator<FailureMessage> = object : Creator<FailureMessage> {
            override fun createFromParcel(source: Parcel): FailureMessage {
                val path = source.readParcelable<Path>(javaClass.getClassLoader())
                val message = source.readString()
                return FailureMessage(path, message)
            }

            override fun newArray(size: Int) = arrayOfNulls<FailureMessage>(size)
        }

    }

}
