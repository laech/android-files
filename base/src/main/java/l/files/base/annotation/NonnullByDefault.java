package l.files.base.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierDefault;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PACKAGE;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

// See https://youtrack.jetbrains.com/issue/IDEA-125281

/**
 * This annotation can be applied to a package, class or
 * method to indicate that the class fields, method return
 * types and parameters in that element are not null by default
 * unless there is: The method overrides a method in a
 * superclass (in which case the annotation of the corresponding
 * parameter in the superclass applies) there is a default
 * parameter annotation applied to a more tightly nested element.
 */
@Documented
@Nonnull
@TypeQualifierDefault({
        ANNOTATION_TYPE,
        CONSTRUCTOR,
        FIELD,
        METHOD,
        PACKAGE,
        PARAMETER,
        TYPE
})
@Retention(SOURCE)
public @interface NonnullByDefault {
}
