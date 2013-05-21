package l.files.ui.app.files;

import android.test.AndroidTestCase;
import android.widget.ListView;
import android.widget.TextView;
import l.files.media.ImageMap;
import l.files.util.FileSystem;

import java.io.File;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class FilesAdapterTest extends AndroidTestCase {

  private File file;
  private FileSystem fileSystem;
  private ImageMap imageMap;
  private ListView list;

  private FilesAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();

    file = mock(File.class);
    given(file.getName()).willReturn("a");
    fileSystem = mock(FileSystem.class);
    imageMap = mock(ImageMap.class);
    list = new ListView(getContext());
    adapter = new FilesAdapter(list, fileSystem, imageMap);
    adapter.addAll(asList(file), null);
  }

  public void testViewShowsIcon() {
    int imageResId = 101;
    given(imageMap.get(file)).willReturn(imageResId);
    verify(view()).setCompoundDrawablesWithIntrinsicBounds(imageResId, 0, 0, 0);
  }

  public void testViewShowsFilename() {
    String name = "name-a";
    given(file.getName()).willReturn(name);
    verify(view()).setText(name);
  }

  public void testViewIsDisabledIfHasNoPermissionToReadFile() {
    given(fileSystem.hasPermissionToRead(file)).willReturn(false);
    verify(view()).setEnabled(false);
  }

  public void testViewIsEnabledIfHasPermissionToReadFile() {
    given(fileSystem.hasPermissionToRead(file)).willReturn(true);
    verify(view()).setEnabled(true);
  }

  private TextView view() {
    return (TextView) adapter.getView(0, mock(TextView.class), null);
  }
}
