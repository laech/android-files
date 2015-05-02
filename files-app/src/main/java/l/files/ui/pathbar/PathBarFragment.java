package l.files.ui.pathbar;

import android.app.Fragment;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import l.files.R;
import l.files.fs.Resource;
import l.files.operations.Events;
import l.files.ui.OpenFileRequest;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static l.files.ui.IconFonts.getDirectoryIcon;

public final class PathBarFragment extends Fragment
        implements OnClickListener {

    private Resource resource;
    private ViewGroup container;

    public void set(Resource resource) {
        this.resource = resource;
        updatePathBar(resource.hierarchy());
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.path_bar, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View root = getView();
        assert root != null;
        container = (ViewGroup) root.findViewById(R.id.path_item_container);
        if (resource != null) {
            updatePathBar(resource.hierarchy());
        }
    }

    private void updatePathBar(List<Resource> hierarchy) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        int i = 0;
        for (; i < hierarchy.size(); i++) {
            Resource resource = hierarchy.get(i);
            View view = container.getChildAt(i);
            if (view == null) {
                view = inflater.inflate(R.layout.path_bar_item, container, false);
                container.addView(view);
            }
            view.setTag(resource);
            view.setVisibility(VISIBLE);
            view.setOnClickListener(this);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(i == 0 ? Build.MODEL : resource.name());

            AssetManager asset = getActivity().getAssets();
            TextView icon = (TextView) view.findViewById(R.id.icon);
            icon.setTypeface(getDirectoryIcon(asset, resource));

        }

        for (; i < container.getChildCount(); i++) {
            container.getChildAt(i).setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        Resource status = (Resource) v.getTag();
        Events.get().post(OpenFileRequest.create(status));
    }

}
