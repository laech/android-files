package l.files.fs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.io.IOException;

import l.files.fs.exception.AccessDenied;
import l.files.testing.fs.PathBaseTest;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Parameterized.class)
public final class PathCreateFailureAccessDeniedTest extends PathBaseTest {

    private final PathCreation creation;

    public PathCreateFailureAccessDeniedTest(PathCreation creation) {
        this.creation = creation;
    }

    @Parameters(name = "{0}")
    public static Iterable<PathCreation[]> data() {
        return PathCreation.valuesAsJUnitParameters();
    }

    @Test
    public void create_failure_due_to_no_write_permission_at_parent()
            throws Exception {

        Path path = dir1().concat("a");
        assertTrue(new File(dir1().toString()).setWritable(false));
        creationFailureAccessDenied(path);
    }

    @Test
    public void create_failure_due_to_no_execute_permission_at_ancestor()
            throws Exception {

        Path parent = dir1().concat("sub");
        assertTrue(new File(parent.toString()).mkdir());
        assertTrue(new File(dir1().toString()).setExecutable(false));

        Path path = parent.concat("a");
        creationFailureAccessDenied(path);
    }

    private void creationFailureAccessDenied(Path path) throws IOException {
        try {
            creation.createUsingOurCodeAssertResult(path);
            fail("Expecting " + AccessDenied.class.getName());
        } catch (AccessDenied e) {
            // Pass
        }
    }

}
