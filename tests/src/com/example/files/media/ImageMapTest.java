package com.example.files.media;

import com.example.files.R;
import com.example.files.test.TempFolder;
import junit.framework.TestCase;

import java.io.File;

import static com.example.files.test.TempFolder.newTempFolder;
import static com.example.files.util.FileSystem.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class ImageMapTest extends TestCase {

  private TempFolder folder;
  private ImageMap images;

  @Override protected void setUp() throws Exception {
    super.setUp();
    images = new ImageMap();
    folder = newTempFolder();
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    folder.delete();
  }

  public void testGetsImageForAlarmsDirectory() {
    assertEquals(R.drawable.ic_folder_alarms, images.get(DIRECTORY_ALARMS));
  }

  public void testGetsImageForAndroidDirectory() {
    assertEquals(R.drawable.ic_folder_android, images.get(DIRECTORY_ANDROID));
  }

  public void testGetsImageForDcimDirectory() {
    assertEquals(R.drawable.ic_folder_dcim, images.get(DIRECTORY_DCIM));
  }

  public void testGetsImageForDownloadDirectory() {
    assertEquals(R.drawable.ic_folder_download, images.get(DIRECTORY_DOWNLOADS));
  }

  public void testGetsImageForMoviesDirectory() {
    assertEquals(R.drawable.ic_folder_movies, images.get(DIRECTORY_MOVIES));
  }

  public void testGetsImageForMusicDirectory() {
    assertEquals(R.drawable.ic_folder_music, images.get(DIRECTORY_MUSIC));
  }

  public void testGetsImageForNotificationsDirectory() {
    assertEquals(R.drawable.ic_folder_notifications, images.get(DIRECTORY_NOTIFICATIONS));
  }

  public void testGetsImageForPicturesDirectory() {
    assertEquals(R.drawable.ic_folder_pictures, images.get(DIRECTORY_PICTURES));
  }

  public void testGetsImageForPodcastsDirectory() {
    assertEquals(R.drawable.ic_folder_podcasts, images.get(DIRECTORY_PODCASTS));
  }

  public void testGetsImageForRingtonesDirectory() {
    assertEquals(R.drawable.ic_folder_ringtones, images.get(DIRECTORY_RINGTONES));
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
