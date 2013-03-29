package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.app.FilesActivity.EXTRA_DIRECTORY;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.files.R;
import com.example.files.event.FileSelectedEvent;
import com.example.files.event.MediaDetectedEvent;
import com.example.files.media.MediaDetector;
import com.example.files.media.MediaMap;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

public final class FilesActivityHelperTest extends TestCase {

  private FilesActivityHelper helper;

  private FileSystem fileSystem;
  private MediaMap mediaMap;
  private MediaDetector mediaDetector;
  private Toaster toaster;
  private FilesActivity activity;
  private File file;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fileSystem = mock(FileSystem.class);
    toaster = mock(Toaster.class);
    mediaMap = mock(MediaMap.class);
    mediaDetector = mock(MediaDetector.class);
    file = mock(File.class);
    activity = mock(FilesActivity.class);
    given(activity.getPackageName()).willReturn("abc");
    helper = new FilesActivityHelper(
        fileSystem, mediaMap, mediaDetector, toaster);
  }

  public void testCallsMediaMapToGetFileMediaType() {
    given(file.isFile()).willReturn(true);
    given(file.getName()).willReturn("a.txt");
    given(mediaMap.get("txt")).willReturn("text/plain");
    given(fileSystem.hasPermissionToRead(file)).willReturn(true);

    handleFileSelectedEvent();

    verify(mediaMap).get("txt");
    verifyZeroInteractions(mediaDetector);
    assertFileShown("text/plain");
  }

  public void testCallsMediaDetectorToDetectFileMediaTypeIfMediaMapFails() {
    given(file.isFile()).willReturn(true);
    given(file.getName()).willReturn("a.txt");
    given(mediaMap.get("txt")).willReturn(null);
    given(fileSystem.hasPermissionToRead(file)).willReturn(true);

    handleFileSelectedEvent();

    verify(mediaDetector).detect(file);
    verifyZeroInteractions(toaster);
    verifyZeroInteractions(activity);
  }

  public void testShowsFileIfMediaTypeIsNotNull() {
    String type = "text/plain";
    handleMediaDetectedEvent(type);
    assertFileShown(type);
  }

  public void testShowsDirectoryIfGotPermissionToReadDirectory() {
    setDirectory();
    handleFileSelectedEvent();
    assertDirectoryShown();
  }

  public void testShowsNoAppFoundIfNoAppCanOpenFileWithMediaType() {
    doThrow(new ActivityNotFoundException())
        .when(activity).startActivity(any(Intent.class));
    handleMediaDetectedEvent("text/plain");
    assertNoAppToOpenFileShown();
  }

  public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
    given(fileSystem.hasPermissionToRead(file)).willReturn(false);
    handleFileSelectedEvent();
    assertPermissionDeniedShown();
  }

  public void testShowsUnknownFileIfUnableToDetermineMediaType() {
    handleMediaDetectedEvent(null);
    assertUnknownFileShown();
  }

  private void assertFileShown(String type) {
    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(activity).startActivity(arg.capture());

    Intent intent = arg.getValue();
    assertEquals(ACTION_VIEW, intent.getAction());
    assertEquals(type, intent.getType());
    assertEquals(fromFile(file), intent.getData());

    verifyZeroInteractions(toaster);
    verifyZeroInteractions(mediaDetector);
  }

  private void assertDirectoryShown() {
    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(activity).startActivityForResult(arg.capture(), eq(0));

    Intent i = arg.getValue();
    assertEquals(file.getAbsolutePath(), i.getStringExtra(EXTRA_DIRECTORY));
    assertEquals(component(activity, FilesActivity.class), i.getComponent());

    verifyZeroInteractions(toaster);
    verifyZeroInteractions(mediaDetector);
  }

  private void assertNoAppToOpenFileShown() {
    verify(activity).startActivity(any(Intent.class));
    verify(toaster).toast(activity, R.string.no_app_to_open_file, LENGTH_SHORT);
    verifyZeroInteractions(mediaDetector);
  }

  private void assertPermissionDeniedShown() {
    verify(toaster).toast(activity, R.string.permission_denied, LENGTH_SHORT);
    verifyZeroInteractions(activity);
    verifyZeroInteractions(mediaDetector);
  }

  private void assertUnknownFileShown() {
    verify(toaster).toast(activity, R.string.unknown_file_type, LENGTH_SHORT);
    verifyZeroInteractions(activity);
    verifyZeroInteractions(mediaDetector);
  }

  private ComponentName component(Context context, Class<?> clazz) {
    return new ComponentName(context, clazz);
  }

  private void handleFileSelectedEvent() {
    helper.handle(new FileSelectedEvent(file), activity);
  }

  private void handleMediaDetectedEvent(String mediaType) {
    helper.handle(new MediaDetectedEvent(file, mediaType), activity);
  }

  private ArgumentCaptor<Intent> intentCaptor() {
    return ArgumentCaptor.forClass(Intent.class);
  }

  private void setDirectory() {
    given(file.getAbsolutePath()).willReturn("/a");
    given(file.isDirectory()).willReturn(true);
    given(fileSystem.hasPermissionToRead(file)).willReturn(true);
  }
}
