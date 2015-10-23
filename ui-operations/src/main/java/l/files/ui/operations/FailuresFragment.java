package l.files.ui.operations;

import android.app.ListFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import static android.graphics.Color.TRANSPARENT;

public final class FailuresFragment extends ListFragment {

    private Adapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new Adapter();
        setListAdapter(adapter);
        getListView().setSelector(new ColorDrawable(TRANSPARENT));
    }

    public void setFailures(List<FailureMessage> failures) {
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(failures);
        adapter.notifyDataSetChanged();
    }

    class Adapter extends ArrayAdapter<FailureMessage> {

        Adapter() {
            super(getActivity(), 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                int layout = R.layout.failures_item;
                convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
            }
            TextView pathView = (TextView) convertView.findViewById(R.id.failure_path);
            TextView msgView = (TextView) convertView.findViewById(R.id.failure_message);
            pathView.setText(getItem(position).file().toString());
            msgView.setText(getItem(position).message());
            return convertView;
        }
    }
}
