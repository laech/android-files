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
import com.squareup.otto.Bus;
import l.files.R;
import l.files.event.SortRequest;
import l.files.setting.SortBy;
import l.files.ui.app.files.sort.Sorter;
import l.files.ui.app.files.sort.Sorters;

import static android.widget.AdapterView.OnItemClickListener;
import static l.files.event.Events.bus;

final class SortDialog extends DialogFragment implements OnItemClickListener {

  static final Supplier<SortDialog> CREATOR = new Supplier<SortDialog>() {
    @Override public SortDialog get() {
      return new SortDialog();
    }
  };

  Bus bus = bus();

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.Dialog);
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
    SortBy sort = ((Sorter) parent.getItemAtPosition(position)).id();
    bus.post(new SortRequest(sort));
    getDialog().dismiss();
  }

  class SorterAdapter extends ArrayAdapter<Sorter> {

    SorterAdapter(Context context) {
      super(context, R.layout.sort_by_item, Sorters.get(getResources()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = (TextView) super.getView(position, convertView, parent);
      view.setText(getItem(position).name(getResources()));
      return view;
    }
  }
}
