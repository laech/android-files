package l.files.ui.browser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import l.files.fs.Files;
import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.browser.databinding.FilesActivityTitleBinding;
import l.files.ui.browser.databinding.FilesActivityTitleItemBinding;

import static android.graphics.Color.WHITE;
import static android.graphics.PorterDuff.Mode.SRC_ATOP;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

final class HierarchyAdapter extends BaseAdapter {

    private List<Path> hierarchy = emptyList();

    @Nullable
    private Path directory;

    void set(Path dir) {
        directory = dir;
        hierarchy = new ArrayList<>(Files.hierarchy(dir));
        Collections.reverse(hierarchy);
        hierarchy = unmodifiableList(hierarchy);
        notifyDataSetChanged();
    }

    List<Path> get() {
        return hierarchy;
    }

    int indexOf(Path dir) {
        return hierarchy.indexOf(dir);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return isEnabled(getItem(position));
    }

    private boolean isEnabled(Path path) {
        assert directory != null;
        return !directory.equals(path);
    }

    @Override
    public int getCount() {
        return hierarchy.size();
    }

    @Override
    public Path getItem(int position) {
        return hierarchy.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        FilesActivityTitleBinding binding;
        if (convertView != null) {
            binding = (FilesActivityTitleBinding) convertView.getTag();
        } else {
            binding = FilesActivityTitleBinding.inflate(inflator(parent), parent, false);
            binding.icon.setColorFilter(WHITE, SRC_ATOP);
            binding.getRoot().setTag(binding);
        }
        Path path = getItem(position);
        binding.setPath(path);
        return binding.getRoot();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, ViewGroup parent) {
        FilesActivityTitleItemBinding binding;
        if (convertView != null) {
            binding = (FilesActivityTitleItemBinding) convertView.getTag();
        } else {
            binding = FilesActivityTitleItemBinding.inflate(inflator(parent), parent, false);
            binding.getRoot().setTag(binding);
        }

        Path path = getItem(position);
        Name name = path.name();
        binding.setPath(path);
        binding.setEnabled(isEnabled(path));
        binding.setName(!name.isEmpty() ? name.toString() : path.toString());

        return binding.getRoot();
    }

    private LayoutInflater inflator(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext());
    }
}
