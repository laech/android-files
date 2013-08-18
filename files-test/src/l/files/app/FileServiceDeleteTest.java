package l.files.app;

import static l.files.app.FileService.Delete;
import static l.files.app.FileService.delete;

import java.io.File;

public final class FileServiceDeleteTest extends FileServiceTest<Delete> {

  public FileServiceDeleteTest() {
    super(Delete.class);
  }

  public void testDeletesFile() throws Exception {
    File file = dir.newFile();
    assertTrue(file.exists());

    startService(delete(getContext(), file));

    waitForNonExistence(file);
  }
}
