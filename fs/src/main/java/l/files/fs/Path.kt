package l.files.fs

import org.apache.commons.io.FilenameUtils
import java.nio.file.Files.exists
import java.nio.file.Files.isDirectory
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.util.regex.Pattern

/**
 * Returns a file at `dstDir` with the name of `source`, if such
 * file exists, append a number at the end of the file name (and before the
 * extension if it's a regular file until the returned file
 * represents a nonexistent file.
 */
fun getNonExistentDestinationFile(source: Path, dstDir: Path): Path {
  val (base, last) = when {
    isDirectory(source) -> Pair(source.fileName?.toString() ?: "", "")
    else -> Pair(source.baseName ?: "", source.extensionWithLeadingDot ?: "")
  }
  return generateSequence(base) { increment(base) }
    .map { dstDir.resolve(it + last) }
    .first { !exists(it, NOFOLLOW_LINKS) }
}

// TODO match only "* (d+)" otherwise too annoying, and wrong when file
//  is date yyyy-mm-dd or is version e.g. a-2.0
private val NAME_WITH_NUMBER_SUFFIX = Pattern.compile("(.*?\\s*)(\\d+)")

private fun increment(base: String): String {
  val matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base)
  return when {
    matcher.matches() -> matcher.group(1) + try {
      val num = matcher.group(2).toLong()
      when {
        num < Long.MAX_VALUE -> (num + 1).toString()
        else -> matcher.group(2) + " 2"
      }
    } catch (ignored: NumberFormatException) {
      matcher.group(2) + " 2"
    }
    base == "" -> "2"
    else -> "$base 2"
  }
}

/**
 * The name part without extension.
 * ```
 * base.ext  ->  base
 * base      ->  base
 * base.     ->  base.
 * .base.ext  -> .base
 * .base      -> .base
 * .base.     -> .base.
 * .          -> .
 * ..         -> ..
 * ```
 */
private val Path.baseName: String?
  get() {
    val name = fileName?.toString() ?: return null
    return when {
      name.lastIndexOf(".") == 0 -> name
      else -> FilenameUtils.getBaseName(name)
    }
  }

/**
 * The extension part without base name.
 * ```
 * base.ext  ->  ext
 * .base.ext  ->  ext
 * base      ->  ""
 * base.     ->  ""
 * .base      ->  ""
 * .base.     ->  ""
 * .          ->  ""
 * ..         ->  ""
 * ```
 */
private val Path.extension: String?
  get() {
    val name = fileName?.toString() ?: return null
    return (when {
      name.lastIndexOf(".") == 0 -> null
      else -> FilenameUtils.getExtension(name)
    })?.takeIf { it.isNotEmpty() }
  }

private val Path.extensionWithLeadingDot: String?
  get() {
    return ".${extension ?: return null}"
  }
