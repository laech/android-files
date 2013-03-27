package com.example.files.animation;

import android.animation.*;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public final class Animations {

  // TODO experimental

  public static Animator listItemRemoval(final View view) {
    AnimatorSet animators = new AnimatorSet();
    animators.addListener(restore(view));
    animators.playSequentially(fade(view), collapse(view));
    return animators;
  }

  private static AnimatorListenerAdapter restore(final View view) {
    final LayoutParams layout = view.getLayoutParams();
    final float originalAlpha = view.getAlpha();
    final int originalHeight = layout.height;

    return new AnimatorListenerAdapter() {
      @Override
      public void onAnimationEnd(Animator animation) {
        view.setAlpha(originalAlpha);
        if (layout.height != originalHeight) {
          layout.height = originalHeight;
          view.requestLayout();
        }
      }
    };
  }

  static Animator fade(final View view) {
    return ObjectAnimator.ofFloat(view, "alpha", 0);
  }

  static Animator collapse(final View view) {
    final LayoutParams layout = view.getLayoutParams();
    ValueAnimator animator = ValueAnimator.ofInt(view.getMeasuredHeight(), 0);
    animator.addUpdateListener(new AnimatorUpdateListener() {
      @Override
      public void onAnimationUpdate(ValueAnimator animation) {
        layout.height = (Integer) animation.getAnimatedValue();
        view.requestLayout();
      }
    });
    return animator;
  }

  private Animations() {
  }
}
