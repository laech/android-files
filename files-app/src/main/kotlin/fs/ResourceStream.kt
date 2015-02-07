package l.files.fs

import java.io.Closeable

trait ResourceStream<out T : PathEntry> : Stream<T>, Closeable
