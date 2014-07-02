package l.files.provider;

import com.google.common.base.Supplier;

import java.io.File;

import l.files.common.testing.FileBaseTest;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static l.files.provider.FilesContract.getFileLocation;

public final class FilesContractTest extends FileBaseTest {

    public void testDeletesFile() throws Exception {
        File file = tmp().createFile("a");
        String location = getFileLocation(file);

        FilesContract.delete(getContext(), asList(location));

        waitForFileToNotExist(file);
    }

    public void testCopiesFile() throws Exception {
        tmp().createFile("a/b");
        String srcLocation = getFileLocation(tmp().get("a"));
        String dstLocation = getFileLocation(tmp().createDir("1"));

        FilesContract.copy(getContext(), asList(srcLocation), dstLocation);

        waitForFileToExist(tmp().get("1/a/b"));
        assertTrue(tmp().get("a/b").exists());
    }

    private void waitForFileToExist(final File file) throws InterruptedException {
        waitFor(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return file.exists();
            }
        });
    }

    private void waitForFileToNotExist(final File file) throws InterruptedException {
        waitFor(new Supplier<Boolean>() {
            @Override
            public Boolean get() {
                return !file.exists();
            }
        });
    }

    private void waitFor(Supplier<Boolean> success) throws InterruptedException {
        for (int i = 0; !success.get(); i++) {
            sleep(50);
            if (i >= 9) {
                fail();
            }
        }
    }
}
