package l.files.io.file.operations;

import com.google.common.base.Function;

import org.mockito.ArgumentCaptor;

import java.io.File;
import java.util.Set;

import l.files.common.testing.FileBaseTest;
import l.files.io.file.FileInfo;

import static com.google.common.collect.Lists.transform;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static l.files.io.file.operations.Count.Listener;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class CountTest extends FileBaseTest {

  public void testCount() throws Exception {
    tmp().createFile("1/a.txt");
    tmp().createFile("3/4/c.txt");

    Listener listener = mock(Listener.class);
    ArgumentCaptor<FileInfo> captor = ArgumentCaptor.forClass(FileInfo.class);

    count(listener, tmp().get());
    verify(listener, atLeastOnce()).onCount(captor.capture());

    Set<String> expected = newHashSet(
        tmp().get().getPath(),
        tmp().get("1").getPath(),
        tmp().get("1/a.txt").getPath(),
        tmp().get("3").getPath(),
        tmp().get("3/4").getPath(),
        tmp().get("3/4/c.txt").getPath());

    Set<String> actual = newHashSet(transform(captor.getAllValues(),
        new Function<FileInfo, String>() {
          @Override public String apply(FileInfo input) {
            return input.path();
          }
        }
    ));
    assertEquals(expected, actual);
  }

  private void count(Listener listener, File file) throws InterruptedException {
    new Count(listener, asList(file.getPath())).call();
  }
}
