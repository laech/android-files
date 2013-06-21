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
import l.files.settings.SortSetting;

import static android.widget.AdapterView.OnItemClickListener;
import static l.files.FilesApp.getApp;
import static l.files.settings.SortSetting.Sort.DATE_MODIFIED;
import static l.files.settings.SortSetting.Sort.NAME;

public class SortByDialog extends DialogFragment implements OnItemClickListener {

  public static final Supplier<SortByDialog> CREATOR = new Supplier<SortByDialog>() {
    @Override public SortByDialog get() {
      return new SortByDialog();
    }
  };

  private SortSetting setting;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(STYLE_NORMAL, R.style.Dialog);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setting = getApp(getActivity()).getSortSetting();
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
    setting.set((SortSetting.Sort) parent.getItemAtPosition(position));
    getDialog().dismiss();
  }

  static class SortByAdapter extends ArrayAdapter<SortSetting.Sort> {

    SortByAdapter(Context context) {
      super(context, R.layout.sort_by_item, new SortSetting.Sort[]{
          NAME, DATE_MODIFIED
      });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = (TextView) super.getView(position, convertView, parent);
      view.setText(getItem(position).label(view.getContext()));
      return view;
    }
  }
}
