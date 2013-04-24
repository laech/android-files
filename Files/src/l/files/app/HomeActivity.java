package l.files.app;

import static l.files.util.FileSystem.DIRECTORY_HOME;

import java.io.File;

import com.google.common.base.Optional;
import com.squareup.otto.Subscribe;
import l.files.event.FileSelectedEvent;
import l.files.event.MediaDetectedEvent;

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
