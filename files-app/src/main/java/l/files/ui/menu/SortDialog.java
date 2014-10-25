package l.files.ui.menu;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import l.files.R;
import l.files.ui.Preferences;
import l.files.ui.category.FileCategorizers;

import static android.widget.AdapterView.OnItemClickListener;
import static l.files.ui.Preferences.getSortOrder;
import static l.files.ui.category.FileCategorizers.SortOption;

public final class SortDialog
    extends DialogFragment implements OnItemClickListener {

  public static final String FRAGMENT_TAG = "sort-dialog";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.Theme_Dialog);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getDialog().setTitle(R.string.sort_by);

    ListView list = (ListView) getView().findViewById(android.R.id.list);
    list.setAdapter(new SorterAdapter(getActivity()));
    list.setOnItemClickListener(this);
  }

  @Override public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    ListView view = new ListView(inflater.getContext());
    view.setId(android.R.id.list);
    return view;
  }

  @Override public void onItemClick(
      AdapterView<?> parent, View view, int position, long id) {
    String sortOrder = ((SortOption) parent.getItemAtPosition(position)).sortOrder();
    Preferences.setSortOrder(getActivity(), sortOrder);
    getDialog().dismiss();
  }

  class SorterAdapter extends ArrayAdapter<SortOption> {

    SorterAdapter(Context context) {
      super(context, R.layout.sort_by_item, FileCategorizers.getSortOptions());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = super.getView(position, convertView, parent);
      SortOption item = getItem(position);
      CheckedTextView check = (CheckedTextView) view.findViewById(R.id.title);
      check.setText(item.labelId());
      check.setChecked(item.sortOrder().equals(getSortOrder(parent.getContext())));
      return view;
    }
  }
}
