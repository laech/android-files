package com.example.files.ui.events.handlers;

import static android.widget.Toast.LENGTH_SHORT;
import static com.example.files.ui.activities.FileListActivity.ARG_FOLDER;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.example.files.R;
import com.example.files.ui.ActivityStarter;
import com.example.files.ui.Toaster;
import com.example.files.ui.activities.FileListActivity;
import com.example.files.ui.events.FileClickEvent;
import com.example.files.util.FileSystem;

public final class FileClickEventHandlerTest extends TestCase {

  private FileClickEventHandler handler;

  private FileSystem fs;
  private ActivityStarter starter;
  private Toaster toaster;
  private Activity activity;
  private File file;

  public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
    given(fs.hasPermissionToRead(file)).willReturn(false);

    handler.handle(new FileClickEvent(activity, file));

    verify(toaster).toast(activity, R.string.permission_denied, LENGTH_SHORT);
    verifyZeroInteractions(starter);
  }

  public void testStartsActivityToShowFolderIfGotPermissionToReadFolder() {
    given(file.getAbsolutePath()).willReturn("/a");
    given(file.isDirectory()).willReturn(true);
    given(fs.hasPermissionToRead(file)).willReturn(true);

    handler.handle(new FileClickEvent(activity, file));

    ArgumentCaptor<Intent> arg = intentCaptor();
    verify(starter).startActivity(eq(activity), arg.capture());
    verifyIntent(arg.getValue());
    verifyZeroInteractions(toaster);
  }

  @Override protected void setUp() throws Exception {
    super.setUp();
    fs = mock(FileSystem.class);
    starter = mock(ActivityStarter.class);
    toaster = mock(Toaster.class);
    file = mock(File.class);
    activity = mock(Activity.class);
    given(activity.getPackageName()).willReturn("abc");
    handler = new FileClickEventHandler(fs, starter, toaster);
  }

  private ComponentName component(Context context, Class<?> clazz) {
    return new ComponentName(context, clazz);
  }

  private ArgumentCaptor<Intent> intentCaptor() {
    return ArgumentCaptor.forClass(Intent.class);
  }

  private void verifyIntent(Intent i) {
    assertEquals(file.getAbsolutePath(), i.getStringExtra(ARG_FOLDER));
    assertEquals(component(activity, FileListActivity.class), i.getComponent());
  }
}
