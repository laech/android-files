package l.files.base.text

import java.math.BigInteger
import java.text.CollationKey
import java.text.Collator

private val segmentPattern = "(\\d+|\\s+|[^\\d\\s]+)".toRegex()

/**
 * Like a locale sensitive [java.text.CollationKey] but performs more
 * natural sorting for numbers.
 *
 * This provides more meaning to humans, for example if you have a list of book
 * chapters to be sorted:
 *
 * ```
 * 1. Introduction
 * 1.1. xxx
 * 1.2. yyy
 * ...
 * 1.9. aaa
 * 1.10. bbb
 * ```
 *
 * This class will sort them as listed above.
 */
class Collation private constructor(
  private val segments: List<Segment>,
) : Comparable<Collation> {

  override fun compareTo(other: Collation) = segments.asSequence()
    .zip(other.segments.asSequence(), Segment::compareTo)
    .firstOrNull { it != 0 }
    ?: segments.size.compareTo(other.segments.size)

  companion object {
    @JvmStatic
    fun create(collator: Collator, value: String): Collation = segmentPattern
      .findAll(value)
      .map {
        val key = collator.getCollationKey(it.value)
        if (it.value.first().isDigit()) {
          Segment.OfInt(key, BigInteger(it.value))
        } else {
          Segment.OfText(key)
        }
      }
      .toList()
      .let(::Collation)
  }
}

private sealed class Segment(val key: CollationKey) : Comparable<Segment> {
  class OfInt(key: CollationKey, val value: BigInteger) : Segment(key)
  class OfText(key: CollationKey) : Segment(key)

  override fun compareTo(other: Segment) = when {
    this is OfInt && other is OfInt -> value.compareTo(other.value)
    else -> key.compareTo(other.key)
  }
}

private fun Char.isDigit(): Boolean = this in '0'..'9'
