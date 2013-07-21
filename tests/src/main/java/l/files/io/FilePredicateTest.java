package l.files.io;

import junit.framework.TestCase;

import java.io.File;

import static l.files.io.FilePredicate.CAN_READ;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public final class FilePredicateTest extends TestCase {

  public void testCanRead() {
    File file = mock(File.class);

    given(file.canRead()).willReturn(true);
    assertThat(CAN_READ.apply(file)).isTrue();

    given(file.canRead()).willReturn(false);
    assertThat(CAN_READ.apply(file)).isFalse();
  }
}
