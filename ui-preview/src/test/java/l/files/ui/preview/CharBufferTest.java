package l.files.ui.preview;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public final class CharBufferTest {

    @Test
    public void construct_from_string_copies_chars() throws Exception {
        String str = "hello";
        CharBuffer buffer = new CharBuffer(str);
        assertEquals("hello", buffer.toString());
        assertEquals(str.length(), buffer.length());
    }

    @Test
    public void auto_expand_buffer_to_fit_input() throws Exception {
        CharBuffer buffer = new CharBuffer(1);
        buffer.append("abc").append("def");
        assertEquals("abcdef", buffer.toString());
    }

    @Test
    public void can_clear_buffer() throws Exception {
        CharBuffer buffer = new CharBuffer("abc");
        buffer.clear();
        assertEquals("", buffer.toString());
        assertEquals(0, buffer.length());
    }

    @Test
    public void copy_returns_exact_copy() throws Exception {
        CharBuffer original = new CharBuffer("abc");
        CharBuffer copy = original.copy();
        assertEquals(original.toString(), copy.toString());
        assertEquals(original.length(), copy.length());
    }

    @Test
    public void copy_does_not_share_internal_buffer() throws Exception {
        CharBuffer original = new CharBuffer("abc");
        CharBuffer copy = original.copy();
        copy.clear().append("d");
        assertEquals("abc", original.toString());
        assertEquals("d", copy.toString());
    }

    @Test
    public void same_hash_code_for_buffers_with_same_content() throws Exception {
        CharBuffer buffer1 = new CharBuffer("abc");
        CharBuffer buffer2 = new CharBuffer("abc");
        assertEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void different_hash_codes_for_buffers_with_different_content() throws Exception {
        CharBuffer buffer1 = new CharBuffer("abc");
        CharBuffer buffer2 = new CharBuffer("ab");
        assertNotEquals(buffer1.hashCode(), buffer2.hashCode());
    }

    @Test
    public void equals_if_buffers_have_same_content() throws Exception {
        CharBuffer buffer1 = new CharBuffer("abc");
        CharBuffer buffer2 = new CharBuffer("abc");
        assertEquals(buffer1, buffer2);
    }

    @Test
    public void not_equal_if_buffers_have_different_content() throws Exception {
        CharBuffer buffer1 = new CharBuffer("abc");
        CharBuffer buffer2 = new CharBuffer("ab");
        assertNotEquals(buffer1, buffer2);
    }


}