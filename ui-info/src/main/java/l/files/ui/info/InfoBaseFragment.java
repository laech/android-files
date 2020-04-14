package l.files.ui.info;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import androidx.appcompat.app.AppCompatDialogFragment;

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
            showCalculatingSizeView();
            calculatingSizeView.postDelayed(this::updateViews, 100);
        }
    }

    private void updateSizeView(Size data) {
        sizeView.setText(formatSize(data.size, data.count));
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
        hideCalculatingSizeView();
    }

    @Override
    public void onLoaderReset(Loader<Size> loader) {
    }

}
