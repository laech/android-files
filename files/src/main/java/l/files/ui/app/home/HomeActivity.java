package l.files.ui.app.home;

import com.google.common.base.Optional;
import com.squareup.otto.Subscribe;
import l.files.ui.app.files.FilesActivity;
import l.files.ui.event.FileSelectedEvent;
import l.files.ui.event.MediaDetectedEvent;

import java.io.File;

import static l.files.util.FileSystem.DIRECTORY_HOME;

public class HomeActivity extends FilesActivity {

  @Override protected Optional<File> getDirectoryToDisplay() {
    return Optional.of(DIRECTORY_HOME);
  }

  @Subscribe @Override public void handle(MediaDetectedEvent event) {
    super.handle(event);
  }

  @Subscribe @Override public void handle(FileSelectedEvent event) {
    super.handle(event);
  }
}
