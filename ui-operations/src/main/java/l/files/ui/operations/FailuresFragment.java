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

import android.support.annotation.Nullable;

import static android.graphics.Color.TRANSPARENT;

public final class FailuresFragment extends ListFragment {

    @Nullable
    private Adapter adapter;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new Adapter();
        setListAdapter(adapter);
        getListView().setSelector(new ColorDrawable(TRANSPARENT));
    }

    public void setFailures(List<FailureMessage> failures) {
        assert adapter != null;
        adapter.setNotifyOnChange(false);
        adapter.clear();
        adapter.addAll(failures);
        adapter.notifyDataSetChanged();
    }

    private class Adapter extends ArrayAdapter<FailureMessage> {

        Adapter() {
            super(getActivity(), 0);
        }

        @Override
        public View getView(int position, @Nullable View convertView, ViewGroup parent) {
            if (convertView == null) {
                int layout = R.layout.failures_item;
                convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
            }
            TextView pathView = convertView.findViewById(R.id.failure_path);
            TextView msgView = convertView.findViewById(R.id.failure_message);
            FailureMessage item = getItem(position);
            assert item != null;
            pathView.setText(item.path().toString());
            msgView.setText(item.message());
            return convertView;
        }
    }
}
