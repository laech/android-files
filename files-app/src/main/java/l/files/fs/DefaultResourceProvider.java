package l.files.fs;

import java.io.File;
import java.net.URI;

import l.files.fs.local.LocalResource;

public enum DefaultResourceProvider implements ResourceProvider {

    INSTANCE;

    @Override
    public Resource get(URI uri) {
        return LocalResource.create(new File(uri));
    }

}
