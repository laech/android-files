package l.files.ui.browser.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import javax.annotation.Nullable;

import l.files.ui.browser.FileSort;
import l.files.ui.browser.preference.Preferences;
import l.files.ui.browser.R;

import static l.files.ui.browser.preference.Preferences.getSort;

public final class SortDialog
        extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {

    public static final String FRAGMENT_TAG = "sort-dialog";

    @Override
    @SuppressWarnings("NullableProblems")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(R.string.sort_by)
                .setAdapter(new SorterAdapter(getContext()), this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ListView listView = ((AlertDialog) dialog).getListView();
        FileSort sort = (FileSort) listView.getItemAtPosition(which);
        Preferences.setSort(getActivity(), sort);
        dialog.dismiss();
    }

    private static class SorterAdapter extends ArrayAdapter<FileSort> {

        SorterAdapter(Context context) {
            super(context, R.layout.sort_by_item, FileSort.values());
        }

        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            FileSort item = getItem(position);
            CheckedTextView check = (CheckedTextView) view.findViewById(R.id.title);
            assert item != null;
            check.setText(item.getLabel(view.getResources()));
            check.setChecked(item.equals(getSort(parent.getContext())));
            return view;
        }
    }

}
