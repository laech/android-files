package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.util.Files.getFileExtension;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import com.example.files.R;
import com.example.files.content.ActivityStarter;
import com.example.files.media.MediaMap;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

public final class FileClickHandlerTest extends TestCase {

  private FileClickHandler handler;

  private ActivityStarter starter;
  private FileSystem fs;
  private MediaMap medias;
  private Toaster toaster;

  private FileListActivity activity;
  private File file;

  public void testShowsFileIfMediaTypeIsNotNull() {
    String type = "text/plain";
    setFileWithMediaType(type);
    handleEvent();
    assertFileShown(type);
  }

  public void testShowsDirectoryIfGotPermissionToReadDirectory() {
    setDirectory();
    handleEvent();
    assertDirectoryShown();
  }

  public void testShowsNoAppFoundIfNoAppCanOpenFileWithMediaType() {
    String type = "text/plain";
    setFileWithMediaType(type);
    doThrow(new ActivityNotFoundException())
        .when(starter).startActivity(any(Context.class), any(Intent.class));

    handleEvent();

    assertNoAppToOpenFileShown();
  }

  public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
    given(fs.hasPermissionToRead(file)).willReturn(false);
    handleEvent();
    assertPermissionDeniedShown();
  }

  public void testShowsUnknownFileIfUnableToDetermineMediaType() {
    setFileWithMediaType(null);
    handleEvent();
    assertUnknownFileShown();
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = mock(FileSystem.class);
    starter = mock(ActivityStarter.class);
    toaster = mock(Toaster.class);
    medias = mock(MediaMap.class);
    file = mock(File.class);
    activity = mock(FileListActivity.class);
    given(activity.getPackageName()).willReturn("abc");
    handler = new FileClickHandler(activity, fs, medias, starter, toaster);
  }

  private void assertFileShown(String type) {
    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(starter).startActivity(eq(activity), arg.capture());

    Intent intent = arg.getValue();
    assertEquals(ACTION_VIEW, intent.getAction());
    assertEquals(type, intent.getType());
    assertEquals(fromFile(file), intent.getData());

    verifyZeroInteractions(toaster);
  }

  private void assertDirectoryShown() {
    verify(activity).show(file.getAbsolutePath());
    verifyZeroInteractions(toaster);
  }

  private void assertNoAppToOpenFileShown() {
    verify(starter).startActivity(eq(activity), any(Intent.class));
    verify(toaster).toast(activity, R.string.no_app_to_open_file, LENGTH_SHORT);
  }

  private void assertPermissionDeniedShown() {
    verify(toaster).toast(activity, R.string.permission_denied, LENGTH_SHORT);
    verifyZeroInteractions(starter);
  }

  private void assertUnknownFileShown() {
    verify(toaster).toast(activity, R.string.unknown_file_type, LENGTH_SHORT);
    verifyZeroInteractions(starter);
  }

  private void handleEvent() {
    handler.onFileSelected(file);
  }

  private ArgumentCaptor<Intent> intentCaptor() {
    return ArgumentCaptor.forClass(Intent.class);
  }

  private void setFileWithMediaType(String type) {
    given(file.getName()).willReturn("a.txt");
    given(file.isFile()).willReturn(true);
    given(medias.get(getFileExtension(file))).willReturn(type);
    given(fs.hasPermissionToRead(file)).willReturn(true);
  }

  private void setDirectory() {
    given(file.getAbsolutePath()).willReturn("/a");
    given(file.isDirectory()).willReturn(true);
    given(fs.hasPermissionToRead(file)).willReturn(true);
  }

}
