package com.example.files.media;

import com.example.files.R;
import com.example.files.test.TempDirectory;
import junit.framework.TestCase;

import java.io.File;

import static com.example.files.test.TempDirectory.newTempDirectory;
import static com.example.files.util.FileSystem.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class ImageMapTest extends TestCase {

    private TempDirectory mDirectory;
    private ImageMap mImageMap;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mImageMap = new ImageMap();
        mDirectory = newTempDirectory();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mDirectory.delete();
    }

    public void testGetsImageForFile() {
        assertEquals(R.drawable.ic_file, mImageMap.get(mDirectory.newFile()));
    }

    public void testGetsImageForPdfFile() {
        assertEquals(R.drawable.ic_file_pdf, mImageMap.get(mDirectory.newFile("a.pdf")));
    }

    public void testGetsImageForDirectory() {
        assertDirectoryImage(R.drawable.ic_directory, mDirectory.get());
    }

    public void testGetsImageForAlarmsDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_alarms, DIRECTORY_ALARMS);
    }

    public void testGetsImageForAndroidDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_android, DIRECTORY_ANDROID);
    }

    public void testGetsImageForDcimDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_dcim, DIRECTORY_DCIM);
    }

    public void testGetsImageForDownloadDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_download, DIRECTORY_DOWNLOADS);
    }

    public void testGetsImageForMoviesDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_movies, DIRECTORY_MOVIES);
    }

    public void testGetsImageForMusicDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_music, DIRECTORY_MUSIC);
    }

    public void testGetsImageForNotificationsDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_notifications, DIRECTORY_NOTIFICATIONS);
    }

    public void testGetsImageForPicturesDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_pictures, DIRECTORY_PICTURES);
    }

    public void testGetsImageForPodcastsDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_podcasts, DIRECTORY_PODCASTS);
    }

    public void testGetsImageForRingtonesDirectory() {
        assertDirectoryImage(R.drawable.ic_directory_ringtones, DIRECTORY_RINGTONES);
    }

    public void testGetsImageIconFromExtension() {
        assertEquals(R.drawable.ic_image, mImageMap.get(createFile("jpg")));
    }

    public void testGetsImageIconFromExtensionIgnoringCase() {
        assertEquals(R.drawable.ic_image, mImageMap.get(createFile("jPg")));
    }

    private void assertDirectoryImage(int resId, File dir) {
        assertTrue(dir.mkdirs() || dir.isDirectory());
        assertEquals(resId, mImageMap.get(dir));
    }

    private File createFile(String ext) {
        File file = mock(File.class);
        given(file.getName()).willReturn("a." + ext);
        return file;
    }
}
