package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.Rect;

interface Previewer {

    boolean acceptsFileExtension(Path path, String extensionInLowercase);

    boolean acceptsMediaType(Path path, String mediaTypeInLowercase);

    Decode create(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context);

}
