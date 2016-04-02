/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v7.app;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.util.Log;

/*
 * Version of ActionBarDrawerToggle that exposes DrawerArrowDrawable's color methods.
 * Needs to be in android.support.v7.app in order to access a constructor in ActionBarDrawerToggle
 * which currently has package-private access.
 */
public class ColorableActionBarDrawerToggle extends ActionBarDrawerToggle {

    private final DrawerArrowDrawable mSlider;

    /**
     * Construct a new ColorableActionBarDrawerToggle.
     *
     * <p>The given {@link Activity} will be linked to the specified {@link DrawerLayout} and
     * its Actionbar's Up button will be set to a custom drawable.
     * <p>This drawable shows a Hamburger icon when drawer is closed and an arrow when drawer
     * is open. It animates between these two states as the drawer opens.</p>
     *
     * <p>String resources must be provided to describe the open/close drawer actions for
     * accessibility services.</p>
     *
     * @param activity                  The Activity hosting the drawer. Should have an ActionBar.
     * @param drawerLayout              The DrawerLayout to link to the given Activity's ActionBar
     * @param openDrawerContentDescRes  A String resource to describe the "open drawer" action
     *                                  for accessibility
     * @param closeDrawerContentDescRes A String resource to describe the "close drawer" action
     *                                  for accessibility
     */
    public ColorableActionBarDrawerToggle( Activity activity
            , DrawerLayout drawerLayout
            , @StringRes int openDrawerContentDescRes
            , @StringRes int closeDrawerContentDescRes ) {
        this(activity, null, drawerLayout, null, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    /**
     * Construct a new ColorableActionBarDrawerToggle with a Toolbar.
     * <p>
     * The given {@link Activity} will be linked to the specified {@link DrawerLayout} and
     * the Toolbar's navigation icon will be set to a custom drawable. Using this constructor
     * will set Toolbar's navigation click listener to toggle the drawer when it is clicked.
     * <p>
     * This drawable shows a Hamburger icon when drawer is closed and an arrow when drawer
     * is open. It animates between these two states as the drawer opens.
     * <p>
     * String resources must be provided to describe the open/close drawer actions for
     * accessibility services.
     * <p>
     * Please use {@link #ActionBarDrawerToggle(Activity, DrawerLayout, int, int)} if you are
     * setting the Toolbar as the ActionBar of your activity.
     *
     * @param activity                  The Activity hosting the drawer.
     * @param toolbar                   The toolbar to use if you have an independent Toolbar.
     * @param drawerLayout              The DrawerLayout to link to the given Activity's ActionBar
     * @param openDrawerContentDescRes  A String resource to describe the "open drawer" action
     *                                  for accessibility
     * @param closeDrawerContentDescRes A String resource to describe the "close drawer" action
     *                                  for accessibility
     */
    public ColorableActionBarDrawerToggle( Activity activity
            , DrawerLayout drawerLayout
            , Toolbar toolbar
            , @StringRes int openDrawerContentDescRes
            , @StringRes int closeDrawerContentDescRes ) {
        this(activity, toolbar, drawerLayout, null, openDrawerContentDescRes, closeDrawerContentDescRes);
    }

    /**
     * In the future, [Google] can make this constructor public if [they] want to let developers customize
     * the animation.
     *
     * [If they make this constructor public, ColorableActionBarDrawerToggle could be in another package.]
     */
    <T extends DrawerArrowDrawable & DrawerToggle>
    ColorableActionBarDrawerToggle( Activity activity
            , Toolbar toolbar
            , DrawerLayout drawerLayout
            , T slider
            , @StringRes int openDrawerContentDescRes
            , @StringRes int closeDrawerContentDescRes ) {
        super( activity
                , toolbar
                , drawerLayout
                , (slider == null) ? slider = (T)new DrawerArrowDrawableToggle(activity, getActionBarThemedContext(activity, toolbar))
                        : slider
                , openDrawerContentDescRes
                , closeDrawerContentDescRes );

        mSlider = slider;
    }

    /*
     * Condensed (equivalent) version of the code in all the getActionBarThemedContext methods
     * called by ActionBarDrawerToggle constructor as of 23.1.
     */
    private static Context getActionBarThemedContext(Activity activity, Toolbar toolbar) {
        Context context = null;
        if (toolbar != null) {
            context = toolbar.getContext();
        } else if (activity instanceof DelegateProvider) { // Allow the Activity to provide an impl
            final Delegate delegate = ((DelegateProvider) activity).getDrawerToggleDelegate();
            context = delegate.getActionBarThemedContext();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            final ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                context = actionBar.getThemedContext();
            } else {
                context = activity;
            }
        }
        return context;
    }

    /// Expose DrawerArrowDrawable's color methods

    public void setColorFilter(ColorFilter colorFilter) {
        if(mSlider != null) {
            mSlider.setColorFilter(colorFilter);
        }
    }

    public void setColor(@ColorInt int color) {
        if(mSlider != null) {
            mSlider.setColor(color);
        }
    }

    @ColorInt
    public int getColor() {
        if(mSlider != null) {
            return mSlider.getColor();
        }
        return 0;//Color.TRANSPARENT;
    }

    public void setAlpha(int alpha) {
        if(mSlider != null) {
            mSlider.setAlpha(alpha);
        }
    }
}