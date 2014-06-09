package l.files.provider;

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

    private void waitForFileToNotExist(File file) throws InterruptedException {
        for (int i = 0; file.exists(); i++) {
            sleep(10);
            if (i >= 9) {
                fail();
            }
        }
    }
}
