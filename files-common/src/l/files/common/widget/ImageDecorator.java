package l.files.common.widget;

import android.view.View;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.io.File;

final class ImageDecorator implements Decorator<File> {

  private final int imageViewId;
  private final int size;

  ImageDecorator(int imageViewId, int size) {
    this.imageViewId = imageViewId;
    this.size = size;
  }

  @Override public void decorate(View view, File file) {
    ImageView image = (ImageView) view.findViewById(imageViewId);
    image.setImageBitmap(null);

    // Try to load the file anyway, if it's not an image, not image will be
    // shown, this can load images even when no standard image extension is
    // used for the file name, at the cause of doing extra work. TODO optimize this

    if (file.isFile()) {
      // TODO https://github.com/square/picasso/issues/206
      Picasso.with(view.getContext())
          .load(file)
          .resize(size, size)
          .centerCrop()
          .into(image);
    }
  }
}
