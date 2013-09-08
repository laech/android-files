package l.files.common.widget;

import static java.util.Locale.ENGLISH;
import static org.apache.commons.io.FilenameUtils.getExtension;

import android.view.View;
import android.widget.ImageView;
import com.google.common.collect.ImmutableSet;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.Set;

final class ImageDecorator implements Decorator<File> {

  // See http://developer.android.com/guide/appendix/media-formats.html
  private static final Set<String> SUPPORTED_FORMATS =
      ImmutableSet.of("jpg", "gif", "png", "bmp", "webp");

  private final int size;

  ImageDecorator(int size) {
    this.size = size;
  }

  @Override public void decorate(View view, File file) {
    ImageView image = (ImageView) view;
    image.setImageBitmap(null);
    if (file.isFile() && isImage(file)) {
      Picasso.with(view.getContext())
          .load(file)
          .resize(size, size)
          .centerCrop()
          .into(image);
    }
  }

  private boolean isImage(File file) {
    String ext = getExtension(file.getName()).toLowerCase(ENGLISH);
    return SUPPORTED_FORMATS.contains(ext);
  }
}