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

  private TempDirectory directory;
  private ImageMap images;

  @Override protected void setUp() throws Exception {
    super.setUp();
    images = new ImageMap();
    directory = newTempDirectory();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    directory.delete();
  }

  public void testGetsImageForFile() {
    assertEquals(R.drawable.ic_file, images.get(directory.newFile()));
  }

  public void testGetsImageForPdfFile() {
    assertEquals(R.drawable.ic_file_pdf, images.get(directory.newFile("a.pdf")));
  }

  public void testGetsImageForDirectory() {
    assertEquals(R.drawable.ic_directory, images.get(directory.get()));
  }

  public void testGetsImageForAlarmsDirectory() {
    assertEquals(R.drawable.ic_directory_alarms, images.get(DIRECTORY_ALARMS));
  }

  public void testGetsImageForAndroidDirectory() {
    assertEquals(R.drawable.ic_directory_android, images.get(DIRECTORY_ANDROID));
  }

  public void testGetsImageForDcimDirectory() {
    assertEquals(R.drawable.ic_directory_dcim, images.get(DIRECTORY_DCIM));
  }

  public void testGetsImageForDownloadDirectory() {
    assertEquals(R.drawable.ic_directory_download, images.get(DIRECTORY_DOWNLOADS));
  }

  public void testGetsImageForMoviesDirectory() {
    assertEquals(R.drawable.ic_directory_movies, images.get(DIRECTORY_MOVIES));
  }

  public void testGetsImageForMusicDirectory() {
    assertEquals(R.drawable.ic_directory_music, images.get(DIRECTORY_MUSIC));
  }

  public void testGetsImageForNotificationsDirectory() {
    assertEquals(R.drawable.ic_directory_notifications, images.get(DIRECTORY_NOTIFICATIONS));
  }

  public void testGetsImageForPicturesDirectory() {
    assertEquals(R.drawable.ic_directory_pictures, images.get(DIRECTORY_PICTURES));
  }

  public void testGetsImageForPodcastsDirectory() {
    assertEquals(R.drawable.ic_directory_podcasts, images.get(DIRECTORY_PODCASTS));
  }

  public void testGetsImageForRingtonesDirectory() {
    assertEquals(R.drawable.ic_directory_ringtones, images.get(DIRECTORY_RINGTONES));
  }

  public void testGetsImageIconFromExtension() {
    assertEquals(R.drawable.ic_image, images.get(createFile("jpg")));
  }

  public void testGetsImageIconFromExtensionIgnoringCase() {
    assertEquals(R.drawable.ic_image, images.get(createFile("jPg")));
  }

  private File createFile(String ext) {
    File file = mock(File.class);
    given(file.getName()).willReturn("a." + ext);
    return file;
  }
}
