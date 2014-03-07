package l.files.service;

import java.io.File;
import java.io.IOException;

import l.files.common.testing.BaseTest;
import l.files.common.testing.TempDir;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Files.write;
import static java.util.Arrays.asList;
import static l.files.service.Cancellables.NO_CANCEL;
import static l.files.service.Counter.Listener;
import static l.files.service.Counter.Result;

public final class CounterTest extends BaseTest {

  private static final Listener NULL_LISTENER = new Listener() {
    @Override public void onFileCounted(int count, long length) {}
  };
  private TempDir tempDir;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tempDir = TempDir.create();
  }

  @Override protected void tearDown() throws Exception {
    tempDir.delete();
    super.tearDown();
  }

  public void testCount() throws Exception {
    File a = tempDir.newFile("1/a.txt");
    File b = tempDir.newFile("2/b.txt");
    File c = tempDir.newFile("3/4/c.txt");
    write("Test", a, UTF_8);
    write("Testing", b, UTF_8);
    write("Testing again", c, UTF_8);

    Result result = count(tempDir.get());
    assertEquals(3, result.count);
    assertEquals(a.length() + b.length() + c.length(), result.length);
  }

  private Result count(File file) throws IOException {
    return new Counter(NO_CANCEL, asList(file), NULL_LISTENER).call();
  }
}
