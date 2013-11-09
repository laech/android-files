package l.files.app.format;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.webkit.MimeTypeMap;

import com.google.common.base.Function;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.io.FilenameUtils.getExtension;

final class IconFontFunction implements Function<File, Typeface> {

  private final AssetManager assets;

  IconFontFunction(AssetManager assets) {
    this.assets = checkNotNull(assets, "assets");
  }

  @Override public Typeface apply(File file) {
    if (file.isDirectory()) {
      return IconFonts.forDirectoryUri(assets, file.toURI().toString());
    } else {
      return IconFonts.forFileMediaType(assets, MimeTypeMap.getSingleton()
          .getMimeTypeFromExtension(getExtension(file.getName())));
    }
  }
}
