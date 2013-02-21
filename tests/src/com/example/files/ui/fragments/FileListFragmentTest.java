package com.example.files.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.FrameLayout;

import com.example.files.ui.fragments.FileListFragmentTest.TestActivity;

public final class FileListFragmentTest
    extends ActivityInstrumentationTestCase2<TestActivity> {

  public static class TestActivity extends Activity {
    private static final int VIEW_ID = 1;

    public void setFragment(FileListFragment fragment) {
      getFragmentManager()
          .beginTransaction()
          .replace(VIEW_ID, fragment)
          .commit();
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView();
    }

    private void setContentView() {
      FrameLayout root = new FrameLayout(this);
      root.setId(VIEW_ID);
      setContentView(root);
    }
  }

  public FileListFragmentTest() {
    super(TestActivity.class);
  }

  public void testDisablesItemIfFileCannotBeRead() {
    // TODO
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
  }

}
