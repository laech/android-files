package l.files.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import l.files.R;
import l.files.media.ImageMap;
import l.files.util.FileSystem;
import l.files.widget.AnimatedAdapter;

import java.io.File;

public final class FilesAdapter2 extends AnimatedAdapter<File> {

  private final FileSystem fileSystem;
  private final ImageMap images;

  public FilesAdapter2(ListView parent) {
    this(parent, FileSystem.INSTANCE, ImageMap.INSTANCE);
  }

  public FilesAdapter2(ListView parent, FileSystem fileSystem, ImageMap images) {
    super(parent);
    this.fileSystem = fileSystem;
    this.images = images;
  }

  @Override protected View newView(File file, ViewGroup parent) {
    return inflate(R.layout.files_item, parent);
  }

  @Override protected void bindView(File file, View view) {
    TextView textView = (TextView) view;
    textView.setEnabled(fileSystem.hasPermissionToRead(file));
    textView.setText(file.getName());
    textView.setCompoundDrawablesWithIntrinsicBounds(images.get(file), 0, 0, 0);
  }
}
