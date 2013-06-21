package l.files.ui.app.files;

import static android.text.format.Formatter.formatShortFileSize;
import static java.util.Arrays.asList;
import static l.files.ui.app.files.FilesAdapter.ViewHolder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;

import l.files.R;
import l.files.media.ImageMap;
import l.files.util.DateTimeFormat;
import l.files.util.FileSystem;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public final class FilesAdapterTest extends AndroidTestCase {

  private File file;
  private FileSystem files;
  private ImageMap images;
  private ListView list;
  private DateTimeFormat format;

  private FilesAdapter adapter;

  @Override protected void setUp() throws Exception {
    super.setUp();

    file = mock(File.class);
    given(file.getName()).willReturn("a");

    files = mock(FileSystem.class);
    images = mock(ImageMap.class);
    list = new ListView(getContext());
    format = new DateTimeFormat(getContext());

    adapter = new FilesAdapter(list, files, images, format);
    adapter.replaceAll(asList(file));
  }

  public void testViewShowsDirectoryUpdatedTime() {
    ViewHolder holder = new ViewHolder();
    holder.info = new TextView(getContext());
    given(file.lastModified()).willReturn(1010L);
    given(file.isFile()).willReturn(false);
    given(file.isDirectory()).willReturn(true);

    adapter.showFileInfo(file, holder);

    String expected = format.format(file.lastModified());
    assertEquals(expected, holder.info.getText());
  }

  public void testViewShowsFileSizeAndUpdatedTime() {
    ViewHolder holder = new ViewHolder();
    holder.info = new TextView(getContext());
    given(file.lastModified()).willReturn(1010L);
    given(file.length()).willReturn(101L);
    given(file.isFile()).willReturn(true);
    given(file.isDirectory()).willReturn(false);

    adapter.showFileInfo(file, holder);

    String size = formatShortFileSize(getContext(), file.length());
    String updated = format.format(file.lastModified());
    assertEquals(
        getContext().getString(R.string.file_size_updated, size, updated),
        holder.info.getText());
  }

  public void testViewShowsFilename() {
    assertEquals(file.getName(), filename(view()));
  }

  public void testViewShowsIcon() {
    view();
    verify(images).get(file);
  }

  public void testViewIsDisabledIfHasNoPermissionToReadFile() {
    given(files.hasPermissionToRead(file)).willReturn(false);
    assertFalse(view().isEnabled());
  }

  public void testViewFilenameIsDisabledIfHasNoPermissionToReadFile() {
    given(files.hasPermissionToRead(file)).willReturn(false);
    assertFalse(view().findViewById(R.id.name).isEnabled());
  }

  public void testViewIsEnabledIfHasPermissionToReadFile() {
    given(files.hasPermissionToRead(file)).willReturn(true);
    assertTrue(view().isEnabled());
  }

  public void testViewFilenameIsEnabledIfHasPermissionToReadFile() {
    given(files.hasPermissionToRead(file)).willReturn(true);
    assertTrue(view().findViewById(R.id.name).isEnabled());
  }

  private View view() {
    return adapter.getView(0, null, list);
  }

  private CharSequence filename(View view) {
    return ((TextView) view.findViewById(R.id.name)).getText();
  }

}
