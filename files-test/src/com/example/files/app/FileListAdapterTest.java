package com.example.files.app;

import android.app.Application;
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
  private FileSystem fileSystem;
  private ImageMap imageMap;
  private FilesAdapter adapter;


  @Override protected void setUp() throws Exception {
    super.setUp();

    view = mock(TextView.class);
    file = mock(File.class);
    fileSystem = mock(FileSystem.class);
    imageMap = mock(ImageMap.class);
    adapter = new FilesAdapter(mockApplication(), fileSystem, imageMap);
    adapter.add(file);
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
    given(imageMap.get(file)).willReturn(R.drawable.ic_launcher);
    adapter.updateView(view, file);
    verify(view).setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.ic_launcher, 0, 0, 0);
  }

  private Application mockApplication() {
    LayoutInflater inflater = mock(LayoutInflater.class);
    Application app = mock(Application.class);
    given(app.getSystemService(LAYOUT_INFLATER_SERVICE)).willReturn(inflater);
    return app;
  }

  private void setAsFile(File file) {
    given(file.isDirectory()).willReturn(false);
    given(file.isFile()).willReturn(true);
  }

  private void setAsFileWithReadPermission(File file, boolean hasPermission) {
    setAsFile(file);
    given(fileSystem.hasPermissionToRead(file)).willReturn(hasPermission);
  }

  private void setAsDirectory(File file) {
    given(file.isDirectory()).willReturn(true);
    given(file.isFile()).willReturn(false);
  }

  private void setAsDirectoryWithReadPermission(File file, boolean permitted) {
    setAsDirectory(file);
    given(fileSystem.hasPermissionToRead(file)).willReturn(permitted);
  }

}
