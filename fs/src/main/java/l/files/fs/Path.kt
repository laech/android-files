package l.files.fs

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.file.OpenOption

fun Path.newBufferedInputStream(): BufferedInputStream =
  newInputStream().buffered()

fun Path.newBufferedOutputStream(vararg options: OpenOption): BufferedOutputStream =
  newOutputStream(*options).buffered()

fun Path.newBufferedDataInputStream() =
  DataInputStream(newBufferedInputStream())

fun Path.newBufferedDataOutputStream(vararg options: OpenOption) =
  DataOutputStream(newBufferedOutputStream(*options))
