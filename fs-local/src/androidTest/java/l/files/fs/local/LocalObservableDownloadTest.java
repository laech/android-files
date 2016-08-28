package l.files.fs.local;

import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.app.DownloadManager.Request;
import android.database.Cursor;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.Callable;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.local.LocalObservableTest.Recorder;
import l.files.testing.Executable;
import l.files.testing.Tests;

import static android.app.DownloadManager.COLUMN_REASON;
import static android.app.DownloadManager.COLUMN_STATUS;
import static android.app.DownloadManager.STATUS_SUCCESSFUL;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Files.deleteIfExists;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.LocalObservableTest.Recorder.observe;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assume.assumeThat;

@RunWith(AndroidJUnit4.class)
public final class LocalObservableDownloadTest extends PathBaseTest {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * TODO
     *
     * New bug affecting Android M (API 23) inotify, meaning some events will
     * not be delivered.
     *
     * Examples:
     *
     *  - File download via DownloadManager
     *  - 'touch file' using adb shell
     *
     * Issues:
     *
     *  - https://code.google.com/p/android/issues/detail?id=189231
     *  - https://code.google.com/p/android-developer-preview/issues/detail?id=3099
     */
    @Test
    public void notifies_files_downloaded_by_download_manager() throws Exception {

        assumeThat(
                "Skipping test on API 23 due to Android bug: " +
                        "https://code.google.com/p/android/issues/detail?id=189231",
                SDK_INT,
                not(M)
        );

        Path downloadDir = downloadsDir();
        Path downloadFile = downloadDir.resolve(
                "test_notifies_files_downloaded_by_download_manager-" +
                        currentTimeMillis());
        try {
            Recorder observer = observe(downloadDir);
            try {
                observer.await(CREATE, downloadFile, newDownload(downloadFile));
            } finally {
                observer.close();
            }
        } finally {
            deleteIfExists(downloadFile);
        }
    }

    private Callable<Void> newDownload(final Path dst) {
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                download(dst);
                return null;
            }
        };
    }

    private void download(Path saveTo) throws Exception {
        assertFalse(Files.exists(saveTo, NOFOLLOW));

        Uri src = Uri.parse("https://www.google.com");
        Uri dst = Uri.parse(saveTo.toUri().toString());
        Request request = new Request(src).setDestinationUri(dst);
        long id = downloadManager().enqueue(request);

        awaitSuccessfulDownload(id, saveTo);
    }

    private void awaitSuccessfulDownload(
            final long id,
            final Path dst
    ) throws Exception {

        Tests.timeout(60, SECONDS, new Executable() {
            @Override
            public void execute() throws Exception {
                assertSuccessfulDownload(id, dst);
            }
        });
    }

    private void assertSuccessfulDownload(
            final long id,
            final Path dst
    ) throws IOException {

        Query query = new Query().setFilterById(id);
        Cursor cursor = downloadManager().query(query);
        try {
            assertTrue(cursor.moveToFirst());
            assertEquals(
                    cursor.getString(cursor.getColumnIndex(COLUMN_REASON)),
                    STATUS_SUCCESSFUL,
                    cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS))
            );
        } finally {
            cursor.close();
        }
        assertTrue(Files.exists(dst, NOFOLLOW));
    }

    private DownloadManager downloadManager() {
        return (DownloadManager) InstrumentationRegistry.getContext()
                .getSystemService(DOWNLOAD_SERVICE);
    }

    private Path downloadsDir() {
        return Paths.get(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS));
    }

}
