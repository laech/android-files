package l.files.ui.preview;

import android.os.Parcel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import l.files.fs.BaseFile;
import l.files.fs.FileConsumer;
import l.files.fs.FileName;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Stat;
import l.files.fs.Stream;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

class TestFile extends BaseFile {

    private final File file;

    TestFile(File file) {
        this.file = file;
    }

    @Override
    public URI uri() {
        return file.toURI();
    }

    @Override
    public String scheme() {
        return "file";
    }

    @Override
    public String path() {
        return file.getPath();
    }

    @Override
    public FileName name() {
        return FileName.of(file.getName());
    }

    @Override
    public TestFile root() {
        return new TestFile(new File("/"));
    }

    @Override
    public TestFile parent() {
        return new TestFile(file.getParentFile());
    }

    @Override
    public TestFile resolve(String other) {
        return new TestFile(new File(file, other));
    }

    @Override
    public TestFile resolveParent(
            l.files.fs.File fromParent,
            l.files.fs.File toParent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pathStartsWith(l.files.fs.File that) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHidden() {
        return file.isHidden();
    }

    @Override
    public boolean exists(LinkOption option) throws IOException {
        return file.exists();
    }

    @Override
    public boolean isReadable() throws IOException {
        return file.canRead();
    }

    @Override
    public boolean isWritable() throws IOException {
        return file.canWrite();
    }

    @Override
    public boolean isExecutable() throws IOException {
        return file.canExecute();
    }

    @Override
    public Observation observe(
            LinkOption option,
            Observer observer,
            FileConsumer childrenConsumer) throws IOException, InterruptedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stream<l.files.fs.File> list(LinkOption option) throws IOException {

        if (!file.exists()) {
            throw new FileNotFoundException();
        }

        final File[] files = file.listFiles();
        final List<l.files.fs.File> children = new ArrayList<>(files.length);
        for (File file : files) {
            children.add(new TestFile(file));
        }

        return new Stream<l.files.fs.File>() {

            @Override
            public void close() throws IOException {
            }

            @Override
            public Iterator<l.files.fs.File> iterator() {
                return children.iterator();
            }

        };
    }

    @Override
    public Stream<l.files.fs.File> listDirs(LinkOption option) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream newInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public OutputStream newOutputStream(boolean append) throws IOException {
        return new FileOutputStream(file, append);
    }

    @Override
    public TestFile createDir() throws IOException {
        if (!file.mkdir()) {
            throw new IOException();
        }
        return this;
    }

    @Override
    public TestFile createFile() throws IOException {
        if (!file.createNewFile()) {
            throw new IOException();
        }
        return this;
    }

    @Override
    public TestFile createLink(l.files.fs.File target) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public TestFile readLink() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Stat stat(LinkOption option) throws IOException {
        return new TestStat(file);
    }

    @Override
    public void moveTo(l.files.fs.File dst) throws IOException {
        if (!file.renameTo(new File(dst.path()))) {
            throw new IOException();
        }
    }

    @Override
    public void delete() throws IOException {
        if (!file.delete()) {
            throw new IOException();
        }
    }

    @Override
    public void setLastModifiedTime(LinkOption option, Instant instant) throws IOException {
        if (!file.setLastModified(instant.to(MILLISECONDS))) {
            throw new IOException();
        }
    }

    @Override
    public void setPermissions(Set<Permission> permissions) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int describeContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        throw new UnsupportedOperationException();
    }

}
