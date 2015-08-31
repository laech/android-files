package l.files.fs;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import auto.parcel.AutoParcel;

/**
 * Represents a file system resource, such as a file or directory.
 */
public interface Resource extends Parcelable {

  /**
   * @deprecated slow
   */
  @Deprecated URI uri();

  /**
   * The scheme of the file system.
   * e.g. "file" for local file system.
   */
  String scheme();

  @Nullable File file();

  /**
   * Gets the path of this resource. The returned path is only valid within
   * the context of the underlying file system.
   */
  String path();

  /**
   * Gets the name of this resource, or empty if this is the root resource.
   */
  Name name();

  /**
   * Gets the parent resource, returns null if this is the root resource.
   */
  @Nullable Resource parent();

  boolean isRoot();

  /**
   * Gets the resource hierarchy of this resource.
   * <p/>
   * e.g. {@code "/a/b" -> ["/", "/a", "/a/b"]}
   */
  List<Resource> hierarchy();

  /**
   * Resolves the given name/path relative to this resource.
   */
  Resource resolve(String other);

  /**
   * Resolves a child with the given name.
   */
  Resource resolve(Name other);

  /**
   * Returns a resource with the given parent replaced.
   * <p/>
   * e.g.
   * <pre>
   * Resource("/a/b").resolve(Resource("/a"), Resource("/c")) =
   * Resource("/c/b")
   * </pre>
   *
   * @throws IllegalArgumentException if {@code !this.startsWith(fromParent)}
   */
  Resource resolveParent(Resource fromParent, Resource toParent);

  /**
   * True if this resource is equal to or a descendant of the given resource.
   */
  boolean startsWith(Resource prefix);

  /**
   * True if this resource is considered a hidden resource.
   */
  boolean hidden();

  boolean exists(LinkOption option) throws IOException;

  /**
   * Returns true if this resource is readable, return false if not.
   * <p/>
   * If this is a link, returns the result for the link target, not the link
   * itself.
   */
  boolean readable() throws IOException;

  /**
   * Returns true if this resource is writable, return false if not.
   * <p/>
   * If this is a link, returns the result for the link target, not the link
   * itself.
   */
  boolean writable() throws IOException;

  /**
   * Returns true if this resource is executable, return false if not.
   * <p/>
   * If this is a link, returns the result for the link target, not the link
   * itself.
   */
  boolean executable() throws IOException;

  /**
   * Observes on this resource for change events.
   * <p/>
   * If this resource is a directory, adding/removing immediate children and
   * any changes to the content/attributes of immediate children of this
   * directory will be notified, this is true for existing children as well as
   * newly added items after observation started.
   * <p/>
   * Note that by the time a listener is notified, the target resource may
   * have already be changed again, therefore a robust application should have
   * an alternative way of handling instead of reply on this fully.
   *
   * @param option if option is {@link LinkOption#NOFOLLOW} and this resource is a
   *               link, observe on the link instead of the link target
   */
  Closeable observe(LinkOption option, Observer observer)
      throws IOException;

  Closeable observe(LinkOption option, Observer observer, Visitor visitor)
      throws IOException;

  /**
   * Performs a traversal that will terminate as soon as an error is
   * encountered.
   */
  void traverse(
      LinkOption option,
      @Nullable Visitor pre,
      @Nullable Visitor post) throws IOException;

  /**
   * Performs a depth first traverse of this tree.
   * <p/>
   * e.g. traversing the follow tree:
   * <pre>
   *     a
   *    / \
   *   b   c
   * </pre>
   * will generate:
   * <pre>
   * pre.accept(a)
   * pre.accept(b)
   * post.accept(b)
   * pre.accept(c)
   * post.accept(c)
   * post.accept(a)
   * </pre>
   *
   * @param option  applies to root only, child links are never followed
   * @param pre     callback for pre order traversals
   * @param post    callback for post order traversals
   * @param handler handles any exception encountered duration traversal
   */
  void traverse(
      LinkOption option,
      @Nullable Visitor pre,
      @Nullable Visitor post,
      @Nullable ExceptionHandler handler) throws IOException;

  /**
   * Lists the immediate children of this resource.
   */
  void list(LinkOption option, Visitor visitor) throws IOException;

  /**
   * List the children into the given collection. Returns the collection.
   */
  <T extends Collection<? super Resource>> T list(
      LinkOption option, T collection) throws IOException;

  List<Resource> list(LinkOption option) throws IOException;

  InputStream input() throws IOException;

  OutputStream output() throws IOException;

  OutputStream output(boolean append) throws IOException;

  Reader reader(Charset charset) throws IOException;

  Writer writer(Charset charset) throws IOException;

  Writer writer(Charset charset, boolean append) throws IOException;

