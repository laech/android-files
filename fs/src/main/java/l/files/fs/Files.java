package l.files.fs;

import java.io.IOException;

public final class Files {

    private Files() {
    }

    public static void traverse(
            File file,
            LinkOption option,
            Visitor visitor) throws IOException {

        new Traverser(file, option, visitor).traverse();
    }

}
