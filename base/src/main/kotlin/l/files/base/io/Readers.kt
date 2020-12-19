package l.files.base.io

import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction
import java.nio.charset.MalformedInputException
import kotlin.math.min

/**
 * Tries each charset in sequence for decoding the
 * input stream, until one is found that can read at most
 * `limit` number of characters successfully, and
 * the resulting string of the read is returned. Returns
 * null if none is successful.
 */
fun readString(
  input: InputStream,
  limit: Int,
  vararg charsets: Charset,
): String? {
  val buffered = input.buffered()
  buffered.mark(Int.MAX_VALUE)
  for (charset in charsets) {
    try {
      return readStringWithCharset(buffered, limit, charset)
    } catch (ignored: MalformedInputException) {
      buffered.reset()
    }
  }
  return null
}

private fun readStringWithCharset(
  input: InputStream,
  limit: Int,
  charset: Charset,
): String {
  val builder = StringBuilder()
  val reader = InputStreamReader(input, newThrowingDecoder(charset))
  val buffer = CharArray(1024)
  while (true) {
    val len = min(buffer.size, limit - builder.length)
    val count = reader.read(buffer, 0, len)
    if (count == -1) {
      return builder.toString()
    }
    builder.append(buffer, 0, count)
    if (builder.length >= limit) {
      return builder.toString()
    }
  }
}

private fun newThrowingDecoder(charset: Charset): CharsetDecoder =
  charset.newDecoder()
    .onUnmappableCharacter(CodingErrorAction.REPORT)
    .onMalformedInput(CodingErrorAction.REPORT)
