package l.files.ui.browser.menu;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import l.files.ui.browser.R;
import l.files.ui.browser.preference.Preferences;
import l.files.ui.browser.sort.FileSort;

import static l.files.ui.browser.preference.Preferences.getSort;

public final class SortDialog
        extends AppCompatDialogFragment
        implements DialogInterface.OnClickListener {

    public static final String FRAGMENT_TAG = "sort-dialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        assert context != null;
        return new AlertDialog.Builder(context)
                .setTitle(R.string.sort_by)
                .setAdapter(new SorterAdapter(context), this)
                .create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        ListView listView = ((AlertDialog) dialog).getListView();
        FileSort sort = (FileSort) listView.getItemAtPosition(which);
        Preferences.setSort(getActivity(), sort);
        dialog.dismiss();
    }

    private static final class SorterAdapter extends ArrayAdapter<FileSort> {

        SorterAdapter(Context context) {
            super(context, R.layout.sort_by_item, FileSort.values());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            FileSort item = getItem(position);
            CheckedTextView check = view.findViewById(R.id.title);
            assert item != null;
            check.setText(item.getLabel(view.getResources()));
            check.setChecked(item.equals(getSort(parent.getContext())));
            return view;
        }
    }

}
