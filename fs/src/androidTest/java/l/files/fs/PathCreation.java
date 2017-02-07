package l.files.fs;

import android.annotation.SuppressLint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

enum PathCreation {

    FILE {
        @Override
        void createUsingOurCodeAssertResult(Path path) throws IOException {
            path.createFile();
            assertTrue(path.stat(NOFOLLOW).isRegularFile());
        }

        @Override
        void createUsingSystemApi(File path) throws IOException {
            assertTrue(path.createNewFile());
        }
    },

    DIRECTORY {
        @Override
        void createUsingOurCodeAssertResult(Path path) throws IOException {
            path.createDirectory();
            assertTrue(path.stat(NOFOLLOW).isDirectory());
        }

        @Override
        void createUsingSystemApi(File path) throws IOException {
            assertTrue(path.mkdir());
        }
    },

    SYMBOLIC_LINK {
        @Override
        void createUsingOurCodeAssertResult(Path path) throws IOException {
            path.createSymbolicLink(Path.of("/"));
            assertTrue(path.stat(NOFOLLOW).isSymbolicLink());
            assertEquals("/", path.readSymbolicLink().toString());
        }

        @Override
        @SuppressLint("NewApi")
        void createUsingSystemApi(File path) throws IOException {
            assumeTrue("android.system.Os not available", SDK_INT >= LOLLIPOP);
            try {
                android.system.Os.symlink("/", path.getPath());
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
    };

    abstract void createUsingOurCodeAssertResult(Path path) throws IOException;

    abstract void createUsingSystemApi(File path) throws IOException;

    static Iterable<PathCreation[]> valuesAsJUnitParameters() {
        List<PathCreation[]> parameters = new ArrayList<>();
        for (PathCreation value : values()) {
            parameters.add(new PathCreation[]{value});
        }
        return unmodifiableList(parameters);
    }
}