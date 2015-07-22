package l.files.fs.local;

import android.system.ErrnoException;
import android.system.Os;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.AlreadyExists;
import l.files.fs.ExceptionHandler;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.NotExist;
import l.files.fs.NotFile;
import l.files.fs.NotLink;
import l.files.fs.Observer;
import l.files.fs.Permission;
import l.files.fs.Resource;
import l.files.fs.Visitor;

import static android.system.OsConstants.EACCES;
import static android.system.OsConstants.EINVAL;
import static android.system.OsConstants.EISDIR;
import static android.system.OsConstants.O_APPEND;
import static android.system.OsConstants.O_CREAT;
import static android.system.OsConstants.O_EXCL;
import static android.system.OsConstants.O_NOFOLLOW;
import static android.system.OsConstants.O_RDONLY;
import static android.system.OsConstants.O_RDWR;
import static android.system.OsConstants.O_TRUNC;
import static android.system.OsConstants.O_WRONLY;
import static android.system.OsConstants.R_OK;
import static android.system.OsConstants.S_IRGRP;
import static android.system.OsConstants.S_IROTH;
import static android.system.OsConstants.S_IRUSR;
import static android.system.OsConstants.S_IRWXU;
import static android.system.OsConstants.S_ISDIR;
import static android.system.OsConstants.S_IWGRP;
import static android.system.OsConstants.S_IWOTH;
import static android.system.OsConstants.S_IWUSR;
import static android.system.OsConstants.S_IXGRP;
import static android.system.OsConstants.S_IXOTH;
import static android.system.OsConstants.S_IXUSR;
import static android.system.OsConstants.W_OK;
import static android.system.OsConstants.X_OK;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;
import static l.files.fs.local.ErrnoExceptions.isCausedByNoFollowLink;
import static l.files.fs.local.ErrnoExceptions.toIOException;

@AutoParcel
public abstract class LocalResource extends Native implements Resource {

  // TODO remove as much custom stuff as possible

  private static final Map<Permission, Integer> permissionBits =
      permissionsToBits();

  private static Map<Permission, Integer> permissionsToBits() {
    Map<Permission, Integer> bits = new HashMap<>();
    bits.put(OWNER_READ, S_IRUSR);
    bits.put(OWNER_WRITE, S_IWUSR);
    bits.put(OWNER_EXECUTE, S_IXUSR);
    bits.put(GROUP_READ, S_IRGRP);
    bits.put(GROUP_WRITE, S_IWGRP);
    bits.put(GROUP_EXECUTE, S_IXGRP);
    bits.put(OTHERS_READ, S_IROTH);
    bits.put(OTHERS_WRITE, S_IWOTH);
    bits.put(OTHERS_EXECUTE, S_IXOTH);
    return unmodifiableMap(bits);
  }

  public static Set<Permission> permissionsFromMode(int mode) {
    Set<Permission> permissions = new HashSet<>();
    if ((mode & S_IRUSR) != 0) permissions.add(OWNER_READ);
    if ((mode & S_IWUSR) != 0) permissions.add(OWNER_WRITE);
    if ((mode & S_IXUSR) != 0) permissions.add(OWNER_EXECUTE);
    if ((mode & S_IRGRP) != 0) permissions.add(GROUP_READ);
    if ((mode & S_IWGRP) != 0) permissions.add(GROUP_WRITE);
    if ((mode & S_IXGRP) != 0) permissions.add(GROUP_EXECUTE);
    if ((mode & S_IROTH) != 0) permissions.add(OTHERS_READ);
    if ((mode & S_IWOTH) != 0) permissions.add(OTHERS_WRITE);
    if ((mode & S_IXOTH) != 0) permissions.add(OTHERS_EXECUTE);
    return unmodifiableSet(permissions);
  }

  private Name name;

  LocalResource() {
  }

  // TODO absolute?
  @Override public abstract File file();

  public static LocalResource create(File file) {
    return new AutoParcel_LocalResource(file);
  }

  private static void ensureIsLocalResource(Resource resource) {
    if (!(resource instanceof LocalResource)) {
      throw new IllegalArgumentException(resource.toString());
    }
  }

