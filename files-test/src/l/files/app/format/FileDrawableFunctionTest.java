package l.files.app.format;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import junit.framework.TestCase;
import l.files.R;

import java.io.File;

import static l.files.app.UserDirs.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FileDrawableFunctionTest extends TestCase {

  private Resources res;
  private File file;
  private FileDrawableFunction images;

  @Override protected void setUp() throws Exception {
    super.setUp();
    res = mock(Resources.class);
    file = mock(File.class);
    images = new FileDrawableFunction(res);
  }

  public void testGetsImageForDirectory() {
    assertDirImg(R.drawable.ic_dir, getDir());
  }

  public void testGetsImageForDirectoryHome() {
    assertDirImg(R.drawable.ic_dir_home, DIR_HOME);
  }

  public void testGetsImageForDirectoryRoot() {
    assertDirImg(R.drawable.ic_dir_device, DIR_ROOT);
  }

  public void testGetsImageForDirectoryAlarms() {
    assertDirImg(R.drawable.ic_dir_alarms, DIR_ALARMS);
  }

  public void testGetsImageForDirectoryAndroid() {
    assertDirImg(R.drawable.ic_dir_android, DIR_ANDROID);
  }

  public void testGetsImageForDirectoryDcim() {
    assertDirImg(R.drawable.ic_dir_dcim, DIR_DCIM);
  }

  public void testGetsImageForDirectoryDownload() {
    assertDirImg(R.drawable.ic_dir_download, DIR_DOWNLOADS);
  }

  public void testGetsImageForDirectoryMovies() {
    assertDirImg(R.drawable.ic_dir_movies, DIR_MOVIES);
  }

  public void testGetsImageForDirectoryMusic() {
    assertDirImg(R.drawable.ic_dir_music, DIR_MUSIC);
  }

  public void testGetsImageForDirectoryNotifications() {
    assertDirImg(R.drawable.ic_dir_notifications, DIR_NOTIFICATIONS);
  }

  public void testGetsImageForDirectoryPictures() {
    assertDirImg(R.drawable.ic_dir_pictures, DIR_PICTURES);
  }

  public void testGetsImageForDirectoryPodcasts() {
    assertDirImg(R.drawable.ic_dir_podcasts, DIR_PODCASTS);
  }

  public void testGetsImageForDirectoryRingtones() {
    assertDirImg(R.drawable.ic_dir_ringtones, DIR_RINGTONES);
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
    Drawable drawable = getDrawable(resId);
    assertEquals(drawable, images.apply(dir));
  }

  private void assertFileImg(int resId, String filename) {
    Drawable drawable = getDrawable(resId);
    assertEquals(drawable, images.apply(getFile(filename)));
  }

  private Drawable getDrawable(int resId) {
    Drawable drawable = mock(Drawable.class);
    given(res.getDrawable(resId)).willReturn(drawable);
    return drawable;
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
