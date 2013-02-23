package com.example.files.lib.ui.adapters;

import static com.example.files.lib.test.TempFolder.newTempFolder;
import static com.example.files.lib.ui.adapters.FileListAdapterTest.TestActivity.EXTRA_FOLDER;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.lib.R;
import com.example.files.lib.test.TempFolder;
import com.example.files.lib.ui.adapters.FileListAdapterTest.TestActivity;

public final class FileListAdapterTest
    extends ActivityInstrumentationTestCase2<TestActivity> {

  public static class TestActivity extends ListActivity {
    static final String EXTRA_FOLDER = "folder";

    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      String folder = getIntent().getStringExtra(EXTRA_FOLDER);
      setListAdapter(new FileListAdapter(this, new File(folder).listFiles()));
    }
  }

  private TempFolder folder;

  public FileListAdapterTest() {
    super(TestActivity.class);
  }

  public void testViewIsDisabledForUnreadableFile() throws Exception {
    createReadableFile(false);
    assertFalse(view(0).isEnabled());
  }

  public void testViewIsEnabledForReadableFile() throws Exception {
    createReadableFile(true);
    assertTrue(view(0).isEnabled());
  }

  public void testViewShowsFileName() throws Exception {
    File file = folder.newFile();
    assertEquals(file.getName(), text(0));
  }

  public void testViewShowsIconForFolder() {
    folder.newFolder();
    assertIcon(R.drawable.ic_dir, icon(0));
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    folder = newTempFolder();
    setTestIntent(folder.get());
  }

  @Override protected void tearDown() throws Exception {
    try {
      folder.delete();
    } finally {
      super.tearDown();
    }
  }

  private void assertIcon(int expectedResId, Bitmap actual) {
    assertTrue(Arrays.equals(toBytes(expectedResId), toBytes(actual)));
  }

  private File createReadableFile(boolean readable) throws IOException {
    File file = folder.newFile();
    file.setReadable(readable, false);
    return file;
  }

  private Bitmap getBitmap(int resId) {
    return ((BitmapDrawable)getDrawable(resId)).getBitmap();
  }

  private Drawable getDrawable(int resId) {
    return getActivity().getResources().getDrawable(resId);
  }

  private Bitmap icon(int i) {
    Drawable drawable = textView(i).getCompoundDrawables()[0];
    return ((BitmapDrawable)drawable).getBitmap();
  }

  private ListView listView() {
    return (ListView)getActivity().findViewById(android.R.id.list);
  }

  private void setTestIntent(File folder) {
    setActivityIntent(new Intent()
        .putExtra(EXTRA_FOLDER, folder.getAbsolutePath()));
  }

  private String text(int i) {
    return textView(i).getText().toString();
  }

  private TextView textView(int i) {
    return (TextView)view(i).findViewById(android.R.id.text1);
  }

  private byte[] toBytes(Bitmap bitmap) {
    ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
    bitmap.copyPixelsToBuffer(buffer);
    return buffer.array();
  }

  private byte[] toBytes(int resId) {
    return toBytes(getBitmap(resId));
  }

  private View view(int i) {
    return listView().getChildAt(i);
  }
}
