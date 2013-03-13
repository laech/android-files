package com.example.files.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.example.files.R;
import com.example.files.media.ImageMap;
import com.example.files.util.FileSystem;
import junit.framework.TestCase;

import java.io.File;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FileListAdapterTest extends TestCase {

  private TextView view;
  private File file;
  private FileSystem fs;
  private ImageMap images;
  private FileListAdapter adapter;

  public void testIsEnabledReturnsFalseIfUserHasNoPermissionToReadFile() {
    given(fs.hasPermissionToRead(file)).willReturn(false);
    assertFalse(adapter.isEnabled(0));
  }

  public void testViewIsDisabledIfUserHasNoPermissionToReadFile() {
    setAsFileWithReadPermission(file, false);
    adapter.updateView(view, file);
    verify(view).setEnabled(false);
  }

  public void testViewIsDisabledIfUserHasNoPermissionToReadDirectory() {
    setAsDirectoryWithReadPermission(file, false);
    adapter.updateView(view, file);
    verify(view).setEnabled(false);
  }

  public void testViewShowsFileName() throws Exception {
    given(file.getName()).willReturn("a");
    adapter.updateView(view, file);
    verify(view).setText("a");
  }

  public void testViewShowsIcon() {
    given(images.get(file)).willReturn(R.drawable.ic_launcher);
    adapter.updateView(view, file);
    verify(view).setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.ic_launcher, 0, 0, 0);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();

    view = mock(TextView.class);
    file = mock(File.class);
    fs = mock(FileSystem.class);
    images = mock(ImageMap.class);
    adapter = new FileListAdapter(mockContext(), new File[]{file}, fs, images);
  }

  private Context mockContext() {
    LayoutInflater inflater = mock(LayoutInflater.class);
    Context ctx = mock(Context.class);
    given(ctx.getSystemService(LAYOUT_INFLATER_SERVICE)).willReturn(inflater);
    return ctx;
  }

  private void setAsFile(File file) {
    given(file.isDirectory()).willReturn(false);
    given(file.isFile()).willReturn(true);
  }

  private void setAsFileWithReadPermission(File file, boolean hasPermission) {
    setAsFile(file);
    given(fs.hasPermissionToRead(file)).willReturn(hasPermission);
  }

  private void setAsDirectory(File file) {
    given(file.isDirectory()).willReturn(true);
    given(file.isFile()).willReturn(false);
  }

  private void setAsDirectoryWithReadPermission(File file, boolean hasPermission) {
    setAsDirectory(file);
    given(fs.hasPermissionToRead(file)).willReturn(hasPermission);
  }

}
