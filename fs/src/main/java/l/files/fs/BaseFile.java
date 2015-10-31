package l.files.fs;

import com.ibm.icu.text.CharsetDetector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

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
    public File createDirs() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        File parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createDir();
        } catch (AlreadyExist ignore) {
        }

        return this;
    }

    @Override
    public File createFiles() throws IOException {
        try {
            if (stat(NOFOLLOW).isRegularFile()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        File parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createFile();
        } catch (AlreadyExist ignore) {
        }

        return this;
    }

    @Override
    public void traverse(LinkOption option, Visitor visitor) throws IOException {
        traverse(option, visitor, null);
    }

    @Override
    public void traverse(
            LinkOption option,
            Visitor visitor,
            Comparator<File> childrenComparator) throws IOException {

        new Traverser(this, option, visitor, childrenComparator).traverse();
    }

    @Override
    public Observation observe(LinkOption option, Observer observer)
            throws IOException, InterruptedException {

        return observe(option, observer, new FileConsumer() {
            @Override
            public void accept(File file) {
            }
        });

    }

    @Override
    public Observation observe(
            LinkOption option,
            BatchObserver batchObserver,
            FileConsumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit) throws IOException, InterruptedException {

        return new BatchObserverNotifier(batchObserver)
                .start(this, option, childrenConsumer, batchInterval, batchInternalUnit);

    }

    @Override
    public File resolve(FileName other) {
        return resolve(other.toString());
    }


    @Override
    public void deleteIfExists() throws IOException {
        try {
            delete();
        } catch (FileNotFoundException ignored) {
        }
    }

    @Override
    public void deleteRecursive() throws IOException {
        traverse(NOFOLLOW, new Visitor.Base() {

            @Override
            public Result onPostVisit(File file) throws IOException {
                file.deleteIfExists();
                return super.onPostVisit(file);
            }

            @Override
            public void onException(File file, IOException e) throws IOException {
                if (e instanceof FileNotFoundException) {
                    return;
                }
                super.onException(file, e);
            }

        });
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
    public Reader newReader(Charset charset) throws IOException {
        return new InputStreamReader(newInputStream(), charset);
    }

    @Override
    public Writer newWriter(Charset charset) throws IOException {
        return new OutputStreamWriter(newOutputStream(), charset);
    }

    @Override
    public Writer newWriter(Charset charset, boolean append) throws IOException {
        return new OutputStreamWriter(newOutputStream(append), charset);
    }

    @Override
    public String readDetectingCharset(int limit) throws IOException {
        try (InputStream in = newBufferedInputStream();
             Reader reader = new CharsetDetector().getReader(in, null)) {
            if (reader != null) {
                char[] buffer = new char[limit];
                int count = reader.read(buffer);
                if (count > -1) {
                    return String.valueOf(buffer, 0, count);
                }
            }
        }
        throw new UnknownCharsetException();
    }

    private static class UnknownCharsetException extends IOException {
    }

    @Override
    public String readAllUtf8() throws IOException {
        StringBuilder builder = new StringBuilder();
        try (Reader reader = newReader(UTF_8)) {
            char[] buffer = new char[BUFFER_SIZE];
            for (int i; (i = reader.read(buffer)) != -1; ) {
                builder.append(buffer, 0, i);
            }
        }
        return builder.toString();
    }

    @Override
    public void writeAllUtf8(CharSequence content) throws IOException {
        writeAll(content, UTF_8);
    }

    @Override
    public void writeAll(CharSequence content, Charset charset) throws IOException {
        try (Writer writer = newWriter(charset)) {
            writer.write(content.toString());
        }
    }

    @Override
    public void appendUtf8(CharSequence content) throws IOException {
        try (Writer writer = newWriter(UTF_8, true)) {
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

    @Override
    public String detectBasicMediaType(Stat stat) throws IOException {
        return BasicDetector.INSTANCE.detect(this, stat);
    }

    @Override
    public String detectContentMediaType(Stat stat) throws IOException {
        return MagicDetector.INSTANCE.detect(this, stat);
    }

    @Override
    public String detectMediaType(Stat stat) throws IOException {
        return MetaMagicDetector.INSTANCE.detect(this, stat);
    }

}
