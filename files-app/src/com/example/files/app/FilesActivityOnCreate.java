package com.example.files.app;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.os.Environment.getExternalStorageDirectory;
import static android.text.TextUtils.isEmpty;
import static com.example.files.app.FilesActivity.EXTRA_DIRECTORY;
import static com.example.files.app.FilesPagerAdapter.POSITION_FILE_LIST;

import java.io.File;

import android.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import com.example.files.R;

final class FilesActivityOnCreate {

  private static final File HOME = getExternalStorageDirectory();

  public static void handleOnCreate(FilesActivity activity) {
    String path = activity.getIntent().getStringExtra(EXTRA_DIRECTORY);
    activity.setHomeActivity(isEmpty(path));

    File directory = activity.isHomeActivity() ? HOME : new File(path);
    updateActionBar(activity, directory);
    activity.setContentView(createViewPager(activity, directory));
  }

  private static ViewPager createViewPager(FilesActivity activity, File dir) {
    String path = dir.getAbsolutePath();
    FragmentManager fm = activity.getSupportFragmentManager();
    ViewPager pager = new ViewPager(activity);
    pager.setId(R.id.content);
    pager.setAdapter(new FilesPagerAdapter(fm, path, isPortrait(activity)));
    pager.setCurrentItem(POSITION_FILE_LIST);
    return pager;
  }

  private static boolean isPortrait(FilesActivity activity) {
    return activity.getResources().getConfiguration()
        .orientation == ORIENTATION_PORTRAIT;
  }

  private static void updateActionBar(FilesActivity activity, File directory) {
    ActionBar actionBar = activity.getActionBar();
    actionBar.setDisplayHomeAsUpEnabled(!activity.isHomeActivity());
    actionBar.setHomeButtonEnabled(!activity.isHomeActivity());
    actionBar.setTitle(activity.isHomeActivity()
        ? activity.getString(R.string.home)
        : directory.getName());
  }

  private FilesActivityOnCreate() {
  }
}
