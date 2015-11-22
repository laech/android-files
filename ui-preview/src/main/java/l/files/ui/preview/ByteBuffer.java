package l.files.ui.preview;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;

final class ByteBuffer {

    private final Buffer buffer;
    private final DataOutput converter;

    ByteBuffer(int initialCapacity) {
        this.buffer = new Buffer(initialCapacity);
        this.converter = new DataOutputStream(this.buffer);
    }

    ByteArrayOutputStream asOutputStream() {
        return buffer;
    }

    byte[] buffer() {
        return buffer.buffer();
    }

    int size() {
        return buffer.size();
    }

    ByteBuffer clear() {
        buffer.reset();
        return this;
    }

    ByteBuffer put(byte[] bytes) {
        try {
            buffer.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    ByteBuffer putInt(int n) {
        try {
            converter.writeInt(n);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    ByteBuffer copy() {
        ByteBuffer copy = new ByteBuffer(buffer.size());
        try {
            copy.buffer.write(buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return copy;
    }

    static ByteBuffer readFrom(DataInputStream in) throws IOException {
        int size = in.readInt();
        if (size > 1024 * 1024) {
            throw new InvalidObjectException(String.valueOf(size));
        }
        ByteBuffer result = new ByteBuffer(size);
        in.readFully(result.buffer());
        result.buffer.size(size);
        return result;
    }

    void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(buffer.size());
        buffer.writeTo(out);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ByteBuffer && ((ByteBuffer) o).buffer.equals(buffer);
    }

    private static final class Buffer extends ByteArrayOutputStream {

        Buffer(int size) {
            super(size);
        }

        byte[] buffer() {
            return buf;
        }

        void size(int size) {
            count = size;
        }

        @Override
        public int hashCode() {
            int h = 1;
            for (int i = 0; i < count; i++) {
                h *= 1000003;
                h ^= buf[i];
            }
            return h;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Buffer)) {
                return false;
            }

            Buffer that = (Buffer) o;
            if (count != that.count) {
                return false;
            }

            for (int i = 0; i < count; i++) {
                if (buf[i] != that.buf[i]) {
                    return false;
                }
            }

            return true;
        }

    }

}