  @Override public String toString() {
    return file().getPath();
  }

  @Override public String scheme() {
    return "file";
  }

  @Override public String path() {
    return file().getPath();
  }

  @Override public URI uri() {
    return file().toURI();
  }

  @Override public Name name() {
    if (name == null) {
      name = Name.of(file().getName());
    }
    return name;
  }

  @Override public boolean hidden() {
    return name().length() > 0 && name().charAt(0) == '.';
  }

  @Nullable
  @Override public LocalResource parent() {
    if (isRoot()) {
      return null;
    } else {
      return new AutoParcel_LocalResource(file().getParentFile());
    }
  }

  @Override public boolean isRoot() {
    return "/".equals(path());
  }

  @Override public List<Resource> hierarchy() {
    List<Resource> hierarchy = new ArrayList<>();
    for (Resource p = this; p != null; p = p.parent()) {
      hierarchy.add(p);
    }
    reverse(hierarchy);
    return unmodifiableList(hierarchy);
  }

  @Override public Closeable observe(
      LinkOption option,
      Observer observer) throws IOException {
    return LocalResourceObservable.observe(this, option, observer, null);
  }

  @Override public Closeable observe(
      LinkOption option,
      Observer observer,
      Visitor visitor) throws IOException {
    return LocalResourceObservable.observe(this, option, observer, visitor);
  }

  @Override public boolean startsWith(Resource other) {
    ensureIsLocalResource(other);
    if (other.parent() == null || other.equals(this)) {
      return true;
    }

    String thisPath = path();
    String thatPath = other.path();
    return thisPath.startsWith(thatPath) &&
        thisPath.charAt(thatPath.length()) == '/';
  }

  @Override public LocalResource resolve(String other) {
    return create(new File(file(), other));
  }

  @Override public Resource resolve(Name other) {
    return resolve(other.toString());
  }

  @Override
  public LocalResource resolveParent(Resource fromParent, Resource toParent) {
    ensureIsLocalResource(fromParent);
    ensureIsLocalResource(toParent);
    if (!startsWith(fromParent)) {
      throw new IllegalArgumentException();
    }
    File parent = toParent.file();
    String child = path().substring(fromParent.path().length());
    return new AutoParcel_LocalResource(new File(parent, child));
  }

  @Override public LocalStat stat(LinkOption option) throws IOException {
    return LocalStat.stat(this, option);
  }

  @Override public boolean exists(LinkOption option) throws IOException {
    requireNonNull(option, "option");
    try {
      // access() follows symbolic links
      // faccessat(AT_SYMLINK_NOFOLLOW) doesn't work on android
      // so use stat here
      stat(option);
      return true;
    } catch (NotExist e) {
      return false;
    }
  }

  @Override public boolean readable() throws IOException {
    return accessible(R_OK);
  }

  @Override public boolean writable() throws IOException {
    return accessible(W_OK);
  }

  @Override public boolean executable() throws IOException {
    return accessible(X_OK);
  }

  private boolean accessible(int mode) throws IOException {
    try {
      Os.access(path(), mode);
      return true;
    } catch (ErrnoException e) {
      if (e.errno == EACCES) {
        return false;
      }
      throw toIOException(e, path());
    }
  }

  @Override public void traverse(
      LinkOption option,
      @Nullable Visitor pre,
      @Nullable Visitor post) throws IOException {
    traverse(option, pre, post, new ExceptionHandler() {
      @Override public void handle(Resource resource, IOException e)
          throws IOException {
        throw e;
      }
    });
  }

  @Override public void traverse(
      LinkOption option,
      @Nullable Visitor pre,
      @Nullable Visitor post,
      @Nullable ExceptionHandler handler) throws IOException {
    new LocalResourceTraverser(this, option, pre, post, handler).traverse();
  }

  @Override public void list(
      final LinkOption option,
      final Visitor visitor) throws IOException {
    LocalResourceStream.list(this, option, new LocalResourceStream.Callback() {
      @Override public boolean accept(
          long inode,
          String name,
          boolean directory) throws IOException {
        return visitor.accept(resolve(name)) != TERMINATE;
      }
    });
  }

