package l.files.common.widget;

import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.io.File;

final class ImageDecorator implements Decorator<File> {

  // TODO release bitmaps that are no longer needed
  // TODO this currently treats every file as an image the first time it encounter them


  private static final class FileCallback implements Callback {
    private final File file;

    FileCallback(File file) {
      this.file = file;
    }

    @Override public void onSuccess() {}

    @Override public void onError() {
      errors.put(file, file);
    }
  }

  private static final LruCache<File, Object> errors = new LruCache<File, Object>(1000);

  private final int size;

  ImageDecorator(int size) {
    this.size = size;
  }

  @Override public void decorate(View view, File file) {
    ImageView image = (ImageView) view;
    image.setImageBitmap(null);
    if (errors.get(file) == null && file.canRead() && file.isFile()) {
      Picasso.with(view.getContext())
          .load(file)
          .resize(size, size)
          .centerCrop()
          .into(image, new FileCallback(file));
    }
  }
}
