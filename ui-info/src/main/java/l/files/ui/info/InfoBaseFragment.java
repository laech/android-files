package l.files.ui.info;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.ui.info.CalculateSizeLoader.Size;

import static android.text.format.Formatter.formatFileSize;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public abstract class InfoBaseFragment
        extends AppCompatDialogFragment
        implements LoaderCallbacks<Size> {

    public static final String FRAGMENT_TAG = "info-dialog";

    static final String ARG_PARENT_DIRECTORY = "parentDirectory";
    static final String ARG_CHILDREN = "children";

    private Path parentDirectory;
    private List<Name> children;

    private TextView sizeView;
    private TextView sizeOnDiskView;
    private ProgressBar calculatingSizeView;

    Path getParentDirectory() {
        return parentDirectory;
    }

    List<Name> getChildren() {
        return children;
    }

    public CharSequence getDisplayedSize() {
        return sizeView.getText();
    }

    public CharSequence getDisplayedSizeOnDisk() {
        return sizeOnDiskView.getText();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog_NoTitle);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                layoutResourceId(),
                container,
                false
        );
    }

    @LayoutRes
    abstract int layoutResourceId();

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        findArgs(getArguments());
    }

    private void findViews(View root) {
        sizeView = root.findViewById(R.id.size);
        sizeOnDiskView = root.findViewById(R.id.size_on_disk);
        calculatingSizeView = root.findViewById(R.id.calculate_size_progress_bar);
    }

    private void findArgs(Bundle args) {
        parentDirectory = args.getParcelable(ARG_PARENT_DIRECTORY);
        children = args.getParcelableArrayList(ARG_CHILDREN);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initLoader();
    }

    private void initLoader() {
        getLoaderManager().initLoader(0, null, this);
        calculatingSizeView.post(this::updateViews);
    }

    private void updateViews() {
        CalculateSizeLoader loader = findLoader();
        if (loader != null && loader.isRunning()) {
            Size size = loader.progress();
            updateSizeView(size);
            updateSizeOnDiskView(size);
            showCalculatingSizeView();
            calculatingSizeView.postDelayed(this::updateViews, 100);
        }
    }

    private void updateSizeView(Size data) {
        sizeView.setText(formatSize(data.size, data.count));
    }

    private void updateSizeOnDiskView(Size data) {
        sizeOnDiskView.setText(formatSizeOnDisk(data.sizeOnDisk));
    }

    private void showCalculatingSizeView() {
        calculatingSizeView.setVisibility(VISIBLE);
    }

    private void hideCalculatingSizeView() {
        calculatingSizeView.setVisibility(GONE);
    }

    String formatSize(long size) {
        return formatFileSize(getActivity(), size);
    }

    String formatSize(long size, int count) {
        return getResources().getQuantityString(
                R.plurals.x_size_y_items,
                count,
                formatSize(size),
                count
        );
    }

    String formatSizeOnDisk(long size) {
        return getString(R.string.x_size_on_disk, formatSize(size));
    }

    @Nullable
    private CalculateSizeLoader findLoader() {
        if (getActivity() == null) {
            return null;
        }
        Loader<?> loader = getLoaderManager().getLoader(0);
        return (CalculateSizeLoader) loader;
    }

    @Override
    public Loader<Size> onCreateLoader(int id, Bundle args) {
        return new CalculateSizeLoader(
                getActivity(),
                parentDirectory,
                children
        );
    }

    @Override
    public void onLoadFinished(Loader<Size> loader, Size data) {
        updateSizeView(data);
        updateSizeOnDiskView(data);
        hideCalculatingSizeView();
    }

    @Override
    public void onLoaderReset(Loader<Size> loader) {
    }

}
