package l.files.fs;

import java.io.File;
import java.io.IOException;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertTrue;

enum PathCreation {

    FILE {
        @Override
        void create(Path path) throws IOException {
            path.createFile();
            assertTrue(path.stat(NOFOLLOW).isRegularFile());
        }

        @Override
        void create(File path) throws IOException {
            assertTrue(path.createNewFile());
        }
    },

    DIRECTORY {
        @Override
        void create(Path path) throws IOException {
            path.createDirectory();
            assertTrue(path.stat(NOFOLLOW).isDirectory());
        }

        @Override
        void create(File path) throws IOException {
            assertTrue(path.mkdir());
        }
    };

    abstract void create(Path path) throws IOException;

    abstract void create(File path) throws IOException;
}
