package l.files.fs;

import android.app.DownloadManager;
import android.app.DownloadManager.*;
import android.database.Cursor;
import android.net.Uri;
import androidx.test.InstrumentationRegistry;
import l.files.testing.Tests;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;
import org.junit.Test;

import static android.app.DownloadManager.*;
import static android.content.Context.DOWNLOAD_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Environment.DIRECTORY_DOWNLOADS;
import static android.os.Environment.getExternalStoragePublicDirectory;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.event.Event.CREATE;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public final class ObservableDownloadTest extends PathBaseTest {

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
     *  - https://code.google.com/p/android-developer-preview/issues/detail ?id=3099
     */
    @Test
    public void notifies_files_downloaded_by_download_manager()
        throws Exception {

        assumeTrue(
            "Skipping test on API 23 (Android M) due to Android bug: " +
                "https://code.google.com/p/android/issues/detail?id=189231",
            SDK_INT != M
        );

        assumeTrue(
            "Skipping test on API 24 (Android N) due to no permission " +
                "to observe on download directory.",
            SDK_INT < N
        );

        Path downloadDir = downloadsDir();
        Path downloadFile = downloadDir.concat(
            "test_notifies_files_downloaded_by_download_manager-" +
                currentTimeMillis());
        try (ObservableTest.Recorder observer =
                 ObservableTest.Recorder.observe(downloadDir)) {
            observer.await(CREATE, downloadFile, () -> download(downloadFile));
        } finally {
            Paths.deleteIfExists(downloadFile);
        }
    }

    private void download(Path saveTo) throws Exception {
        assertFalse(saveTo.exists(NOFOLLOW_LINKS));

        Uri src = Uri.parse("https://www.google.com");
        Uri dst = saveTo.toUri();
        Request request = new Request(src).setDestinationUri(dst);
        long id = downloadManager().enqueue(request);

        awaitSuccessfulDownload(id, saveTo);
    }

    private void awaitSuccessfulDownload(
        long id,
        Path dst
    ) throws Exception {
        Tests.timeout(60, SECONDS, () -> assertSuccessfulDownload(id, dst));
    }

    private void assertSuccessfulDownload(
        long id,
        Path dst
    ) {
        Query query = new Query().setFilterById(id);
        try (Cursor cursor = downloadManager().query(query)) {
            assertTrue(cursor.moveToFirst());
            assertEquals(
                cursor.getString(cursor.getColumnIndex(COLUMN_REASON)),
                STATUS_SUCCESSFUL,
                cursor.getInt(cursor.getColumnIndex(COLUMN_STATUS))
            );
        }
        assertTrue(dst.exists(NOFOLLOW_LINKS));
    }

    private DownloadManager downloadManager() {
        return (DownloadManager) InstrumentationRegistry.getContext()
            .getSystemService(DOWNLOAD_SERVICE);
    }

    private Path downloadsDir() {
        return Path.of(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS));
    }

}
