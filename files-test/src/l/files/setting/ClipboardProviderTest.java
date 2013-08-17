package l.files.setting;

import static android.content.ClipData.newIntent;
import static android.content.ClipData.newPlainText;
import static android.content.ClipDescription.MIMETYPE_TEXT_INTENT;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.common.io.Files.toAbsolutePaths;
import static l.files.setting.ClipboardProvider.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Intent;

import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;

public final class ClipboardProviderTest extends TestCase {

  private ClipboardManager manager;
  private ClipboardProvider provider;

  @Override protected void setUp() throws Exception {
    super.setUp();
    manager = mock(ClipboardManager.class);
    provider = new ClipboardProvider(manager);
  }

  public void testProducerIsAnnotated() throws Exception {
    Method method = ClipboardProvider.class.getMethod("get");
    assertNotNull(method.getAnnotation(Produce.class));
  }

  public void testCutRequestHandlerIsAnnotated() throws Exception {
    Method method = ClipboardProvider.class.getMethod("handle", CutRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testCopyRequestHandlerIsAnnotated() throws Exception {
    Method method = ClipboardProvider.class.getMethod("handle", CopyRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testCutRequestIsHandled() {
    provider.handle(new CutRequest(new File("/")));

    ClipData clip = captureEvent();
    assertDescription(clip.getDescription());
    assertIntent(clip.getItemAt(0).getIntent(), ACTION_CUT, new File("/"));
    assertEquals(1, clip.getItemCount());
  }

  public void testCopyRequestIsHandled() {
    provider.handle(new CopyRequest(new File("/")));

    ClipData clip = captureEvent();
    assertDescription(clip.getDescription());
    assertIntent(clip.getItemAt(0).getIntent(), ACTION_COPY, new File("/"));
    assertEquals(1, clip.getItemCount());
  }

  public void testGetsCurrentClip() {
    String[] paths = toAbsolutePaths(new File("/"));
    given(manager.getPrimaryClip()).willReturn(newIntent(null,
        new Intent(ACTION_CUT).putExtra(EXTRA_FILES, paths)));

    Clipboard clip = provider.get();
    assertTrue(clip.isCut());
    assertEquals(newHashSet(new File("/")), clip.value());
  }

  public void testGetReturnsNullIfNoFileClip() {
    given(manager.getPrimaryClip()).willReturn(newPlainText(null, "blah"));
    assertNull(provider.get());
  }

  public void testGetReturnsNullIfNoClip() {
    given(manager.getPrimaryClip()).willReturn(null);
    assertNull(provider.get());
  }

  private static void assertIntent(Intent intent, String action, File... files) {
    assertEquals(action, intent.getAction());
    assertTrue(Arrays.equals(toAbsolutePaths(files), intent.getStringArrayExtra(EXTRA_FILES)));
  }

  private static void assertDescription(ClipDescription description) {
    assertEquals(1, description.getMimeTypeCount());
    assertEquals(MIMETYPE_TEXT_INTENT, description.getMimeType(0));
  }

  private ClipData captureEvent() {
    ArgumentCaptor<ClipData> arg = ArgumentCaptor.forClass(ClipData.class);
    verify(manager).setPrimaryClip(arg.capture());
    return arg.getValue();
  }
}
