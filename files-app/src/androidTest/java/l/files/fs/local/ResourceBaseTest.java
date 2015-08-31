package l.files.fs.local;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import l.files.common.testing.BaseTest;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Visitor;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;

public abstract class ResourceBaseTest extends BaseTest {

  private Resource dir1;
  private Resource dir2;

  protected final Resource dir1() {
    if (dir1 == null) {
      dir1 = LocalResource.create(createTempDir());
    }
    return dir1;
  }

  protected final Resource dir2() {
    if (dir2 == null) {
      dir2 = LocalResource.create(createTempDir());
    }
    return dir2;
  }

  private File createTempDir() {
    try {
      File file = File.createTempFile("test", null);
      assertTrue(file.delete());
      assertTrue(file.mkdirs());
      return file;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override protected void tearDown() throws Exception {
    delete(dir1);
    delete(dir2);
    super.tearDown();
  }

  private static void delete(Resource resource) throws IOException {
    if (resource == null) {
      return;
    }
    resource.traverse(
        NOFOLLOW,
        new Visitor() {
          @Override public Result accept(Resource resource) throws IOException {
            try {
              resource.setPermissions(EnumSet.allOf(Permission.class));
            } catch (IOException ignore) {
            }
            return CONTINUE;
          }
        },
        new Visitor() {
          @Override public Result accept(Resource resource) throws IOException {
            resource.delete();
            return CONTINUE;
          }
        });
  }

}
