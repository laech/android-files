package l.files.media;

import static java.lang.System.nanoTime;
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

import java.io.File;
import java.io.IOException;

import l.files.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public final class ImageMapTest {

  @Rule public TemporaryFolder dir = new TemporaryFolder();

  private ImageMap images;

  @Before public void setUp() throws Exception {
    images = new ImageMap();
  }

  @Test public void getsImageForDirectory() throws Exception {
    assertDirImg(R.drawable.ic_directory, dir.newFolder());
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

  @Test public void getsImageForFile() throws Exception {
    assertFileImg(R.drawable.ic_file, "");
  }

  @Test public void getsImageForFilePdf() throws Exception {
    assertFileImg(R.drawable.ic_file_pdf, "pdf");
  }

  @Test public void getsImageForFileImage() {
    assertFileImg(R.drawable.ic_file_image, "jpg");
  }

  @Test public void getsImageForFileAudio() {
    assertFileImg(R.drawable.ic_file_audio, "mp3");
  }

  @Test public void getsImageForFileVideo() {
    assertFileImg(R.drawable.ic_file_video, "mp4");
  }

  @Test public void getsImageForFileArchive() {
    assertFileImg(R.drawable.ic_file_archive, "zip");
  }

  @Test public void getsImageForFileText() {
    assertFileImg(R.drawable.ic_file_text, "txt");
  }

  @Test public void getsImageForFileExtensionIgnoringCase() {
    assertFileImg(R.drawable.ic_file_image, "jPg");
  }

  private void assertDirImg(int resId, File dir) {
    assertThat(dir.mkdirs() || dir.isDirectory()).isTrue();
    assertThat(images.get(dir)).isEqualTo(resId);
  }

  private void assertFileImg(int resId, String ext) {
    assertThat(images.get(createFile(ext))).isEqualTo(resId);
  }

  private File createFile(String ext) {
    try {
      return dir.newFile(String.valueOf(nanoTime()) + "." + ext);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
