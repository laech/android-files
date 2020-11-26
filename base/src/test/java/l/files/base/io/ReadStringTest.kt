package l.files.base.io

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.US_ASCII
import java.nio.charset.StandardCharsets.UTF_8

class ReadStringTest {

  @Test
  fun readString_single_charset_read_full_string() {
    testReadString("hello", US_ASCII, Int.MAX_VALUE, "hello", UTF_8)
  }

  @Test
  fun readString_single_charset_read_partial_string() {
    testReadString("hello", US_ASCII, 1, "h", UTF_8)
  }

  @Test
  fun readString_single_charset_read_empty_string() {
    testReadString("hello", US_ASCII, 0, "", UTF_8)
  }

  @Test
  fun readString_multi_charset_read_full_string() {
    val src = repeat("a", 8192) + repeat("你", 8192)
    testReadString(src, UTF_8, Int.MAX_VALUE, src, US_ASCII, UTF_8)
  }

  @Test
  fun readString_multi_charset_read_partial_string() {
    val src = repeat("a", 8192) + repeat("你", 8192)
    val expected = repeat("a", 8192) + "你"
    testReadString(src, UTF_8, 8193, expected, US_ASCII, UTF_8)
  }

  @Test
  fun readString_multi_charset_read_empty_string() {
    val src = repeat("a", 1024) + repeat("你", 1024)
    testReadString(src, UTF_8, 0, "", US_ASCII, UTF_8)
  }

  private fun testReadString(
    src: String,
    srcCharset: Charset,
    limit: Int,
    expected: String,
    vararg charsets: Charset
  ) {
    val input = object : ByteArrayInputStream(src.toByteArray(srcCharset)) {
      override fun markSupported() = false
    }
    val actual = readString(input, limit, *charsets)
    assertEquals(expected, actual)
  }

  private fun repeat(s: String, n: Int): String =
    (0 until n).fold(StringBuilder(s.length * n)) { builder, _ ->
      builder.append(s)
    }.toString()
}
