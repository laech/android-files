package l.files.app;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import com.google.common.net.MediaType;
import java.io.File;
import l.files.R;
import l.files.common.io.Detector;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;
import l.files.test.BaseTest;
import org.mockito.ArgumentCaptor;

public final class FileOpenerTest extends BaseTest {

  private Context context;
  private Detector detector;
  private Toaster toaster;
  private File file;

  private FileOpener helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = mock(File.class);
    context = mock(Context.class);
    toaster = mock(Toaster.class);
    detector = mock(Detector.class);
    helper = new FileOpener(context, detector, toaster, new ShowFileTaskExecutor());
  }

  public void testDetectsFileContentMediaType() {
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.apply(file);
    assertFileShown(PLAIN_TEXT_UTF_8);
  }

  public void testShowsFileIfMediaTypeIsNotNull() {
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.apply(file);
    assertFileShown(PLAIN_TEXT_UTF_8);
  }

  public void testShowsNoAppFoundIfNoAppCanOpenFileWithMediaType() {
    doThrow(new ActivityNotFoundException()).when(context).startActivity(any(Intent.class));
    setFileMediaType(PLAIN_TEXT_UTF_8);
    helper.apply(file);
    assertNoAppToOpenFileShown();
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

  private void assertNoAppToOpenFileShown() {
    verify(detector).detect(file);
    verify(context).startActivity(any(Intent.class));
    verify(toaster).toast(context, R.string.no_app_to_open_file);
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
      FileOpener.ShowFileTask showFile = (FileOpener.ShowFileTask) task;
      MediaType type = showFile.doInBackground();
      showFile.onPostExecute(type);
    }
  }
}
