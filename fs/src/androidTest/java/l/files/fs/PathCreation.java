package l.files.fs;

import android.annotation.SuppressLint;
import android.system.ErrnoException;
import android.system.Os;

import java.io.File;
import java.io.IOException;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
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
            assumeTrue(SDK_INT >= LOLLIPOP);
            try {
                Os.symlink("/", path.getPath());
            } catch (ErrnoException e) {
                throw new IOException(e);
            }
        }
    };

    abstract void createUsingOurCodeAssertResult(Path path) throws IOException;

    abstract void createUsingSystemApi(File path) throws IOException;
}
