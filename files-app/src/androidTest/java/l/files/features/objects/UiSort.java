package l.files.features.objects;

import android.app.Dialog;
import android.app.Instrumentation;
import android.widget.ListView;

import l.files.ui.browser.FileSort;
import l.files.ui.browser.FilesActivity;
import l.files.ui.menu.SortDialog;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static l.files.features.objects.Instrumentations.awaitOnMainThread;

public final class UiSort
{
    private final Instrumentation instrument;
    private final FilesActivity activity;

    public UiSort(
            final Instrumentation instrument,
            final FilesActivity activity)
    {
        this.instrument = instrument;
        this.activity = activity;
    }

    public UiFileActivity by(final FileSort sort)
    {
        awaitOnMainThread(instrument, new Runnable()
        {
            @Override
            public void run()
            {
                final ListView list = listView();
                for (int i = 0; i < list.getChildCount(); i++)
                {
                    final FileSort that = (FileSort) list.getItemAtPosition(i);
                    if (that == sort)
                    {
                        list.performItemClick(list.getChildAt(i), i, i);
                        return;
                    }
                }
                fail();
            }
        });
        return new UiFileActivity(instrument, activity);
    }

    private ListView listView()
    {
        return (ListView) dialog().findViewById(android.R.id.list);
    }

    private Dialog dialog()
    {
        final SortDialog fragment = (SortDialog)
                activity.getFragmentManager()
                        .findFragmentByTag(SortDialog.FRAGMENT_TAG);
        assertNotNull(fragment);
        final Dialog dialog = fragment.getDialog();
        assertNotNull(dialog);
        return dialog;
    }
}
