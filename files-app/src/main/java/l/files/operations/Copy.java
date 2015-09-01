package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.logging.Logger;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

final class Copy extends Paste {

  private static final Logger logger = Logger.get(Copy.class);
  private static final int BUFFER_SIZE = 1024 * 8;

  private final AtomicLong copiedByteCount = new AtomicLong();
  private final AtomicInteger copiedItemCount = new AtomicInteger();

  Copy(Collection<? extends Resource> sources, Resource destination) {
    super(sources, destination);
  }

  public int getCopiedItemCount() {
    return copiedItemCount.get();
  }

  public long getCopiedByteCount() {
    return copiedByteCount.get();
  }

  @Override void paste(Resource from, Resource to) throws IOException {
    from.traverse(
        NOFOLLOW,
        copyItems(from, to),
        updateDirectoryTimestamps(from, to),
        recordOnException());
  }

  private Visitor copyItems(final Resource from, final Resource to) {
    return new Visitor() {
      @Override public Result accept(Resource src) throws IOException {
        if (isInterrupted()) return TERMINATE;

        Stat stat = src.stat(NOFOLLOW);
        Resource dst = src.resolveParent(from, to);

        if (stat.isSymbolicLink()) {
          copyLink(src, stat, dst);
        } else if (stat.isDirectory()) {
          createDirectory(stat, dst);
        } else if (stat.isRegularFile()) {
          copyFile(src, stat, dst);
        } else {
          throw new IOException("Not file or directory");
        }

        return CONTINUE;
      }
    };
  }

  private Visitor updateDirectoryTimestamps(final Resource from, final Resource to) {
    return new Visitor() {
      @Override public Result accept(Resource src) throws IOException {
        if (isInterrupted()) return TERMINATE;

        Stat stat = src.stat(NOFOLLOW);
        Resource dst = src.resolveParent(from, to);
        if (stat.isDirectory()) {
          setTimes(stat, dst);
        }

        return CONTINUE;
      }
    };
  }

  private void copyLink(Resource src, Stat stat, Resource dst) throws IOException {
    dst.createLink(src.readLink());
    copiedByteCount.addAndGet(stat.size());
    copiedItemCount.incrementAndGet();
    setTimes(stat, dst);
  }

  private void createDirectory(Stat stat, Resource dst) throws IOException {
    dst.createDirectory();
    copiedByteCount.addAndGet(stat.size());
    copiedItemCount.incrementAndGet();
  }

  private void copyFile(Resource src, Stat stat, Resource dst) throws IOException {
    if (isInterrupted()) return;

    try (InputStream source = src.input();
         OutputStream sink = dst.output()) {
      byte[] buf = new byte[BUFFER_SIZE];
      int n;
      while ((n = source.read(buf)) > 0) {
        sink.write(buf, 0, n);
        copiedByteCount.addAndGet(n);
      }
      copiedItemCount.incrementAndGet();

      setTimes(stat, dst);
    } catch (IOException e) {
      try {
        dst.delete();
      } catch (IOException ex) {
        logger.warn(ex, "Failed to delete path on exception %s", dst);
      }

      if (!(e instanceof ClosedByInterruptException)) {
        throw e;
      }
    }
  }

  private void setTimes(Stat src, Resource dst) {
    try {
      dst.setLastModifiedTime(NOFOLLOW, src.lastModifiedTime());
    } catch (IOException e) {
      logger.debug(e, "Failed to set modification time on %s", dst);
    }

    try {
      dst.setLastAccessedTime(NOFOLLOW, src.lastAccessedTime());
    } catch (IOException e) {
      logger.debug(e, "Failed to set access time on %s", dst);
    }
  }

}
