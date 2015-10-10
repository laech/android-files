package l.files.fs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.FOLLOW;

public abstract class BaseFile implements File {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public String toString() {
        return path();
    }

    @Override
    public List<File> hierarchy() {
        List<File> hierarchy = new ArrayList<>();
        for (File p = this; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        reverse(hierarchy);
        return unmodifiableList(hierarchy);
    }

    @Override
    public Closeable observe(LinkOption option, Observer observer) throws IOException {

        return observe(option, observer, new FileConsumer() {
            @Override
            public void accept(File file) {
            }
        });

    }

    @Override
    public Closeable observe(
            LinkOption option,
            BatchObserver batchObserver,
            FileConsumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit) throws IOException {

        return new BatchObserverNotifier(batchObserver)
                .start(this, option, childrenConsumer, batchInterval, batchInternalUnit);

    }

    @Override
    public File resolve(FileName other) {
        return resolve(other.toString());
    }

    @Override
    public OutputStream newOutputStream() throws IOException {
        return newOutputStream(false);
    }

    @Override
    public InputStream newBufferedInputStream() throws IOException {
        return new BufferedInputStream(newInputStream());
    }

    @Override
    public DataInputStream newBufferedDataInputStream() throws IOException {
        return new DataInputStream(newBufferedInputStream());
    }

    @Override
    public OutputStream newBufferedOutputStream() throws IOException {
        return new BufferedOutputStream(newOutputStream());
    }

    @Override
    public DataOutputStream newBufferedDataOutputStream() throws IOException {
        return new DataOutputStream(newBufferedOutputStream());
    }

    @Override
    public String readAllUtf8() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = new InputStreamReader(newInputStream(), UTF_8)) {
            char[] buffer = new char[BUFFER_SIZE];
            for (int i; (i = reader.read(buffer)) != -1; ) {
                builder.append(buffer, 0, i);
            }
        }
        return builder.toString();
    }

    @Override
    public void writeAllUtf8(CharSequence content) throws IOException {
        try (Writer writer = new OutputStreamWriter(newOutputStream())) {
            writer.write(content.toString());
        }
    }

    @Override
    public void appendUtf8(CharSequence content) throws IOException {
        try (Writer writer = new OutputStreamWriter(newOutputStream(true), UTF_8)) {
            writer.write(content.toString());
        }
    }

    @Override
    public void copyFrom(InputStream in) throws IOException {
        try (OutputStream out = newOutputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            for (int i; (i = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, i);
            }
        }
    }

    @Override
    public void removePermissions(Set<Permission> permissions) throws IOException {
        Set<Permission> existing = stat(FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(perms);
    }

}
