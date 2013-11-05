package l.files.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.io.InputStream;

import l.files.R;
import l.files.common.io.Detector;
import l.files.common.os.AsyncTaskExecutor;
import l.files.common.widget.Toaster;
import l.files.test.BaseTest;

import static android.content.Intent.ACTION_VIEW;
import static com.google.common.net.MediaType.PLAIN_TEXT_UTF_8;
import static java.io.File.createTempFile;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public final class FileOpenerTest extends BaseTest {

  private Context context;
  private Detector detector;
  private Toaster toaster;
  private String fileUri;

  private FileOpener helper;

  @Override protected void setUp() throws Exception {
    super.setUp();
    fileUri = createTempFile("abc", "124").toURI().toString();
    context = mock(Context.class);
    toaster = mock(Toaster.class);
    detector = mock(Detector.class);
    helper = new FileOpener(context, detector, toaster,
        new ShowFileTaskExecutor());
  }

  public void testDetectsFileContentMediaType() throws Exception {
    setFileMediaType(PLAIN_TEXT_UTF_8.toString());
    helper.apply(fileUri);
    assertFileShown(PLAIN_TEXT_UTF_8.toString());
  }

  public void testShowsFileIfMediaTypeIsNotNull() throws Exception {
    setFileMediaType(PLAIN_TEXT_UTF_8.toString());
    helper.apply(fileUri);
    assertFileShown(PLAIN_TEXT_UTF_8.toString());
  }

  public void testShowsNoAppFoundIfNoAppCanOpenFileWithMediaType()
      throws Exception {
    doThrow(new ActivityNotFoundException())
        .when(context).startActivity(any(Intent.class));
    setFileMediaType(PLAIN_TEXT_UTF_8.toString());
    helper.apply(fileUri);
    assertNoAppToOpenFileShown();
  }

  private void assertFileShown(String media) throws IOException {
    verifyZeroInteractions(toaster);
    verify(detector).detect(any(InputStream.class));

    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(context).startActivity(arg.capture());

    Intent intent = arg.getValue();
    assertEquals(ACTION_VIEW, intent.getAction());
    assertEquals(media, intent.getType());
    assertEquals(Uri.parse(fileUri), intent.getData());
  }

  private void assertNoAppToOpenFileShown() throws Exception {
    verify(detector).detect(any(InputStream.class));
    verify(context).startActivity(any(Intent.class));
    verify(toaster).toast(context, R.string.no_app_to_open_file);
  }

  private void setFileMediaType(String media) throws Exception {
    given(detector.detect(any(InputStream.class))).willReturn(media);
  }

  private ArgumentCaptor<Intent> intentCaptor() {
    return ArgumentCaptor.forClass(Intent.class);
  }

  private static class ShowFileTaskExecutor implements AsyncTaskExecutor {
    @Override
    @SafeVarargs
    public final <Params> void execute(AsyncTask<Params, ?, ?> task,
                                       Params... params) {
      FileOpener.ShowFileTask showFile = (FileOpener.ShowFileTask) task;
      String type = showFile.doInBackground();
      showFile.onPostExecute(type);
    }
  }
}
