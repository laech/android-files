package l.files.animation;

import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.widget.ListView;
import l.files.widget.HeightChangeable;

import java.util.Collection;
import java.util.Map;

import static android.animation.Animator.AnimatorListener;
import static com.google.common.collect.Maps.newHashMap;

public final class Animations {

  private Animations() {
  }

  /**
   * Creates an animation map whose keys are items that are visible on screen
   * and are contained in the collection of items to be removed.
   */
  public static Map<Object, Animator> newRemovalAnimationForVisibleItems(
      Collection<?> itemsToRemove, ListView list) {

    final Map<Object, Animator> animators = newHashMap();

    int first = list.getFirstVisiblePosition();
    int last = list.getLastVisiblePosition();
    for (int i = first; i <= last; i++) {
      Object item = list.getItemAtPosition(i);
      if (itemsToRemove.contains(item)) {
        View view = list.getChildAt(i - first);
        animators.put(item, newListItemRemovalAnimation(view));
      }
    }

    return animators;
  }

  public static AnimatorSet newAnimatorSet(
      Collection<Animator> animators, AnimatorListener listener) {
    AnimatorSet set = new AnimatorSet();
    set.playTogether(animators);
    set.addListener(listener);
    return set;
  }

  /**
   * @param view the item view to animate, must implement {@link HeightChangeable}
   */
  public static Animator newListItemAdditionAnimation(final View view) {
    AnimatorSet animator = new AnimatorSet();
    animator.addListener(setTransparentOnStart(view));
    animator.playSequentially(expand((HeightChangeable) view), fadeIn(view));
    return animator;
  }

  private static AnimatorListenerAdapter setTransparentOnStart(final View view) {
    return new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        view.setAlpha(0);
      }
    };
  }

  private static Animator expand(final HeightChangeable view) {
    ValueAnimator animator = ValueAnimator.ofInt(0, view.getOriginalHeight());
    animator.addUpdateListener(new AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        view.setHeight((Integer) animation.getAnimatedValue());
      }
    });
    animator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        view.resetHeight();
      }
    });
    return animator;
  }

  private static Animator fadeIn(View view) {
    return ObjectAnimator.ofFloat(view, "alpha", 0, 1);
  }

  /**
   * @param view the item view to animate, must implement {@link HeightChangeable}
   */
  public static Animator newListItemRemovalAnimation(final View view) {
    AnimatorSet animator = new AnimatorSet();
    animator.playSequentially(fadeOut(view), collapse((HeightChangeable) view));
    animator.addListener(restoreAlphaOnEnd(view));
    return animator;
  }

  private static AnimatorListenerAdapter restoreAlphaOnEnd(final View view) {
    final float originalAlpha = view.getAlpha();
    return new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        view.setAlpha(originalAlpha);
      }
    };
  }

  private static Animator fadeOut(View view) {
    return ObjectAnimator.ofFloat(view, "alpha", 0);
  }

  private static Animator collapse(final HeightChangeable view) {
    ValueAnimator animator = ValueAnimator.ofInt(view.getOriginalHeight(), 0);
    animator.addUpdateListener(new AnimatorUpdateListener() {
      @Override public void onAnimationUpdate(ValueAnimator animation) {
        view.setHeight((Integer) animation.getAnimatedValue());
      }
    });
    animator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        view.resetHeight();
      }
    });
    return animator;
  }
}
