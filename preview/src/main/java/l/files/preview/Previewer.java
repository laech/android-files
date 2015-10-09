package l.files.preview;

import l.files.fs.File;
import l.files.fs.Stat;

interface Previewer {

    boolean accept(String mediaType);

    Decode create(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context);

}
