package l.files.media;

import static l.files.util.FileSystem.DIRECTORY_ALARMS;
import static l.files.util.FileSystem.DIRECTORY_ANDROID;
import static l.files.util.FileSystem.DIRECTORY_DCIM;
import static l.files.util.FileSystem.DIRECTORY_DOWNLOADS;
import static l.files.util.FileSystem.DIRECTORY_HOME;
import static l.files.util.FileSystem.DIRECTORY_MOVIES;
import static l.files.util.FileSystem.DIRECTORY_MUSIC;
import static l.files.util.FileSystem.DIRECTORY_NOTIFICATIONS;
import static l.files.util.FileSystem.DIRECTORY_PICTURES;
import static l.files.util.FileSystem.DIRECTORY_PODCASTS;
import static l.files.util.FileSystem.DIRECTORY_RINGTONES;
import static l.files.util.FileSystem.DIRECTORY_ROOT;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;

import l.files.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class ImageMapTest {

  private File file;
  private ImageMap images;

  @Before public void setUp() {
    file = mock(File.class);
    images = new ImageMap();
  }

  @Test public void getsImageForDirectory() {
    assertDirImg(R.drawable.ic_directory, getDir());
  }

  @Test public void getsImageForDirectoryHome() {
    assertDirImg(R.drawable.ic_directory_home, DIRECTORY_HOME);
  }

  @Test public void getsImageForDirectoryRoot() {
    assertDirImg(R.drawable.ic_directory_device, DIRECTORY_ROOT);
  }

  @Test public void getsImageForDirectoryAlarms() {
    assertDirImg(R.drawable.ic_directory_alarms, DIRECTORY_ALARMS);
  }

  @Test public void getsImageForDirectoryAndroid() {
    assertDirImg(R.drawable.ic_directory_android, DIRECTORY_ANDROID);
  }

  @Test public void getsImageForDirectoryDcim() {
    assertDirImg(R.drawable.ic_directory_dcim, DIRECTORY_DCIM);
  }

  @Test public void getsImageForDirectoryDownload() {
    assertDirImg(R.drawable.ic_directory_download, DIRECTORY_DOWNLOADS);
  }

  @Test public void getsImageForDirectoryMovies() {
    assertDirImg(R.drawable.ic_directory_movies, DIRECTORY_MOVIES);
  }

  @Test public void getsImageForDirectoryMusic() {
    assertDirImg(R.drawable.ic_directory_music, DIRECTORY_MUSIC);
  }

  @Test public void getsImageForDirectoryNotifications() {
    assertDirImg(R.drawable.ic_directory_notifications, DIRECTORY_NOTIFICATIONS);
  }

  @Test public void getsImageForDirectoryPictures() {
    assertDirImg(R.drawable.ic_directory_pictures, DIRECTORY_PICTURES);
  }

  @Test public void getsImageForDirectoryPodcasts() {
    assertDirImg(R.drawable.ic_directory_podcasts, DIRECTORY_PODCASTS);
  }

  @Test public void getsImageForDirectoryRingtones() {
    assertDirImg(R.drawable.ic_directory_ringtones, DIRECTORY_RINGTONES);
  }

  @Test public void getsImageForFile() {
    assertFileImg(R.drawable.ic_file, "");
  }

  @Test public void getsImageForFilePdf() {
    assertFileImg(R.drawable.ic_file_pdf, "a.pdf");
  }

  @Test public void getsImageForFileImage() {
    assertFileImg(R.drawable.ic_file_image, "a.jpg");
  }

  @Test public void getsImageForFileAudio() {
    assertFileImg(R.drawable.ic_file_audio, "a.mp3");
  }

  @Test public void getsImageForFileVideo() {
    assertFileImg(R.drawable.ic_file_video, "a.mp4");
  }

  @Test public void getsImageForFileArchive() {
    assertFileImg(R.drawable.ic_file_archive, "a.zip");
  }

  @Test public void getsImageForFileText() {
    assertFileImg(R.drawable.ic_file_text, "a.txt");
  }

  @Test public void getsImageForFileExtensionIgnoringCase() {
    assertFileImg(R.drawable.ic_file_image, "a.jPg");
  }

  private void assertDirImg(int resId, File dir) {
    assertThat(dir.mkdirs() || dir.isDirectory()).isTrue();
    assertThat(images.get(dir)).isEqualTo(resId);
  }

  private void assertFileImg(int resId, String filename) {
    assertThat(images.get(getFile(filename))).isEqualTo(resId);
  }

  private File getFile(String filename) {
    given(file.isFile()).willReturn(true);
    given(file.getName()).willReturn(filename);
    return file;
  }

  private File getDir() {
    given(file.isDirectory()).willReturn(true);
    return file;
  }
}
