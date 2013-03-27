package com.example.files.media;

import com.example.files.R;
import com.example.files.test.TempDirectory;
import junit.framework.TestCase;

import java.io.File;

import static com.example.files.test.TempDirectory.newTempDirectory;
import static com.example.files.util.FileSystem.DIRECTORY_ALARMS;
import static com.example.files.util.FileSystem.DIRECTORY_ANDROID;
import static com.example.files.util.FileSystem.DIRECTORY_DCIM;
import static com.example.files.util.FileSystem.DIRECTORY_DOWNLOADS;
import static com.example.files.util.FileSystem.DIRECTORY_MOVIES;
import static com.example.files.util.FileSystem.DIRECTORY_MUSIC;
import static com.example.files.util.FileSystem.DIRECTORY_NOTIFICATIONS;
import static com.example.files.util.FileSystem.DIRECTORY_PICTURES;
import static com.example.files.util.FileSystem.DIRECTORY_PODCASTS;
import static com.example.files.util.FileSystem.DIRECTORY_RINGTONES;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class ImageMapTest extends TestCase {

  private TempDirectory directory;
  private ImageMap images;

  @Override protected void setUp() throws Exception {
    super.setUp();
    images = new ImageMap();
    directory = newTempDirectory();
  }

  @Override protected void tearDown() throws Exception {
    directory.delete();
    super.tearDown();
  }

  public void testGetsImageForDirectory() {
    assertDirectoryImage(
        R.drawable.ic_directory,
        directory.get());
  }

  public void testGetsImageForDirectoryAlarms() {
    assertDirectoryImage(
        R.drawable.ic_directory_alarms,
        DIRECTORY_ALARMS);
  }

  public void testGetsImageForDirectoryAndroid() {
    assertDirectoryImage(
        R.drawable.ic_directory_android,
        DIRECTORY_ANDROID);
  }

  public void testGetsImageForDirectoryDcim() {
    assertDirectoryImage(
        R.drawable.ic_directory_dcim,
        DIRECTORY_DCIM);
  }

  public void testGetsImageForDirectoryDownload() {
    assertDirectoryImage(
        R.drawable.ic_directory_download,
        DIRECTORY_DOWNLOADS);
  }

  public void testGetsImageForDirectoryMovies() {
    assertDirectoryImage(
        R.drawable.ic_directory_movies,
        DIRECTORY_MOVIES);
  }

  public void testGetsImageForDirectoryMusic() {
    assertDirectoryImage(
        R.drawable.ic_directory_music,
        DIRECTORY_MUSIC);
  }

  public void testGetsImageForDirectoryNotifications() {
    assertDirectoryImage(
        R.drawable.ic_directory_notifications,
        DIRECTORY_NOTIFICATIONS);
  }

  public void testGetsImageForDirectoryPictures() {
    assertDirectoryImage(
        R.drawable.ic_directory_pictures,
        DIRECTORY_PICTURES);
  }

  public void testGetsImageForDirectoryPodcasts() {
    assertDirectoryImage(
        R.drawable.ic_directory_podcasts,
        DIRECTORY_PODCASTS);
  }

  public void testGetsImageForDirectoryRingtones() {
    assertDirectoryImage(
        R.drawable.ic_directory_ringtones,
        DIRECTORY_RINGTONES);
  }

  public void testGetsImageForFile() {
    assertEquals(
        R.drawable.ic_file,
        images.get(directory.newFile()));
  }

  public void testGetsImageForFilePdf() {
    assertEquals(
        R.drawable.ic_file_pdf,
        images.get(directory.newFile("a.pdf")));
  }

  public void testGetsImageForFileImage() {
    assertEquals(
        R.drawable.ic_file_image,
        images.get(createFile("jpg")));
  }

  public void testGetsImageForFileAudio() {
    assertEquals(
        R.drawable.ic_file_audio,
        images.get(createFile("mp3")));
  }

  public void testGetsImageForFileVideo() {
    assertEquals(
        R.drawable.ic_file_video,
        images.get(createFile("mp4")));
  }

  public void testGetsImageForFileArchive() {
    assertEquals(
        R.drawable.ic_file_archive,
        images.get(createFile("zip")));
  }

  public void testGetsImageForFileText() {
    assertEquals(
        R.drawable.ic_file_text,
        images.get(createFile("txt")));
  }

  public void testGetsImageIconFromExtensionIgnoringCase() {
    assertEquals(R.drawable.ic_file_image, images.get(createFile("jPg")));
  }

  private void assertDirectoryImage(int resId, File dir) {
    assertTrue(dir.mkdirs() || dir.isDirectory());
    assertEquals(resId, images.get(dir));
  }

  private File createFile(String ext) {
    File file = mock(File.class);
    given(file.getName()).willReturn("a." + ext);
    return file;
  }
}
