package l.files.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import androidx.annotation.Nullable;

import static java.util.logging.Level.WARNING;

public final class Throwables {

    private static final Logger logger = Logger.getLogger(Throwables.class.getName());

    @Nullable
    private static final Method addSuppressed = getAddSuppressedMethod();

    @Nullable
    private static Method getAddSuppressedMethod() {
        try {
            return Throwable.class.getMethod("addSuppressed", Throwable.class);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private Throwables() {
    }

    public static void addSuppressed(Throwable e, Throwable suppressed) {
        if (addSuppressed != null) {
            try {
                addSuppressed.invoke(e, suppressed);
            } catch (IllegalAccessException err) {
                throw new AssertionError(err);
            } catch (InvocationTargetException err) {
                throw new AssertionError(err);
            }
        } else {
            logger.log(WARNING, "addSuppressed(...)", e);
        }
    }

}
