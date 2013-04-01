package com.example.files.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.example.files.app.SidebarFragment;

public final class TestSidebarFragmentActivity extends FragmentActivity {

  private SidebarFragment fragment;

  public SidebarFragment getFragment() {
    return fragment;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    View view = new View(this);
    view.setId(android.R.id.content);
    setContentView(view);

    fragment = new SidebarFragment();
    getSupportFragmentManager()
        .beginTransaction()
        .replace(android.R.id.content, fragment)
        .commit();
  }
}
