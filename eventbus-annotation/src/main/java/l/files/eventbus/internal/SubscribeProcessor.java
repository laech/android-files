package l.files.eventbus.internal;

import com.google.auto.service.AutoService;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

import de.greenrobot.event.ThreadMode;
import l.files.eventbus.Subscribe;

import static java.util.Collections.singleton;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Processor for {@link Subscribe} for validating the annotated method
 * declaration.
 */
@AutoService(Processor.class)
public final class SubscribeProcessor extends AbstractProcessor {

  @Override public Set<String> getSupportedAnnotationTypes() {
    return singleton(Subscribe.class.getName());
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment round) {
    boolean claimed = annotations.size() == 1 &&
        annotations.iterator().next().getQualifiedName()
            .toString().equals(Subscribe.class.getName());
    if (claimed) {
      process(round);
    }
    return claimed;
  }

  private void process(RoundEnvironment round) {
    Set<ExecutableElement> methods = ElementFilter.methodsIn(
        round.getElementsAnnotatedWith(Subscribe.class));
    for (ExecutableElement method : methods) {
      Subscribe annotation = method.getAnnotation(Subscribe.class);
      validateMethodName(method, annotation.value());
      validateMethodArgument(method);
    }
  }

  private void validateMethodName(ExecutableElement method, ThreadMode mode) {
    String expectedName = "onEvent";
    if (!ThreadMode.PostThread.equals(mode)) {
      expectedName += mode;
    }
    if (!method.getSimpleName().toString().equals(expectedName)) {
      processingEnv.getMessager().printMessage(ERROR,
          "Method annotated with @Subscribe(ThreadMode." + mode + ") " +
              "must be called \"" + expectedName + "\" " +
              "for event notification to work.", method);
    }
  }

  private void validateMethodArgument(ExecutableElement method) {
    if (method.getParameters().size() != 1) {
      processingEnv.getMessager().printMessage(ERROR,
          "Subscription method should have exactly one argument.", method);
    }
  }

}
