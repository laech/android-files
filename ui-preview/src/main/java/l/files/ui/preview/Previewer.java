package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;

interface Previewer {

    boolean accept(Path path, String mediaType);

    Decode create(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context);

}
