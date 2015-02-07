package l.files.fs.local

import java.io.File
import l.files.fs.Path

fun File.toResourcePath(): Path = LocalPath.of(this)

fun String.toResourcePath(): Path = LocalPath.of(File(this))
