package l.files.event;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.Intent;
import com.squareup.otto.Subscribe;
import java.lang.reflect.Method;
import l.files.event.internal.FileService;
import l.files.test.BaseTest;
import l.files.test.TempDir;
import org.mockito.ArgumentCaptor;

public final class IoProviderTest extends BaseTest {

  private TempDir dir;
  private Application context;
  private IoProvider provider;

  @Override protected void setUp() throws Exception {
    super.setUp();
    dir = TempDir.create();
    context = mock(Application.class);
    provider = new IoProvider(context);
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  public void testDeleteRequestHandlerIsAnnotated() throws Exception {
    Method method = IoProvider.class.getMethod("handle", DeleteRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testPasteRequestCutHandlerIsAnnotated() throws Exception {
    Method method = IoProvider.class.getMethod("handle", PasteRequest.Cut.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testPasteRequestCopyHandlerIsAnnotated() throws Exception {
    Method method = IoProvider.class.getMethod("handle", PasteRequest.Copy.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testDeleteRequestIsHandled() {
    provider.handle(new DeleteRequest(dir.newFile()));
    Intent intent = captureIntent();
    assertEquals(FileService.Delete.class.getName(), intent.getComponent().getClassName());
  }

  public void testPasteRequestCutIsHandled() {
    provider.handle(new PasteRequest.Cut(newHashSet(dir.newFile()), dir.get()));
    Intent intent = captureIntent();
    assertEquals(FileService.Cut.class.getName(), intent.getComponent().getClassName());
  }

  public void testPasteRequestCopyIsHandled() {
    provider.handle(new PasteRequest.Copy(newHashSet(dir.newFile()), dir.get()));
    Intent intent = captureIntent();
    assertEquals(FileService.Copy.class.getName(), intent.getComponent().getClassName());
  }

  private Intent captureIntent() {
    ArgumentCaptor<Intent> arg = ArgumentCaptor.forClass(Intent.class);
    verify(context).startService(arg.capture());
    return arg.getValue();
  }
}
