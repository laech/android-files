package com.example.files.app;

import static android.content.Intent.ACTION_VIEW;
import static android.net.Uri.fromFile;
import static android.widget.Toast.LENGTH_SHORT;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.File;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;

import com.example.files.R;
import com.example.files.content.ActivityStarter;
import com.example.files.media.MediaDetector;
import com.example.files.util.FileSystem;
import com.example.files.widget.Toaster;

public final class FileClickHandlerTest extends TestCase {

    private FileClickHandler mFileClickHandler;

    private ActivityStarter mActivityStarter;
    private FileSystem mFileSystem;
    private MediaDetector mMediaDetector;
    private Toaster mToaster;

    private FileListActivity mActivity;
    private File mFile;

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
                .when(mActivityStarter).startActivity(any(Context.class), any(Intent.class));

        handleEvent();

        assertNoAppToOpenFileShown();
    }

    public void testShowsPermissionDeniedIfNoPermissionToReadFile() {
        given(mFileSystem.hasPermissionToRead(mFile)).willReturn(false);
        handleEvent();
        assertPermissionDeniedShown();
    }

    public void testShowsUnknownFileIfUnableToDetermineMediaType() {
        setFileWithMediaType(null);
        handleEvent();
        assertUnknownFileShown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFileSystem = mock(FileSystem.class);
        mActivityStarter = mock(ActivityStarter.class);
        mToaster = mock(Toaster.class);
        mMediaDetector = mock(MediaDetector.class);
        mFile = mock(File.class);
        mActivity = mock(FileListActivity.class);
        given(mActivity.getPackageName()).willReturn("abc");
        mFileClickHandler = new FileClickHandler(
                mActivity, mFileSystem, mMediaDetector, mActivityStarter, mToaster);
    }

    private void assertFileShown(String type) {
        ArgumentCaptor<Intent> arg = intentCaptor();
        verify(mActivityStarter).startActivity(eq(mActivity), arg.capture());

        Intent intent = arg.getValue();
        assertEquals(ACTION_VIEW, intent.getAction());
        assertEquals(type, intent.getType());
        assertEquals(fromFile(mFile), intent.getData());

        verifyZeroInteractions(mToaster);
    }

    private void assertDirectoryShown() {
        verify(mActivity).show(mFile.getAbsolutePath());
        verifyZeroInteractions(mToaster);
    }

    private void assertNoAppToOpenFileShown() {
        verify(mActivityStarter).startActivity(eq(mActivity), any(Intent.class));
        verify(mToaster).toast(mActivity, R.string.no_app_to_open_file, LENGTH_SHORT);
    }

    private void assertPermissionDeniedShown() {
        verify(mToaster).toast(mActivity, R.string.permission_denied, LENGTH_SHORT);
        verifyZeroInteractions(mActivityStarter);
    }

    private void assertUnknownFileShown() {
        verify(mToaster).toast(mActivity, R.string.unknown_file_type, LENGTH_SHORT);
        verifyZeroInteractions(mActivityStarter);
    }

    private void handleEvent() {
        mFileClickHandler.onFileSelected(mFile);
    }

    private ArgumentCaptor<Intent> intentCaptor() {
        return ArgumentCaptor.forClass(Intent.class);
    }

    private void setFileWithMediaType(final String type) {
        given(mFile.getName()).willReturn("a.txt");
        given(mFile.isFile()).willReturn(true);
        given(mFileSystem.hasPermissionToRead(mFile)).willReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ((MediaDetector.Callback) invocation.getArguments()[1]).onResult(mFile, type);
                return null;
            }
        }).when(mMediaDetector).detect(eq(mFile), any(MediaDetector.Callback.class));
    }

    private void setDirectory() {
        given(mFile.getAbsolutePath()).willReturn("/a");
        given(mFile.isDirectory()).willReturn(true);
        given(mFileSystem.hasPermissionToRead(mFile)).willReturn(true);
    }

}
