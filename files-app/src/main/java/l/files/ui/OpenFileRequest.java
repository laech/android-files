package l.files.ui;

import auto.parcel.AutoParcel;
import l.files.fs.Path;

@AutoParcel
public abstract class OpenFileRequest {

    OpenFileRequest() {
    }

    public abstract Path getPath();

    public static OpenFileRequest create(Path path) {
        return new AutoParcel_OpenFileRequest(path);
    }

}