  @Override public <T extends Collection<? super Resource>> T list(
      final LinkOption option,
      final T collection) throws IOException {
    list(option, new Visitor() {
      @Override public Result accept(Resource resource) throws IOException {
        collection.add(resource);
        return CONTINUE;
      }
    });
    return collection;
  }

  @Override public List<Resource> list(LinkOption option) throws IOException {
    return list(option, new ArrayList<Resource>());
  }

  @Override public InputStream input(LinkOption option) throws IOException {
    requireNonNull(option, "option");

    int flags = O_RDONLY | (option == NOFOLLOW ? O_NOFOLLOW : 0);
    try {
      final FileDescriptor fd = Os.open(path(), flags, 0);
      // Above call allows opening directories, this check ensure this is
      // not a directory
      try {
        if (S_ISDIR(Os.fstat(fd).st_mode)) {
          throw new NotFile(path());
        }
      } catch (Throwable e) {
        Os.close(fd);
        throw e;
      }
      return new FileInputStream(fd) {
        @Override public void close() throws IOException {
          try {
            super.close();
          } finally {
            closeQuietly(fd);
          }
        }
      };

    } catch (ErrnoException e) {
      if (isCausedByNoFollowLink(e, this)) {
        throw new NotFile(path(), e);
      }
      throw toIOException(e, path());
    }
  }

  private void closeQuietly(FileDescriptor fd) {
    try {
      Os.close(fd);
    } catch (ErrnoException e) {
      // Ignore
    }
  }

  @Override public OutputStream output(LinkOption option) throws IOException {
    return output(option, false);
  }

  @Override public OutputStream output(
      LinkOption option,
      boolean append) throws IOException {
    requireNonNull(option, "option");

    // Same default flags and mode as FileOutputStream
    int mode = S_IRUSR | S_IWUSR;
    int flags = O_WRONLY
        | O_CREAT
        | (append ? O_APPEND : O_TRUNC)
        | (option == NOFOLLOW ? O_NOFOLLOW : 0);
    try {
      final FileDescriptor fd = Os.open(path(), flags, mode);
      return new FileOutputStream(fd) {
        @Override public void close() throws IOException {
          try {
            super.close();
          } finally {
            closeQuietly(fd);
          }
        }
      };
    } catch (ErrnoException e) {
      if (isCausedByNoFollowLink(e, this)) {
        throw new NotFile(path(), e);
      }
      if (e.errno == EISDIR) {
        throw new NotFile(path(), e);
      }
      throw toIOException(e, path());
    }
  }

  @Override public Reader reader(
      LinkOption option,
      Charset charset) throws IOException {
    return new InputStreamReader(input(option), charset);
  }

  @Override public Writer writer(
      LinkOption option,
      Charset charset) throws IOException {
    return writer(option, charset, false);
  }

  @Override public Writer writer(
      LinkOption option,
      Charset charset,
      boolean append) throws IOException {
    return new OutputStreamWriter(output(option, append), charset);
  }

