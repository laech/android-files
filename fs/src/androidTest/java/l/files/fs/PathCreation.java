package l.files.fs;

import java.io.File;
import java.io.IOException;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;

enum PathCreation {

    FILE {
        @Override
        void createUsingOurCode(Path path) throws IOException {
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
        void createUsingOurCode(Path path) throws IOException {
            path.createDirectory();
            assertTrue(path.stat(NOFOLLOW).isDirectory());
        }

        @Override
        void createUsingSystemApi(File path) throws IOException {
            assertTrue(path.mkdir());
        }
    };

    abstract void createUsingOurCode(Path path) throws IOException;

    abstract void createUsingSystemApi(File path) throws IOException;
}
