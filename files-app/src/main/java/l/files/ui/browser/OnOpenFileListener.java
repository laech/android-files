package l.files.ui.browser;

import android.support.annotation.Nullable;

import l.files.fs.File;
import l.files.fs.Stat;

public interface OnOpenFileListener {

    void onOpen(File file);

    void onOpen(File file, @Nullable Stat stat);

}
