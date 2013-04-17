package l.files.app;

import static l.files.util.FileSystem.DIRECTORY_HOME;

import java.io.File;

import com.google.common.base.Optional;

public class HomeActivity extends FilesActivity {

  @Override protected Optional<File> getDirectoryToDisplay() {
    return Optional.of(DIRECTORY_HOME);
  }

}