  /**
   * Creates this resource as a directory. Will fail if the directory already
   * exists.
   *
   * @return this
   */
  Resource createDirectory() throws IOException;

  /**
   * Creates this resource and any missing parents as directories. This will
   * throw the same exceptions as {@link #createDirectory()} except will not
   * error if already exists as a directory.
   *
   * @return this
   */
  Resource createDirectories() throws IOException;

  /**
   * Creates the underlying resource as a file.
   *
   * @return this
   */
  Resource createFile() throws IOException;

  /**
   * Creates this resource as a file and creates any missing parents. This
   * will throw the same exceptions as {@link #createFile()} except will not
   * error if already exists.
   *
   * @return this
   */
  Resource createFiles() throws IOException;

  /**
   * Creates the underlying resource as a link to point to the given
   * location.
   *
   * @return this
   */
  Resource createLink(Resource target) throws IOException;

  /**
   * If this is a link, returns the target resource.
   */
  Resource readLink() throws IOException;

  /**
   * Reads the status of this resource.
   */
  Stat stat(LinkOption option) throws IOException;

  /**
   * Moves this resource tree to the given destination, destination must not
   * exist.
   * <p/>
   * If this is a link, the link itself is moved, link target resource is
   * unaffected.
   */
  void moveTo(Resource dst) throws IOException;

  /**
   * Deletes this resource. Fails if this is a non-empty directory.
   */
  void delete() throws IOException;

  /**
   * Updates the access time for this resource.
   */
  void setAccessed(LinkOption option, Instant instant) throws IOException;

  /**
   * Updates the modification time for this resource.
   */
  void setModified(LinkOption option, Instant instant) throws IOException;

  /**
   * Sets the permissions of this resource, this replaces the existing
   * permissions, not add.
   * <p/>
   * If this is a link, the permission of the target resource will be changed,
   * the permission of the link itself cannot be changed.
   */
  void setPermissions(Set<Permission> permissions) throws IOException;

  /**
   * Removes the given permissions from this resource's existing permissions.
   * <p/>
   * If this is a link, the permission of the target resource will be changed,
   * the permission of the link itself cannot be changed.
   */
  void removePermissions(Set<Permission> permissions) throws IOException;

  /**
   * Reads the underlying file content as string.
   */
  String readString(Charset charset) throws IOException;

  /**
   * Appends the underlying file content as string into the given appendable,
   * returns the appendable.
   */
  <T extends Appendable> T readString(Charset charset, T appendable) throws IOException;

  /**
   * Overrides the content of this resource with the given content.
   */
  void writeString(Charset charset, CharSequence content) throws IOException;

  @AutoParcel
  abstract class Name implements CharSequence {
    Name() {
    }

    abstract String value();

    public static Name of(String name) {
      return new AutoParcel_Resource_Name(name);
    }

    public static Name empty() {
      return of("");
    }

    /**
     * Locale sensitive name comparator.
     */
    public static Comparator<Name> comparator(Locale locale) {
      final Collator collator = Collator.getInstance(locale);
      return new Comparator<Name>() {
        @Override public int compare(Name a, Name b) {
          return collator.compare(a.toString(), b.toString());
        }
      };
    }

    private int indexOfExtSeparator() {
      int i = value().lastIndexOf('.');
      return (i == -1 || i == 0 || i == length() - 1) ? -1 : i;
    }

    /**
     * The name part without extension.
     * <pre>
     *  base.ext  ->  base
     *  base      ->  base
     *  base.     ->  base.
     * .base.ext  -> .base
     * .base      -> .base
     * .base.     -> .base.
     * .          -> .
     * ..         -> ..
     * </pre>
     */
    public String base() {
      int i = indexOfExtSeparator();
      return i != -1 ? value().substring(0, i) : value();
    }

    /**
     * The extension part without base name.
     * <pre>
     *  base.ext  ->  ext
     * .base.ext  ->  ext
     *  base      ->  ""
     *  base.     ->  ""
     * .base      ->  ""
     * .base.     ->  ""
     * .          ->  ""
     * ..         ->  ""
     * </pre>
     */
    public String ext() {
      int i = indexOfExtSeparator();
      return i != -1 ? value().substring(i + 1) : "";
    }

    /**
     * {@link #ext()} with a leading dot if it's not empty.
     */
    public String dotExt() {
      String ext = ext();
      return ext.isEmpty() ? ext : "." + ext;
    }

    @Override public int length() {
      return value().length();
    }

    @Override public char charAt(int index) {
      return value().charAt(index);
    }

    @Override public CharSequence subSequence(int start, int end) {
      return value().subSequence(start, end);
    }

    @Override public String toString() {
      return value();
    }
  }
}
