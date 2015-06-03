package l.files.features.objects;

import android.app.Instrumentation;

import l.files.ui.browser.FilesActivity;
import l.files.ui.rename.RenameFragment;

public final class UiRename extends UiFileCreation<UiRename>
{

    public UiRename(
            final Instrumentation in,
            final FilesActivity activity)
    {
        super(in, activity, RenameFragment.TAG);
    }

}
