package l.files.ui.preview;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class ByteBufferTest {

    @Test
    public void can_clear_buffer() throws Exception {
        ByteBuffer buffer = new ByteBuffer(10);
        buffer.putInt(0);
        buffer.clear();
        assertEquals(0, buffer.size());
    }

    @Test
    public void copy_returns_exact_copy() throws Exception {
        ByteBuffer original = new ByteBuffer(16);
        original.putInt(10);
        ByteBuffer copy = original.copy();
        assertEquals(original.size(), copy.size());
        assertArrayEquals(
                original.asOutputStream().toByteArray(),
                copy.asOutputStream().toByteArray());
    }

    @Test
    public void copy_does_not_share_internal_buffer() throws Exception {

        ByteBuffer original = new ByteBuffer(16);
        original.putInt(1);

        ByteBuffer copy = original.copy();
        copy.clear().putInt(2);

        assertArrayEquals(new byte[]{0, 0, 0, 1}, original.asOutputStream().toByteArray());
        assertArrayEquals(new byte[]{0, 0, 0, 2}, copy.asOutputStream().toByteArray());
    }

    @Test
    public void same_hash_code_for_buffers_with_same_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(1);
        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void different_hash_codes_for_buffers_with_different_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(2);
        assertNotEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void equals_if_buffers_have_same_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(1);
        assertEquals(buffer1, buffer2);
    }

    @Test
    public void not_equal_if_buffers_have_different_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(2);
        assertNotEquals(buffer1, buffer2);
    }


}