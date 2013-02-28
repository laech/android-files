package com.example.files.ui.adapters;

import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.test.TestFileListAdapterActivity.EXTRA_FOLDER;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.files.R;
import com.example.files.test.TempFolder;
import com.example.files.test.TestFileListAdapterActivity;

public final class FileListAdapterTest
    extends ActivityInstrumentationTestCase2<TestFileListAdapterActivity> {

  private TempFolder folder;

  public FileListAdapterTest() {
    super(TestFileListAdapterActivity.class);
  }

  public void testViewIsDisabledForUnexecutableFolder() throws Exception {
    folder.newFolder().setExecutable(false, false);
    assertFalse(view(0).isEnabled());
  }

  public void testViewIsDisabledForUnreadableFile() throws Exception {
    folder.newFile().setReadable(false, false);
    assertFalse(view(0).isEnabled());
  }

  public void testViewIsDisabledForUnreadableFolder() throws Exception {
    folder.newFolder().setReadable(false, false);
    assertFalse(view(0).isEnabled());
  }

  public void testViewIsEnabledForExecutableFile() throws Exception {
    folder.newFile().setExecutable(true, false);
    assertTrue(view(0).isEnabled());
  }

  public void testViewIsEnabledForExecutableFolder() throws Exception {
    folder.newFolder().setExecutable(true, false);
    assertTrue(view(0).isEnabled());
  }

  public void testViewIsEnabledForReadableFile() throws Exception {
    folder.newFile().setReadable(true, false);
    assertTrue(view(0).isEnabled());
  }

  public void testViewIsEnabledForReadableFolder() throws Exception {
    folder.newFolder().setReadable(true, false);
    assertTrue(view(0).isEnabled());
  }

  public void testViewIsEnabledForUnexecutableFile() throws Exception {
    folder.newFile().setExecutable(false, false);
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
