package l.files.common.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMapWithExpectedSize;

/**
 * An adapter that will use the configured {@link Viewer}s for getting views for
 * items.
 * <p/>
 * For example:
 * <pre>
 *   {@code adapter.addViewer(Object.class, objectViewer);}
 *   {@code adapter.addViewer(String.class, stringViewer);}
 * </pre>
 * In this example, the {@code objectViewer} will be used to handle all items
 * that are subtypes of {@code Object}, but not {@link String}s, because we've
 * configured {@code stringViewer} for that. The lookup of the viewer is bottom
 * up from the type hierarchy of the item - if we can't find a viewer to handle
 * the exact type of an item, we try to find one that can handle the interfaces
 * the item directly implements (if the item implements multiple interfaces and
 * there are viewers configured for more than one of those interfaces, one of
 * those viewers will be used at random), if still not found, we repeat for the
 * superclasses of the item's type. If no viewer can be found, an {@link
 * IllegalStateException} will be thrown.
 *
 * @see Viewer
 * @see #addViewer(Class, Viewer)
 */
public abstract class ViewerAdapter extends BaseAdapter {

  private final Map<Class<?>, Viewer<Object>> viewers;
  private final Map<Class<?>, Integer> types;

  public ViewerAdapter() {
    viewers = newHashMapWithExpectedSize(1);
    types = newHashMapWithExpectedSize(1);
  }

  /**
   * Adds a viewer for getting views for items of a the given type, or items
   * that are subtypes of the given type.
   * <p/>
   * This method will override any viewer that has been set for the given type
   * previously.
   */
  @SuppressWarnings("unchecked")
  public <T> void addViewer(Class<? extends T> c, Viewer<? super T> viewer) {
    viewers.put(c, (Viewer<Object>) viewer);
    types.put(c, types.size());
  }

  @Override public int getItemViewType(int position) {
    Object item = getItem(position);
    return types.get(findClass(item));
  }

  @Override public int getViewTypeCount() {
    return types.size();
  }

  @Override public View getView(int position, View view, ViewGroup parent) {
    Object item = getItem(position);
    Viewer<Object> viewer = viewers.get(findClass(item));
    return viewer.getView(item, view, parent);
  }

  @SuppressWarnings("unchecked")
  private <T, E extends T> Class<T> findClass(E item) {
    for (Class<?> c = item.getClass(); c != null; c = c.getSuperclass()) {
      if (viewers.containsKey(c)) {
        return (Class<T>) c;
      }
      for (Class<?> inf : c.getInterfaces()) {
        if (viewers.containsKey(inf)) {
          return (Class<T>) inf;
        }
      }
    }
    throw new IllegalStateException("No viewer found for: " + item);
  }
}
