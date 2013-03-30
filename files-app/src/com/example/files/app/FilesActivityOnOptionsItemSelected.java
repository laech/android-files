package com.example.files.app;

import android.content.Intent;
import android.view.MenuItem;
import com.example.files.R;

final class FilesActivityOnOptionsItemSelected {

  static boolean handleOnOptionsItemSelected(
      FilesActivity activity, MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        return showHome(activity);
      case R.id.settings:
        return showSettings(activity);
      default:
        return false;
    }
  }

  private static boolean showHome(FilesActivity activity) {
    activity.goHome();
    return true;
  }

  private static boolean showSettings(FilesActivity activity) {
    activity.startActivity(new Intent(activity, SettingsActivity.class));
    return true;
  }

  private FilesActivityOnOptionsItemSelected() {
  }
}
