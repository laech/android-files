package l.files.ui.base.fs;

import android.support.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.Stat;

public interface OnOpenFileListener {

    void onOpen(Path file);

    void onOpen(Path file, @Nullable Stat stat);

}
