package com.example.files.app;

import android.os.Bundle;
import android.view.MenuItem;

public final class SettingsActivity extends BaseActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .replace(android.R.id.content, new SettingsFragment())
          .commit();
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
