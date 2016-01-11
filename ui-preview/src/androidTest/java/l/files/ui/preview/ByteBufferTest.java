package l.files.ui.preview;

import android.test.MoreAsserts;

import junit.framework.TestCase;

import static android.test.MoreAsserts.assertNotEqual;


public final class ByteBufferTest extends TestCase {

    public void test_can_clear_buffer() throws Exception {
        ByteBuffer buffer = new ByteBuffer(10);
        buffer.putInt(0);
        buffer.clear();
        assertEquals(0, buffer.size());
    }

    public void test_copy_returns_exact_copy() throws Exception {
        ByteBuffer original = new ByteBuffer(16);
        original.putInt(10);
        ByteBuffer copy = original.copy();
        assertEquals(original.size(), copy.size());
        MoreAsserts.assertEquals(
                original.asOutputStream().toByteArray(),
                copy.asOutputStream().toByteArray());
    }

    public void test_copy_does_not_share_internal_buffer() throws Exception {

        ByteBuffer original = new ByteBuffer(16);
        original.putInt(1);

        ByteBuffer copy = original.copy();
        copy.clear().putInt(2);

        MoreAsserts.assertEquals(new byte[]{0, 0, 0, 1}, original.asOutputStream().toByteArray());
        MoreAsserts.assertEquals(new byte[]{0, 0, 0, 2}, copy.asOutputStream().toByteArray());
    }

    public void test_same_hash_code_for_buffers_with_same_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(1);
        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    public void test_different_hash_codes_for_buffers_with_different_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(2);
        assertNotEqual(buffer1.hashCode(), buffer2.hashCode());
    }

    public void test_equals_if_buffers_have_same_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(1);
        assertEquals(buffer1, buffer2);
    }

    public void test_not_equal_if_buffers_have_different_content() throws Exception {
        ByteBuffer buffer1 = new ByteBuffer(4);
        ByteBuffer buffer2 = new ByteBuffer(4);
        buffer1.putInt(1);
        buffer2.putInt(2);
        assertNotEqual(buffer1, buffer2);
    }


}