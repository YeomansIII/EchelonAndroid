package io.yeomans.echelon.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by jason on 4/1/16.
 */
public class SquareRelativeLayout extends RelativeLayout {
    public SquareRelativeLayout(Context context) {
        super(context);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // simple implementation, this can be done better )
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int squareSize = getMeasuredWidth(); // square size
        int measureSpec = MeasureSpec.makeMeasureSpec(squareSize, MeasureSpec.EXACTLY);
        super.onMeasure(measureSpec, measureSpec); // we should remeasure childrens to fit square
    }
}
