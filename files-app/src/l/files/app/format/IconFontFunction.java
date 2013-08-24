package l.files.app.format;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.google.common.base.Function;
import java.io.File;

final class IconFontFunction implements Function<File, Typeface> {

  private final AssetManager assets;

  IconFontFunction(AssetManager assets) {
    this.assets = checkNotNull(assets, "assets");
  }

  @Override public Typeface apply(File file) {
    if (file.isDirectory()) {
      return IconFonts.dir(assets, file);
    } else {
      return IconFonts.file(assets, file);
    }
  }
}
