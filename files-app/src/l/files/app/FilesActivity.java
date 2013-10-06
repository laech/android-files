package l.files.app;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import com.google.common.base.Function;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.common.app.BaseFragmentActivity;
import l.files.common.app.OptionsMenus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static android.app.ActionBar.LayoutParams;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getParcelableArrayList;
import static l.files.app.FilesApp.getBus;
import static l.files.app.UserDirs.DIR_HOME;
import static l.files.app.format.Formats.label;
import static l.files.app.menu.Menus.newCloseTabMenu;
import static l.files.app.menu.Menus.newTabMenu;

public final class FilesActivity extends BaseFragmentActivity implements TabHandler {

    public static final String EXTRA_DIR = FilesPagerFragment.ARG_DIRECTORY;

    private static final String STATE_TAB_ITEMS = "tabTitles";
    private static final String STATE_ID_SEED = "idGenerator";

    Bus mBus;
    File mDirectory;

    ViewPager mViewPager;
    ViewPagerTabBar mTabs;

    ActionMode mCurrentActionMode;
    ActionMode.Callback mCurrentActionModeCallback;

    FilesPagerFragment mCurrentPagerFragment;
    DrawerLayout mDrawerLayout;
    DrawerListener mDrawerListener;
    Function<File, String> mLabels;
    IdGenerator mIdGenerator;

