package l.files.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.google.common.net.MediaType;
import junit.framework.TestCase;
import l.files.R;
import l.files.common.io.Detector;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;
import l.files.event.OpenFileRequest;
import org.mockito.ArgumentCaptor;

import java.io.File;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static l.files.app.FilesActivity.EXTRA_DIR;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public final class OpenFileRequestConsumerTest extends TestCase {

  private Context context;
  private Detector detector;
  private Toaster toaster;
  private File file;

  private OpenFileRequestConsumer helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = mock(File.class);
    context = mock(Context.class);
    toaster = mock(Toaster.class);
    detector = mock(Detector.class);
    helper = new OpenFileRequestConsumer(context, detector, toaster, new ShowFileTaskExecutor());
  }

  public void testDetectsFileContentMediaType() {
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.take(new OpenFileRequest(file));
    assertFileShown(PLAIN_TEXT_UTF_8);
  }

  public void testShowsFileIfMediaTypeIsNotNull() {
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.take(new OpenFileRequest(file));
    assertFileShown(PLAIN_TEXT_UTF_8);
  }

  public void testShowsDirIfGotPermissionToReadDirectory() {
    given(file.isDirectory()).willReturn(true);
    given(file.canRead()).willReturn(true);
    helper.take(new OpenFileRequest(file));
    assertDirShown();
  }

  public void testShowsNoAppFoundIfNoAppCanOpenFileWithMediaType() {
    doThrow(new ActivityNotFoundException()).when(context).startActivity(any(Intent.class));
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.take(new OpenFileRequest(file));
    assertNoAppToOpenFileShown();
  }

  public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
    given(file.canRead()).willReturn(false);
    helper.take(new OpenFileRequest(file));
    assertPermissionDeniedShown();
  }

  private void assertFileShown(MediaType type) {
    verifyZeroInteractions(toaster);
    verify(detector).detect(file);

    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(context).startActivity(arg.capture());

    Intent intent = arg.getValue();
    assertEquals(ACTION_VIEW, intent.getAction());
    assertEquals(type.toString(), intent.getType());
    assertEquals(Uri.fromFile(file), intent.getData());
  }

  private void assertDirShown() {
    verifyZeroInteractions(toaster);
    verifyZeroInteractions(detector);

    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(context).startActivity(arg.capture());

    Intent intent = arg.getValue();
    assertEquals(file.getAbsolutePath(), intent.getStringExtra(EXTRA_DIR));
    assertEquals(FilesActivity.class.getName(), intent.getComponent().getClassName());
  }

  private void assertNoAppToOpenFileShown() {
    verify(detector).detect(file);
    verify(context).startActivity(any(Intent.class));
    verify(toaster).toast(context, R.string.no_app_to_open_file);
  }

  private void assertPermissionDeniedShown() {
    verify(toaster).toast(context, R.string.permission_denied);
    verifyZeroInteractions(context);
    verifyZeroInteractions(detector);
  }

  private void setFileMediaType(MediaType type) {
    given(file.isFile()).willReturn(true);
    given(file.canRead()).willReturn(true);
    given(detector.detect(file)).willReturn(type);
  }

  private ArgumentCaptor<Intent> intentCaptor() {
    return ArgumentCaptor.forClass(Intent.class);
  }

  private static class ShowFileTaskExecutor implements AsyncTaskExecutor {
    @Override
    public <Params> void execute(AsyncTask<Params, ?, ?> task, Params... params) {
      OpenFileRequestConsumer.ShowFileTask showFile = (OpenFileRequestConsumer.ShowFileTask) task;
      MediaType type = showFile.doInBackground();
      showFile.onPostExecute(type);
    }
  }
}
