package l.files.features.objects;

import android.app.Instrumentation;

import l.files.ui.browser.FilesActivity;
import l.files.ui.newdir.NewDirFragment;

public final class UiNewDir extends UiFileCreation<UiNewDir>
{

    public UiNewDir(
            final Instrumentation instrument,
            final FilesActivity activity)
    {
        super(instrument, activity, NewDirFragment.TAG);
    }

}
