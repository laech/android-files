package l.files.shared.app;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import l.files.shared.R;
import l.files.shared.app.FilesAdapter;
import l.files.shared.media.ImageMap;
import l.files.shared.util.FileSystem;
import android.app.Application;
import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.view.LayoutInflater;
import android.widget.TextView;

public final class FilesAdapterTest extends AndroidTestCase {

  private TextView view;
  private File file;
  private FileSystem fileSystem;
  private ImageMap imageMap;
  private FilesAdapter adapter;
  private Resources resources;

  @Override protected void setUp() throws Exception {
    super.setUp();

    view = mock(TextView.class);
    file = mock(File.class);
    fileSystem = mock(FileSystem.class);
    imageMap = mock(ImageMap.class);
    resources = mock(Resources.class);
    adapter = new FilesAdapter(mockApplication(), fileSystem, imageMap);
    adapter.add(file);
  }

  public void testGetViewTypeCountIs2() {
    assertEquals(2, adapter.getViewTypeCount());
  }

  public void testGetsItemViewTypeForFile() {
    adapter.clear();
    adapter.add(file);
    assertEquals(0, adapter.getItemViewType(0));
  }

  public void testGetsItemViewTypeForHeader() {
    adapter.clear();
    adapter.add("header");
    assertEquals(1, adapter.getItemViewType(0));
  }

  public void testIsEnabledIfItemIsFile() {
    adapter.clear();
    adapter.add(file);
    assertTrue(adapter.isEnabled(0));
  }

  public void testIsNotEnabledIfItemIsHeader() {
    adapter.clear();
    adapter.add("hello");
    assertFalse(adapter.isEnabled(0));
  }

  public void testGetsViewForFile() {
    adapter.updateViewForHeader("hello", view);
    verify(view).setText("hello");
  }

  public void testViewIsDisabledIfUserHasNoPermissionToReadFile() {
    setAsFileWithReadPermission(file, false);
    adapter.updateViewForFile(file, view);
    verify(view).setEnabled(false);
  }

  public void testViewIsDisabledIfUserHasNoPermissionToReadDirectory() {
    setAsDirectoryWithReadPermission(file, false);
    adapter.updateViewForFile(file, view);
    verify(view).setEnabled(false);
  }

  public void testViewShowsFileName() throws Exception {
    given(fileSystem.getDisplayName(file, resources)).willReturn("a");
    adapter.updateViewForFile(file, view);
    verify(view).setText("a");
  }

  public void testViewShowsIcon() {
    given(imageMap.get(file)).willReturn(R.drawable.ic_directory);
    adapter.updateViewForFile(file, view);
    verify(view).setCompoundDrawablesWithIntrinsicBounds(
        R.drawable.ic_directory, 0, 0, 0);
  }

  private Application mockApplication() {
    LayoutInflater inflater = mock(LayoutInflater.class);
    Application app = mock(Application.class);
    given(app.getSystemService(LAYOUT_INFLATER_SERVICE)).willReturn(inflater);
    given(app.getResources()).willReturn(resources);
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
