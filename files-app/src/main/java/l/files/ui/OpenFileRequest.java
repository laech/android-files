package l.files.ui;

import auto.parcel.AutoParcel;
import l.files.fs.Resource;

@AutoParcel
public abstract class OpenFileRequest {

    OpenFileRequest() {
    }

    public abstract Resource getResource();

    public static OpenFileRequest create(Resource resource) {
        return new AutoParcel_OpenFileRequest(resource);
    }

}
