package l.files.fs

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

fun Path.newBufferedInputStream(): BufferedInputStream =
  newInputStream().buffered()

fun Path.newBufferedOutputStream(append: Boolean): BufferedOutputStream =
  newOutputStream(append).buffered()

fun Path.newBufferedDataInputStream() =
  DataInputStream(newBufferedInputStream())

fun Path.newBufferedDataOutputStream(append: Boolean) =
  DataOutputStream(newBufferedOutputStream(append))