    final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.files_activity);

        initFields(getSavedId(state));
        setViewPager(getSavedTabItems(state));
        setDrawer();
        setActionBar(getActionBar());
        setOptionsMenu(OptionsMenus.compose(
                newTabMenu(this),
                newCloseTabMenu(this)));
    }

    private int getSavedId(Bundle state) {
        return getInt(state, STATE_ID_SEED, 0);
    }

    private ArrayList<TabItem> getSavedTabItems(Bundle state) {
        return getParcelableArrayList(state, STATE_TAB_ITEMS, TabItem.class);
    }

    private File getInitialDirectory() {
        String path = getIntent().getStringExtra(EXTRA_DIR);
        return path == null ? DIR_HOME : new File(path);
    }

    private void initFields(int idSeed) {
        mIdGenerator = new IdGenerator(idSeed);
        mLabels = label(getResources());
        mBus = getBus(this);
        mDirectory = getInitialDirectory();
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabs = new ViewPagerTabBar(this, mBus);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerListener = new DrawerListener();
    }

    private void setViewPager(ArrayList<TabItem> items) {
        if (items.isEmpty()) {
            items = newArrayList(new TabItem(mIdGenerator.get(), mDirectory, mLabels.apply(mDirectory)));
        }
        mViewPager.setAdapter(new FilesPagerAdapter(items));
        mViewPager.setOffscreenPageLimit(2);
        mTabs.setViewPager(mViewPager);
    }

    private void setDrawer() {
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerLayout.setDrawerListener(mDrawerListener);
    }

    private void setActionBar(ActionBar actionBar) {
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(mTabs.getRootContainer(), new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_ID_SEED, mIdGenerator.get());
        outState.putParcelableArrayList(STATE_TAB_ITEMS, newArrayList(getPagerAdapter().getItems()));
    }

    @Override
    public void onBackPressed() {
        if (mCurrentPagerFragment.popBackStack()) {
            return;
        }
        if (mViewPager.getAdapter().getCount() > 1) {
            closeCurrentTab();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KEYCODE_BACK) {
            while (mCurrentPagerFragment.hasBackStack()) {
                mCurrentPagerFragment.popBackStack();
            }
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
        mCurrentActionMode = null;
        mCurrentActionModeCallback = null;
        mViewPager.setEnabled(true);
        mDrawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        super.onActionModeStarted(mode);
        mCurrentActionMode = mode;
        mViewPager.setEnabled(false);
        mDrawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        mCurrentActionModeCallback = callback;
        return super.onWindowStartingActionMode(callback);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!mDrawerLayout.isDrawerOpen(Gravity.START)) {
            return super.onPrepareOptionsMenu(menu);
        }
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        return false;
    }

    public ActionMode getCurrentActionMode() {
        return mCurrentActionMode;
    }

    public ActionMode.Callback getCurrentActionModeCallback() {
        return mCurrentActionModeCallback;
    }

    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    public ViewPager getViewPager() {
        return mViewPager;
    }

    public ViewPagerTabBar getViewPagerTabBar() {
        return mTabs;
    }

    public FilesPagerFragment getCurrentPagerFragment() {
        return mCurrentPagerFragment;
    }

    private FilesPagerAdapter getPagerAdapter() {
        return (FilesPagerAdapter) mViewPager.getAdapter();
    }

    @Subscribe
    public void handle(@SuppressWarnings("UnusedParameters") CloseActionModeRequest request) {
        if (mCurrentActionMode != null) {
            mCurrentActionMode.finish();
        }
    }

    @Subscribe
    public void handle(final OpenFileRequest request) {
        closeDrawerThenRun(new Runnable() {
            @Override
            public void run() {
                mCurrentPagerFragment.show(request.value());
            }
        });
    }

    @Subscribe
    public void handle(@SuppressWarnings("UnusedParameters") ViewPagerTabBar.OnUpSelected up) {
        if (mCurrentPagerFragment.hasBackStack()) {
            closeDrawerThenRun(new Runnable() {
                @Override
                public void run() {
                    mCurrentPagerFragment.popBackStack();
                }
            });
        } else if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    @Override
    public void openNewTab() {
        FilesPagerAdapter adapter = getPagerAdapter();
        adapter.addItem(mIdGenerator.get());
        mViewPager.setCurrentItem(adapter.getCount() - 1, true);
    }

    @Override
    public void closeCurrentTab() {
        if (getPagerAdapter().getCount() == 1) {
            finish();
        } else {
            getPagerAdapter().removeCurrentItem();
        }
    }

    private void closeDrawerThenRun(Runnable runnable) {
        if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
            mDrawerListener.mRunOnClosed = runnable;
            mDrawerLayout.closeDrawers();
        } else {
            runnable.run();
        }
    }

    class FilesPagerAdapter extends FragmentPagerAdapter {
        private final List<TabItem> mItems;
        private final Map<Object, Integer> mPositions;

        FilesPagerAdapter(List<TabItem> items) {
            super(getSupportFragmentManager());
            mItems = items;
            mPositions = newHashMap();
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).getId();
        }

        @Override
        public Fragment getItem(int position) {
            final File directory = mItems.get(position).getDirectory();
            final Fragment fragment = FilesPagerFragment.create(directory);
            mPositions.put(fragment, position);
            return fragment;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, final int position, Object object) {
            super.setPrimaryItem(container, position, object);
            if (mCurrentPagerFragment != null && mCurrentPagerFragment != object) {
                mCurrentPagerFragment.setHasOptionsMenu(false);
            }
            mCurrentPagerFragment = (FilesPagerFragment) object;
            mCurrentPagerFragment.setHasOptionsMenu(true);
            updateTabTitle(position);
        }

        private void updateTabTitle(final int position) {
            final String title = mLabels.apply(mCurrentPagerFragment.getCurrentDirectory());
            mItems.get(position).setTitle(title);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mTabs.updateTab(position, title, mCurrentPagerFragment.hasBackStack());
                }
            });
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mItems.get(position).getTitle();
        }

        @Override
        public int getItemPosition(Object object) {
            final Integer position = mPositions.get(object);
            return position != null ? position : POSITION_NONE;
        }

        List<TabItem> getItems() {
            return mItems;
        }

        void addItem(int id) {
            final String title = mLabels.apply(DIR_HOME);
            mItems.add(new TabItem(id, DIR_HOME, title));
            mTabs.addTab(title);
            notifyDataSetChanged();
        }

        void removeCurrentItem() {
            mTabs.removeTab(mViewPager.getCurrentItem());
            mItems.remove(mViewPager.getCurrentItem());
            mPositions.remove(mCurrentPagerFragment);
            notifyDataSetChanged();
            if (mViewPager.getCurrentItem() >= getCount()) {
                mViewPager.setCurrentItem(getCount() - 1);
            }
        }
    }

    class DrawerListener extends DrawerLayout.SimpleDrawerListener {

        Runnable mRunOnClosed;

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu();
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            invalidateOptionsMenu();
            if (mRunOnClosed != null) {
                mRunOnClosed.run();
                mRunOnClosed = null;
            }
        }
    }
}
