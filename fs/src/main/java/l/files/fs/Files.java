package l.files.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

public final class Files {

    private Files() {
    }

    public static List<Path> hierarchy(Path path) {
        List<Path> hierarchy = new ArrayList<>();
        for (Path p = path; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        reverse(hierarchy);
        return unmodifiableList(hierarchy);
    }

    public static OutputStream newOutputStream(Path path) throws IOException {
        return newOutputStream(path, false);
    }

    public static OutputStream newOutputStream(Path path, boolean append)
            throws IOException {
        return path.fileSystem().newOutputStream(path, append);
    }

    public static InputStream newInputStream(Path path)
            throws IOException {
        return path.fileSystem().newInputStream(path);
    }

    public static InputStream newBufferedInputStream(Path path)
            throws IOException {
        return new BufferedInputStream(newInputStream(path));
    }

    public static DataInputStream newBufferedDataInputStream(Path path)
            throws IOException {
        return new DataInputStream(newBufferedInputStream(path));
    }

    public static OutputStream newBufferedOutputStream(Path path)
            throws IOException {
        return new BufferedOutputStream(newOutputStream(path));
    }

    public static DataOutputStream newBufferedDataOutputStream(Path path)
            throws IOException {
        return new DataOutputStream(newBufferedOutputStream(path));
    }

}
