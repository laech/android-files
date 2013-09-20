package l.files.app;

import static l.files.test.TestFileListContainerFragmentActivity.DIRECTORY;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import com.squareup.otto.Subscribe;
import java.io.File;
import java.lang.reflect.Method;
import l.files.R;
import l.files.common.base.Consumer;
import l.files.common.widget.Toaster;
import l.files.test.BaseActivityTest;
import l.files.test.TempDir;
import l.files.test.TestFileListContainerFragmentActivity;
import org.mockito.ArgumentCaptor;

public final class FileListContainerFragmentTest
    extends BaseActivityTest<TestFileListContainerFragmentActivity> {

  private File file;
  private TempDir dir;

  public FileListContainerFragmentTest() {
    super(TestFileListContainerFragmentActivity.class);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = mock(File.class);
    dir = TempDir.create();
    setActivityIntent(new Intent().putExtra(DIRECTORY, dir.get().getAbsolutePath()));
  }

  @Override protected void tearDown() throws Exception {
    dir.delete();
    super.tearDown();
  }

  @SuppressWarnings("unchecked")
  public void testOpenFileRequestIsHandled() {
    given(file.isFile()).willReturn(true);
    given(file.canRead()).willReturn(true);
    fragment().fileOpener = mock(Consumer.class);
    fragment().handle(new OpenFileRequest(file));
    verify(fragment().fileOpener).apply(file);
  }

  public void testOpenFileRequestHandlerMethodIsAnnotated() throws Exception {
    Method method = FileListContainerFragment.class.getMethod("handle", OpenFileRequest.class);
    assertNotNull(method.getAnnotation(Subscribe.class));
  }

  public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
    given(file.canRead()).willReturn(false);
    fragment().toaster = mock(Toaster.class);
    fragment().handle(new OpenFileRequest(file));
    verify(fragment().toaster).toast(activity(), R.string.permission_denied);
  }

  public void testShowsDirIfGotPermissionToReadDirectory() {
    ArgumentCaptor<FilesFragment> captor = ArgumentCaptor.forClass(FilesFragment.class);
    FragmentTransaction transaction = mockFragmentTransaction();
    fragment().manager = mockFragmentManager(transaction);
    given(file.isDirectory()).willReturn(true);
    given(file.canRead()).willReturn(true);

    fragment().handle(new OpenFileRequest(file));

    verify(fragment().manager).beginTransaction();
    verify(transaction).replace(eq(android.R.id.content), captor.capture(), eq(FilesFragment.TAG));
    verify(transaction).addToBackStack(null);
    verify(transaction).commit();
    assertEquals(file.getAbsolutePath(),
        captor
            .getValue()
            .getArguments()
            .getString(FilesFragment.ARG_DIRECTORY));
  }

  public void testPopsBackStackOnHomePressed() {
    fragment().manager = mock(FragmentManager.class);
    fragment().handle(OnHomePressedEvent.INSTANCE);
    verify(fragment().manager).popBackStack();
  }

  private FragmentTransaction mockFragmentTransaction() {
    FragmentTransaction transaction = mock(FragmentTransaction.class);
    given(transaction.replace(anyInt(), any(Fragment.class), anyString())).willReturn(transaction);
    given(transaction.setTransition(anyInt())).willReturn(transaction);
    given(transaction.addToBackStack(anyString())).willReturn(transaction);
    return transaction;
  }

  private FragmentManager mockFragmentManager(FragmentTransaction transaction) {
    FragmentManager manager = mock(FragmentManager.class);
    given(manager.beginTransaction()).willReturn(transaction);
    return manager;
  }

  private FileListContainerFragment fragment() {
    return activity().fragment();
  }
}
