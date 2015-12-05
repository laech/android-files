package l.files.fs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;

@Deprecated
public abstract class BaseFile implements File {

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
        Files.createDirs(path());
        return this;
    }

    @Override
    public File createFiles() throws IOException {
        Files.createFiles(path());
        return this;
    }

    @Override
    public void traverse(LinkOption option, Visitor visitor) throws IOException {
        traverse(option, visitor, null);
    }

    @Override
    public void traverse(
            final LinkOption option,
            final Visitor visitor,
            final Comparator<File> childrenComparator) throws IOException {

        Files.traverse(path(), option, new TraversalCallback<Path>() {

            private Result convert(Visitor.Result result) {
                switch (result) {
                    case CONTINUE:
                        return Result.CONTINUE;
                    case SKIP:
                        return Result.SKIP;
                    case TERMINATE:
                        return Result.TERMINATE;
                    default:
                        throw new IllegalStateException();
                }
            }

            @Override
            public Result onPreVisit(Path path) throws IOException {
                return convert(visitor.onPreVisit(newInstance(path)));
            }

            @Override
            public Result onPostVisit(Path path) throws IOException {
                return convert(visitor.onPostVisit(newInstance(path)));
            }

            @Override
            public void onException(Path path, IOException e) throws IOException {
                visitor.onException(newInstance(path), e);
            }
        });
    }

    protected abstract File newInstance(Path path);

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
            final LinkOption option,
            final BatchObserver batchObserver,
            final FileConsumer childrenConsumer,
            final long batchInterval,
            final TimeUnit batchInternalUnit) throws IOException, InterruptedException {

        return Files.observe(
                path(),
                option,
                batchObserver,
                new FileSystem.Consumer<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        childrenConsumer.accept(resolve(entry.name()));
                        return true;
                    }
                },
                batchInterval,
                batchInternalUnit);

    }

    @Override
    public void deleteIfExists() throws IOException {
        Files.deleteIfExists(path());
    }

    @Override
    public void deleteRecursive() throws IOException {
        Files.deleteRecursive(path());
    }

    @Override
    public void deleteRecursiveIfExists() throws IOException {
        Files.deleteRecursiveIfExists(path());
    }

    @Override
    public OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(path());
    }

    @Override
    public InputStream newBufferedInputStream() throws IOException {
        return Files.newBufferedInputStream(path());
    }

    @Override
    public DataInputStream newBufferedDataInputStream() throws IOException {
        return Files.newBufferedDataInputStream(path());
    }

    @Override
    public OutputStream newBufferedOutputStream() throws IOException {
        return Files.newBufferedOutputStream(path());
    }

    @Override
    public DataOutputStream newBufferedDataOutputStream() throws IOException {
        return Files.newBufferedDataOutputStream(path());
    }

    @Override
    public Reader newReader(Charset charset) throws IOException {
        return Files.newReader(path(), charset);
    }

    @Override
    public Writer newWriter(Charset charset) throws IOException {
        return Files.newWriter(path(), charset);
    }

    @Override
    public Writer newWriter(Charset charset, boolean append) throws IOException {
        return Files.newWriter(path(), charset, append);
    }

    @Override
    public String readDetectingCharset(int limit) throws IOException {
        return Files.readDetectingCharset(path(), limit);
    }

    @Override
    public String readAllUtf8() throws IOException {
        return Files.readAllUtf8(path());
    }

    @Override
    public void writeAllUtf8(CharSequence content) throws IOException {
        Files.writeUtf8(path(), content);
    }

    @Override
    public void writeAll(CharSequence content, Charset charset) throws IOException {
        Files.write(path(), content, charset);
    }

    @Override
    public void appendUtf8(CharSequence content) throws IOException {
        Files.appendUtf8(path(), content);
    }

    @Override
    public void copyFrom(InputStream in) throws IOException {
        Files.copy(in, path());
    }

    @Override
    public void removePermissions(Set<Permission> permissions) throws IOException {
        Files.removePermissions(path(), permissions);
    }

    @Override
    public String detectBasicMediaType(Stat stat) throws IOException {
        return Files.detectBasicMediaType(path(), stat);
    }

    @Override
    public String detectContentMediaType(Stat stat) throws IOException {
        return Files.detectContentMediaType(path(), stat);
    }

    @Override
    public String detectMediaType(Stat stat) throws IOException {
        return Files.detectMediaType(path(), stat);
    }

}
