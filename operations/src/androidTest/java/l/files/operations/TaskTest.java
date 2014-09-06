package l.files.operations;

import java.util.List;

import de.greenrobot.event.EventBus;
import l.files.common.testing.BaseTest;

import static l.files.io.file.operations.FileOperation.Failure;
import static org.assertj.core.api.Assertions.assertThat;

public class TaskTest extends BaseTest {

  private EventBus bus;

  @Override protected void setUp() throws Exception {
    super.setUp();
    bus = new EventBus();
  }

  public void testDoInBackgroundReturnsEmptyFailuresOnSuccess() {
    List<Failure> failures = create(0, bus).doInBackground();
    assertThat(failures).isNotNull().isEmpty();
  }

  protected Task create(int id, EventBus bus) {
    return new Task(id, bus) {
      @Override protected void doTask() {}
    };
  }

}
