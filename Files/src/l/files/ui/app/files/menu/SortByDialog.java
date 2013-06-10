package l.files.ui.app.files.menu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.base.Supplier;
import l.files.R;

import static android.widget.AdapterView.OnItemClickListener;

public class SortByDialog extends DialogFragment implements OnItemClickListener {

  public static final Supplier<SortByDialog> CREATOR = new Supplier<SortByDialog>() {
    @Override public SortByDialog get() {
      return new SortByDialog();
    }
  };

  public static final Integer[] OPTIONS = new Integer[]{
      R.string.name,
      R.string.date_modified,
      R.string.size
  };

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.Dialog);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getDialog().setTitle(R.string.sort_by);

    ListView list = (ListView) getView().findViewById(android.R.id.list);
    list.setAdapter(new SortByAdapter(getActivity()));
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
    // TODO
  }

  static class SortByAdapter extends ArrayAdapter<Integer> {

    SortByAdapter(Context context) {
      super(context, R.layout.sort_by_item, OPTIONS);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = (TextView) super.getView(position, convertView, parent);
      view.setText(view.getContext().getString(getItem(position)));
      return view;
    }
  }
}