  @Override public LocalResource createDirectory() throws IOException {
    try {
      // Same permission bits as java.io.File.mkdir() on Android
      Os.mkdir(path(), S_IRWXU);
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
    return this;
  }

  @Override public LocalResource createDirectories() throws IOException {
    try {
      if (stat(NOFOLLOW).isDirectory()) {
        return this;
      }
    } catch (NotExist ignore) {
      // Ignore will create
    }
    Resource parent = parent();
    if (parent != null) {
      parent.createDirectories();
    }

    try {
      createDirectory();
    } catch (AlreadyExists e) {
      if (!stat(NOFOLLOW).isDirectory()) {
        throw e;
      }
    }

    return this;
  }

  @Override public LocalResource createFile() throws IOException {
    // Same flags and mode as java.io.File.createNewFile() on Android
    int flags = O_RDWR | O_CREAT | O_EXCL;
    int mode = S_IRUSR | S_IWUSR;
    try {
      FileDescriptor fd = Os.open(path(), flags, mode);
      Os.close(fd);
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
    return this;
  }

  @Override public Resource createFiles() throws IOException {
    try {
      if (stat(NOFOLLOW).isRegularFile()) {
        return this;
      }
    } catch (NotExist ignore) {
      // Ignore will create
    }

    Resource parent = parent();
    if (parent != null) {
      parent.createDirectories();
    }
    return createFile();
  }

  @Override
  public LocalResource createLink(Resource target) throws IOException {
    ensureIsLocalResource(target);
    String targetPath = target.path();
    try {
      Os.symlink(targetPath, path());
    } catch (ErrnoException e) {
      throw toIOException(e, path(), targetPath);
    }
    return this;
  }

  @Override public LocalResource readLink() throws IOException {
    try {
      String link = Os.readlink(path());
      return create(new File(link));
    } catch (ErrnoException e) {
      if (e.errno == EINVAL) {
        throw new NotLink(path());
      }
      throw toIOException(e, path());
    }
  }

  @Override public void moveTo(Resource dst) throws IOException {
    ensureIsLocalResource(dst);

    String dstPath = dst.path();
    String srcPath = path();
    try {
      // renameat2() not available on Android
      dst.stat(NOFOLLOW);
      throw new AlreadyExists(dstPath);
    } catch (NotExist e) {
      // Okay
    }
    try {
      Os.rename(srcPath, dstPath);
    } catch (ErrnoException e) {
      throw toIOException(e, srcPath, dstPath);
    }
  }

  @Override public void delete() throws IOException {
    try {
      Os.remove(path());
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
  }

  @Override public void setAccessed(LinkOption option, Instant instant)
      throws IOException {
    requireNonNull(option, "option");
    requireNonNull(instant, "instant");
    try {
      long seconds = instant.seconds();
      int nanos = instant.nanos();
      boolean followLink = option == FOLLOW;
      setAccessTime(path(), seconds, nanos, followLink);
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
  }

  private static native void setAccessTime(
      String path,
      long seconds,
      int nanos,
      boolean followLink) throws ErrnoException;

  @Override public void setModified(
      LinkOption option,
      Instant instant) throws IOException {

    requireNonNull(option, "option");
    requireNonNull(instant, "instant");
    try {
      long seconds = instant.seconds();
      int nanos = instant.nanos();
      boolean followLink = option == FOLLOW;
      setModificationTime(path(), seconds, nanos, followLink);
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
  }

  private static native void setModificationTime(
      String path,
      long seconds,
      int nanos,
      boolean followLink) throws ErrnoException;

  @Override public void setPermissions(Set<Permission> permissions)
      throws IOException {
    int mode = 0;
    for (Permission permission : permissions) {
      mode |= permissionBits.get(permission);
    }
    try {
            /* To change permission on the link itself instead of the target:
             *  - fchmodat(AT_FDCWD, path, mode, AT_SYMLINK_NOFOLLOW)
             * but not implemented.
             */
      Os.chmod(path(), mode);
    } catch (ErrnoException e) {
      throw toIOException(e, path());
    }
  }

  @Override public void removePermissions(Set<Permission> permissions)
      throws IOException {
    Set<Permission> existing = stat(FOLLOW).permissions();
    Set<Permission> perms = new HashSet<>(existing);
    perms.removeAll(permissions);
    setPermissions(perms);
  }

  @Override public String readString(LinkOption option, Charset charset)
      throws IOException {
    return readString(option, charset, new StringBuilder()).toString();
  }

  @Override public <T extends Appendable> T readString(
      LinkOption option,
      Charset charset,
      T appendable) throws IOException {

    try (Reader reader = reader(option, charset)) {
      for (CharBuffer buffer = CharBuffer.allocate(8192);
           reader.read(buffer) > -1; ) {
        buffer.flip();
        appendable.append(buffer);
      }
    }
    return appendable;
  }

  @Override public void writeString(
      LinkOption option,
      Charset charset,
      CharSequence content) throws IOException {
    try (Writer writer = writer(option, charset)) {
      writer.write(content.toString());
    }
  }

}
