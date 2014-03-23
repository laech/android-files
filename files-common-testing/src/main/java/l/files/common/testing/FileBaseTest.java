package l.files.common.testing;

public abstract class FileBaseTest extends BaseTest {

  private TempDir tmp;

  @Override protected void setUp() throws Exception {
    super.setUp();
    tmp = TempDir.create("tmp_");
  }

  @Override protected void tearDown() throws Exception {
    super.tearDown();
    tmp.delete();
  }

  public TempDir tmp() {
    return tmp;
  }
}
