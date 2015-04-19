package l.files.fs.local;

import com.google.common.io.Closer;

import java.io.Closeable;
import java.io.IOException;
import java.util.EnumSet;

import l.files.common.testing.BaseTest;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.ResourceVisitor;
import l.files.fs.local.LocalResource;

import static com.google.common.io.Files.createTempDir;
import static l.files.fs.ResourceVisitor.Order.PRE;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;

public abstract class ResourceBaseTest extends BaseTest {

    private LocalResource dir1;
    private LocalResource dir2;

    protected final LocalResource dir1() {
        if (dir1 == null) {
            dir1 = LocalResource.create(createTempDir());
        }
        return dir1;
    }

    protected final LocalResource dir2() {
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
        resource.traverse(new ResourceVisitor() {
            @Override
            public Result accept(Order order, Resource resource) throws IOException {
                if (order == PRE) {
                    try {
                        resource.setPermissions(EnumSet.allOf(Permission.class));
                    } catch (UnsupportedOperationException e) {
                        // TODO specify type in callback to avoid this catch
                    }
                } else {
                    resource.delete();
                }
                return CONTINUE;
            }
        });
    }

}
