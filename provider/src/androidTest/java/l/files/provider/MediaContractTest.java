package l.files.provider;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Optional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import l.files.common.testing.FileBaseTest;

import static android.graphics.Bitmap.CompressFormat;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.CompressFormat.PNG;
import static android.graphics.Bitmap.Config;
import static android.graphics.Bitmap.Config.ARGB_4444;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.Config.RGB_565;
import static l.files.provider.FilesContract.getFileId;
import static l.files.provider.MediaContract.Bounds;
import static l.files.provider.MediaContract.decodeBounds;

public final class MediaContractTest extends FileBaseTest {

  public void testDecodeBoundsDoesNotHangOnSystemFiles() throws Exception {
    Runnable fail = new Runnable() {
      @Override public void run() {
        throw new AssertionError("Failed");
      }
    };
    Handler handler = new Handler(Looper.getMainLooper());
    handler.postDelayed(fail, 1000);

    // Calling BitmapFactory directly on these files will cause the Android
    // native code to retry infinitely
    decodeBounds(getContext(), getFileId(new File("/proc/1/maps")));
    handler.removeCallbacks(fail);
  }

  public void testDecodeBounds() throws Exception {
    Random random = new Random();
    for (Config config : new Config[]{RGB_565, ARGB_4444, ARGB_8888}) {
      for (CompressFormat format : new CompressFormat[]{JPEG, PNG}) {
        int width = random.nextInt(1000) + 1;
        int height = random.nextInt(1000) + 1;
        File file = file(config, format, width, height);
        testDecodeBounds(file, config, format, width, height);
      }
    }
  }

  private File file(Config conf, CompressFormat fmt, int width, int height) {
    return tmp().createFile(conf + "_" + width + "x" + height + "." + fmt);
  }

  private void testDecodeBounds(
      File file, Config config, CompressFormat format, int width, int height) {
    Bitmap bitmap = Bitmap.createBitmap(width, height, config);
    try (OutputStream out = new FileOutputStream(file)) {
      bitmap.compress(format, 100, out);
    } catch (IOException e) {
      throw new AssertionError(file.toString(), e);
    } finally {
      bitmap.recycle();
    }
    Optional<Bounds> bounds = decodeBounds(getContext(), getFileId(file));
    assertTrue("Failed to decode " + file.toString(), bounds.isPresent());
    assertEquals(file.toString(), width, bounds.get().width());
    assertEquals(file.toString(), height, bounds.get().height());
  }
}
