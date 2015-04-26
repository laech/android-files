package l.files.fs.local;

import com.google.common.io.Closer;

import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;

import l.files.common.testing.BaseTest;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceException;
import l.files.fs.ResourceVisitor;

import static com.google.common.io.Files.createTempDir;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;

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

    @Override
    protected void tearDown() throws Exception {
        try (Closer closer = Closer.create()) {
            closer.register(deleteOnClose(dir1));
            closer.register(deleteOnClose(dir2));
        }
        super.tearDown();
    }

    private static Closeable deleteOnClose(final Resource resource) {
        return new Closeable() {
            @Override
            public void close() throws IOException {
                if (resource != null) {
                    delete(resource);
                }
            }
        };
    }

    private static void delete(Resource resource) throws IOException {
        resource.traverse(
                NOFOLLOW,
                new ResourceVisitor() {
                    @Override
                    public Result accept(Resource resource) throws IOException {
                        try {
                            resource.setPermissions(EnumSet.allOf(Permission.class));
                        } catch (ResourceException ignore) {
                        }
                        return CONTINUE;
                    }
                },
                new ResourceVisitor() {
                    @Override
                    public Result accept(Resource resource) throws IOException {
                        resource.delete();
                        return CONTINUE;
                    }
                });
    }

}