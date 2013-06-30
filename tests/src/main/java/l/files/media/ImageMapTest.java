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

import junit.framework.TestCase;
import l.files.R;

public final class ImageMapTest extends TestCase {

  private File file;
  private ImageMap images;

  @Override protected void setUp() throws Exception {
    super.setUp();
    file = mock(File.class);
    images = new ImageMap();
  }

  public void testGetsImageForDirectory() {
    assertDirImg(R.drawable.ic_directory, getDir());
  }

  public void testGetsImageForDirectoryHome() {
    assertDirImg(R.drawable.ic_directory_home, DIRECTORY_HOME);
  }

  public void testGetsImageForDirectoryRoot() {
    assertDirImg(R.drawable.ic_directory_device, DIRECTORY_ROOT);
  }

  public void testGetsImageForDirectoryAlarms() {
    assertDirImg(R.drawable.ic_directory_alarms, DIRECTORY_ALARMS);
  }

  public void testGetsImageForDirectoryAndroid() {
    assertDirImg(R.drawable.ic_directory_android, DIRECTORY_ANDROID);
  }

  public void testGetsImageForDirectoryDcim() {
    assertDirImg(R.drawable.ic_directory_dcim, DIRECTORY_DCIM);
  }

  public void testGetsImageForDirectoryDownload() {
    assertDirImg(R.drawable.ic_directory_download, DIRECTORY_DOWNLOADS);
  }

  public void testGetsImageForDirectoryMovies() {
    assertDirImg(R.drawable.ic_directory_movies, DIRECTORY_MOVIES);
  }

  public void testGetsImageForDirectoryMusic() {
    assertDirImg(R.drawable.ic_directory_music, DIRECTORY_MUSIC);
  }

  public void testGetsImageForDirectoryNotifications() {
    assertDirImg(R.drawable.ic_directory_notifications, DIRECTORY_NOTIFICATIONS);
  }

  public void testGetsImageForDirectoryPictures() {
    assertDirImg(R.drawable.ic_directory_pictures, DIRECTORY_PICTURES);
  }

  public void testGetsImageForDirectoryPodcasts() {
    assertDirImg(R.drawable.ic_directory_podcasts, DIRECTORY_PODCASTS);
  }

  public void testGetsImageForDirectoryRingtones() {
    assertDirImg(R.drawable.ic_directory_ringtones, DIRECTORY_RINGTONES);
  }

  public void testGetsImageForFile() {
    assertFileImg(R.drawable.ic_file, "");
  }

  public void testGetsImageForFilePdf() {
    assertFileImg(R.drawable.ic_file_pdf, "a.pdf");
  }

  public void testGetsImageForFileImage() {
    assertFileImg(R.drawable.ic_file_image, "a.jpg");
  }

  public void testGetsImageForFileAudio() {
    assertFileImg(R.drawable.ic_file_audio, "a.mp3");
  }

  public void testGetsImageForFileVideo() {
    assertFileImg(R.drawable.ic_file_video, "a.mp4");
  }

  public void testGetsImageForFileArchive() {
    assertFileImg(R.drawable.ic_file_archive, "a.zip");
  }

  public void testGetsImageForFileText() {
    assertFileImg(R.drawable.ic_file_text, "a.txt");
  }

  public void testGetsImageForFileExtensionIgnoringCase() {
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
