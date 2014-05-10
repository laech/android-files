package l.files.io.file.operations;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.FileBaseTest;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.io.file.operations.Cancellables.NO_CANCEL;
import static l.files.io.file.operations.Count.Listener;
import static l.files.io.file.operations.Count.Result;

public final class CountTest extends FileBaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onFileCounted(int count, long length) {}
  };

  public void testCount() throws Exception {
    File a = tmp().createFile("1/a.txt");
    File b = tmp().createFile("2/b.txt");
    File c = tmp().createFile("3/4/c.txt");
    write("Test", a, UTF_8);
    write("Testing", b, UTF_8);
    write("Testing again", c, UTF_8);

    Result result = count(tmp().get());
    assertEquals(3, result.count);
    assertEquals(a.length() + b.length() + c.length(), result.length);
  }

  private Result count(File file) throws IOException {
    return new Count(NO_CANCEL, asList(file), NULL_LISTENER).call();
  }
}
