package io.yeomans.echelon.ui.other;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by jason on 10/25/16.
 */

public class PullRevealBehavior<V extends View>
  extends CoordinatorLayout.Behavior<V> {
  /**
   * Default constructor for instantiating a PullRevealBehavior in code.
   */
  public PullRevealBehavior() {
  }

  /**
   * Default constructor for inflating a PullRevealBehavior from layout.
   *
   * @param context The {@link Context}.
   * @param attrs   The {@link AttributeSet}.
   */
  public PullRevealBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    // Extract any custom attributes out
    // preferably prefixed with behavior_ to denote they
    // belong to a behavior
  }
}
