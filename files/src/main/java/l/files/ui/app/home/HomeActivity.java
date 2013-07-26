package l.files.ui.app.home;

import com.google.common.base.Optional;
import com.squareup.otto.Subscribe;
import l.files.event.OpenFileRequest;
import l.files.ui.app.files.FilesActivity;

import java.io.File;

import static l.files.ui.UserDirs.DIR_HOME;

public class HomeActivity extends FilesActivity {

  // TODO

  @Override protected Optional<File> getDirectoryToDisplay() {
    return Optional.of(DIR_HOME);
  }

  @Subscribe @Override public void handle(OpenFileRequest request) {
    super.handle(request);
  }
}
