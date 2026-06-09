/*
 * Copyright (c) 2020, 2025, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.helloandroid;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.core.view.WindowCompat;

public class DalvikStatusBarService {

    private static final String TAG = Util.TAG;

    private final Activity activity;

    public DalvikStatusBarService(Activity activity) {
        this.activity = activity;
    }

    private void setColor(final int color) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // < 21
            Log.e(TAG, "StatusBar service is not supported for the current Android version");
            return;
        }
        if (activity == null) {
            Log.e(TAG, "FXActivity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        if (Util.isDebug()) {
            Log.v(TAG, "Set StatusBar color, value: " + color);
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = activity.getWindow();
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // <= 34
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    // make status bar transparent
                    window.setStatusBarColor(0x00000000);
                    // paint window
                    window.setBackgroundDrawable(new ColorDrawable(color));
                } else { // >= 35
                    View decorView = window.getDecorView();

                    // Apply color
                    decorView.setBackground(new ColorDrawable(color));
                }
            }
        });
    }

    private void setSystemBarsAppearance(final boolean darkStatusBar, final boolean darkNavigationBar) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // < 21
            Log.e(TAG, "StatusBar service is not supported for the current Android version");
            return;
        }
        if (activity == null) {
            Log.e(TAG, "FXActivity not found. This service is not allowed when "
                    + "running in background mode or from wearable");
            return;
        }

        if (Util.isDebug()) {
            Log.v(TAG, "Set StatusBar appearance: " + (darkStatusBar ? "dark" : "light") + ", NavigationBar appearance: " + (darkNavigationBar ? "dark" : "light"));
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Window window = activity.getWindow();
                View decorView = window.getDecorView();
                WindowCompat.getInsetsController(window, decorView).setAppearanceLightStatusBars(darkStatusBar);
                WindowCompat.getInsetsController(window, decorView).setAppearanceLightNavigationBars(darkNavigationBar);
            }
        });
    }

    public float getNativeStatusBarHeight() {
        int result = 0;
        int resourceId = activity.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return pxToDp(result);
    }

    public float getNativeNavigationBarHeight() {
        int result = 0;
        int resourceId = activity.getResources()
                .getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return pxToDp(result);
    }

    public float pxToDp(int px) {
        return px / activity.getResources().getDisplayMetrics().density;
    }

    public int getNativeSafeInsetLeftPx() {
        return getSafeArea(activity).left;
    }

    public int getNativeSafeInsetTopPx() {
        return getSafeArea(activity).top;
    }

    public int getNativeSafeInsetRightPx() {
        return getSafeArea(activity).right;
    }

    public int getNativeSafeInsetBottomPx() {
        return getSafeArea(activity).bottom;
    }

    public float getNativePxToDp(int px) {
        return pxToDp(px);
    }

    public static Rect getSafeArea(Activity activity) {

        View decor = activity.getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API 30+
            WindowInsets insets = decor.getRootWindowInsets();
            if (insets == null) return new Rect();

            android.graphics.Insets i =
                    insets.getInsets(
                            WindowInsets.Type.systemBars()
                                    | WindowInsets.Type.displayCutout()
                    );

            return new Rect(i.left, i.top, i.right, i.bottom);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28–29
            WindowInsets insets = decor.getRootWindowInsets();
            if (insets == null) return new Rect();

            int left = 0;
            int top = insets.getSystemWindowInsetTop();
            int right = 0;
            int bottom = insets.getSystemWindowInsetBottom();

            DisplayCutout cutout = insets.getDisplayCutout();
            if (cutout != null) {
                left = cutout.getSafeInsetLeft();
                top = Math.max(top, cutout.getSafeInsetTop());
                right = cutout.getSafeInsetRight();
                bottom = Math.max(bottom, cutout.getSafeInsetBottom());
            }

            return new Rect(left, top, right, bottom);

        } else {
            // API 24–27 (NO cutout support)
            WindowInsets insets = decor.getRootWindowInsets();
            if (insets == null) return new Rect();

            return new Rect(
                    insets.getSystemWindowInsetLeft(),
                    insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(),
                    insets.getSystemWindowInsetBottom()
            );
        }
    }

}