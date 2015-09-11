package l.files.fs;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.common.base.Consumer;

import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.fs.LinkOption.FOLLOW;

public abstract class BaseFile implements File {

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

        return observe(option, observer, new Consumer<File>() {
            @Override
            public void apply(File input) {
            }
        });

    }

    @Override
    public File resolve(FileName other) {
        return resolve(other.toString());
    }

    @Override
    public OutputStream output() throws IOException {
        return output(false);
    }

    @Override
    public Writer writer(Charset charset) throws IOException {
        return new OutputStreamWriter(output(), charset);
    }

    @Override
    public Writer writer(Charset charset, boolean append) throws IOException {
        return new OutputStreamWriter(output(append), charset);
    }

    @Override
    public void removePermissions(Set<Permission> permissions) throws IOException {
        Set<Permission> existing = stat(FOLLOW).permissions();
        Set<Permission> perms = new HashSet<>(existing);
        perms.removeAll(permissions);
        setPermissions(perms);
    }

    @Override
    public String toString(Charset charset) throws IOException {
        return toString(charset, new StringBuilder()).toString();
    }

    @Override
    public <T extends Appendable> T toString(Charset charset, T appendable) throws IOException {
        try (Reader reader = reader(charset)) {
            for (CharBuffer buffer = CharBuffer.allocate(8192);
                 reader.read(buffer) > -1; ) {
                buffer.flip();
                appendable.append(buffer);
            }
        }
        return appendable;
    }

    @Override
    public void writeString(Charset charset, CharSequence content) throws IOException {
        try (Writer writer = writer(charset)) {
            writer.write(content.toString());
        }
    }

}
