package io.yeomans.echelon.ui.other;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ScrollingView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jason on 10/8/16.
 */

public final class FlingBehavior extends AppBarLayout.Behavior {
  private static final int TOP_CHILD_FLING_THRESHOLD = 3;
  private boolean isPositive;

  public FlingBehavior() {
  }

  public FlingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
    if (target instanceof ScrollingView) {
      final ScrollingView scrollingView = (ScrollingView) target;
      consumed = velocityY > 0 || scrollingView.computeVerticalScrollOffset() > 0;
    }
    return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
  }

  @Override
  public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    isPositive = dy > 0;
  }
}
