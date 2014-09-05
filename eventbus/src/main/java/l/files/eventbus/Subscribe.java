package l.files.eventbus;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.ThreadMode;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates a method will be receiving events from an {@link EventBus}. If a
 * method is annotated with this annotation, the compiler will validate the
 * method such as ensuring it's name correctly, if validation fails an error
 * will be omitted by the compiler.
 *
 * @see EventBus
 * @see ThreadMode
 */
@Documented
@Target(METHOD)
@Retention(RUNTIME)
public @interface Subscribe {

  /**
   * The {@link ThreadMode} of the event method.
   * Default is {@link ThreadMode#PostThread}.
   */
  ThreadMode value() default ThreadMode.PostThread;

}
