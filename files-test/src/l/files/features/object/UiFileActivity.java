package l.files.features.object;

import android.app.Instrumentation;
import android.view.ActionMode;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import l.files.R;
import l.files.app.FilesActivity;

import java.io.File;
import java.util.concurrent.Callable;

import static android.view.View.VISIBLE;
import static junit.framework.Assert.assertEquals;
import static l.files.common.widget.ListViews.getItems;
import static l.files.features.object.Instrumentations.awaitOnMainThread;

public final class UiFileActivity {

    private final Instrumentation mInstrumentation;
    private final FilesActivity mActivity;

    public UiFileActivity(Instrumentation instrumentation, FilesActivity activity) {
        mInstrumentation = instrumentation;
        mActivity = activity;
    }

    public UiFileActivity check(final File file, final boolean checked) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() {
                ListView list = getListView();
                int i = getItems(list).indexOf(file);
                if (i > -1) {
                    list.setItemChecked(i, checked);
                    return true;
                }
                return false;
            }
        });
        return this;
    }

    public UiRename rename() {
        return awaitOnMainThread(mInstrumentation, new Callable<UiRename>() {
            @Override
            public UiRename call() {
                final ActionMode mode = mActivity.getCurrentActionMode();
                final ActionMode.Callback callback = mActivity.getCurrentActionModeCallback();
                callback.onActionItemClicked(mode, getRenameMenuItem());
                return new UiRename(mInstrumentation, mActivity);
            }
        });
    }

    public UiFileActivity selectItem(final File file) {
        return awaitOnMainThread(mInstrumentation, new Callable<UiFileActivity>() {
            @Override
            public UiFileActivity call() {
                if (getItems(getListView()).isEmpty()) {
                    return null;
                }
                final ListView list = getListView();
                final int position = getItems(list).indexOf(file);
                final int firstVisiblePosition = list.getFirstVisiblePosition();
                final int viewPosition = position - firstVisiblePosition;
                final View view = list.getChildAt(viewPosition);
                list.performItemClick(view, viewPosition, position);
                return UiFileActivity.this;
            }
        });
    }


    public UiFileActivity selectPage(final int position) {
        return awaitOnMainThread(mInstrumentation, new Callable<UiFileActivity>() {
            @Override
            public UiFileActivity call() {
                mActivity.getViewPager().setCurrentItem(position, false);
                return UiFileActivity.this;
            }
        });
    }

    public UiFileActivity selectTab(final int position) {
        return awaitOnMainThread(mInstrumentation, new Callable<UiFileActivity>() {
            @Override
            public UiFileActivity call() throws Exception {
                mActivity.getViewPagerTabBar().getTabAt(position).getRootView().performClick();
                return UiFileActivity.this;
            }
        });
    }

    public UiFileActivity openNewTab() {
        mInstrumentation.invokeMenuActionSync(mActivity, R.id.new_tab, 0);
        return this;
    }

    public UiFileActivity pressBack() {
        return awaitOnMainThread(mInstrumentation, new Callable<UiFileActivity>() {
            @Override
            public UiFileActivity call() throws Exception {
                mActivity.onBackPressed();
                return UiFileActivity.this;
            }
        });
    }

    public UiFileActivity assertCanRename(final boolean can) {
        assertEquals(can, getRenameMenuItem().isEnabled());
        return this;
    }

    private MenuItem getRenameMenuItem() {
        return mActivity.getCurrentActionMode().getMenu().findItem(R.id.rename);
    }

    public UiFileActivity assertTabCount(final int count) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return mActivity.getViewPagerTabBar().getTabCount() == count;
            }
        });
        return this;
    }

    public UiFileActivity assertCurrentDirectory(final File dir) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return dir.equals(mActivity.getCurrentPagerFragment().getCurrentDirectory());
            }
        });
        return this;
    }

    public UiFileActivity assertSelectedTabPosition(final int position) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return mActivity.getViewPager().getCurrentItem() == position;
            }
        });
        return this;
    }

    private ListView getListView() {
        return (ListView) mActivity
                .findViewById(R.id.file_list_fragment)
                .findViewById(android.R.id.list);
    }

    public UiFileActivity assertTabHighlightedAt(final int position, final boolean highlighted) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return highlighted == mActivity
                        .getViewPagerTabBar()
                        .getTabAt(position)
                        .getRootView()
                        .isSelected();
            }
        });
        return this;
    }

    public UiFileActivity assertTabBackIndicatorVisibleAt(final int position, final boolean visible) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return visible == (VISIBLE == mActivity
                        .getViewPagerTabBar()
                        .getTabAt(position)
                        .getBackIndicatorView()
                        .getVisibility());
            }
        });
        return this;
    }

    public UiFileActivity assertTabTitleAt(final int position, final String title) {
        awaitOnMainThread(mInstrumentation, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return title.equals(mActivity
                        .getViewPagerTabBar()
                        .getTabAt(position)
                        .getTitleView()
                        .getText());
            }
        });
        return this;
    }
}
