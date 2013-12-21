package l.files.app.decorator;

import l.files.app.decorator.decoration.Decoration;

import static com.google.common.base.Preconditions.checkNotNull;

abstract class BaseDecorator<T> implements Decorator {

  private final Decoration<T> decoration;

  @SuppressWarnings("unchecked")
  BaseDecorator(Decoration<? extends T> decoration) {
    this.decoration = (Decoration<T>) checkNotNull(decoration, "decoration");
  }

  protected final Decoration<T> decoration() {
    return decoration;
  }
}
