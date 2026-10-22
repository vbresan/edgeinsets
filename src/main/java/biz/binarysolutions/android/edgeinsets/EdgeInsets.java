package biz.binarysolutions.android.edgeinsets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.MaterialColors;

import java.util.Objects;

public final class EdgeInsets {

    private static final int MIN_SDK = 36;
    private static final String STRIP_TAG = "EdgeInsets.strip";

    private static final class BasePadding {

        final int left;
        final int right;
        final int top;
        final int bottom;

        BasePadding(View root) {

            left   = root.getPaddingLeft();
            right  = root.getPaddingRight();
            top    = root.getPaddingTop();
            bottom = root.getPaddingBottom();
        }
    }

    private EdgeInsets() {}

    private static int getPrimaryColor(View view) {

        int colorId = androidx.appcompat.R.attr.colorPrimary;
        return MaterialColors.getColor(view, colorId);
    }

    private static View createTopStripView(Activity activity, int backgroundColor) {

        View topStrip = new View(activity);
        topStrip.setBackgroundColor(backgroundColor);
        topStrip.setTag(STRIP_TAG);

        return topStrip;
    }

    private static FrameLayout.LayoutParams createTopStripLayoutParams() {

        return new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            0,
            Gravity.TOP
        );
    }

    private static View createSideCutView(Activity activity) {

        View view = new View(activity);
        view.setBackgroundColor(Color.BLACK);

        return view;
    }

    private static FrameLayout.LayoutParams createSideCutLayoutParams(int gravity) {

        return new FrameLayout.LayoutParams(
            0,
            ViewGroup.LayoutParams.MATCH_PARENT,
            gravity
        );
    }

    private static void setWidth(View view, int width) {

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.width != width) {
            layoutParams.width  = width;
            view.requestLayout();
        }
    }

    private static void setHeight(View view, int height) {

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        if (layoutParams.height != height) {
            layoutParams.height = height;
            view.requestLayout();
        }
    }

    /* Gravity.LEFT and RIGHT are the right choice here, since insets are
        physical sides and shouldn't flip in RTL.
     */
    @SuppressLint("RtlHardcoded")
    public static void apply(@NonNull Activity activity) {

        Objects.requireNonNull(activity, "activity must not be null");

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content.getChildCount() == 0) {
            throw new IllegalStateException(
                "EdgeInsets.apply() must be called after setContentView()"
            );
        }

        if (Build.VERSION.SDK_INT < MIN_SDK) {
            return;
        }

        if (content.findViewWithTag(STRIP_TAG) != null) {
            return;   // already applied
        }

        View root  = content.getChildAt(0);
        int  color = getPrimaryColor(root);

        View topStrip = createTopStripView(activity, color);
        content.addView(topStrip, createTopStripLayoutParams());

        View cutLeft = createSideCutView(activity);
        content.addView(cutLeft, createSideCutLayoutParams(Gravity.LEFT));

        View cutRight = createSideCutView(activity);
        content.addView(cutRight, createSideCutLayoutParams(Gravity.RIGHT));

        Window  window  = activity.getWindow();
        View    decor   = window.getDecorView();
        WindowInsetsControllerCompat controller =
            WindowCompat.getInsetsController(window, decor);

        final boolean defaultLightNavigation =
            controller.isAppearanceLightNavigationBars();

        final BasePadding basePadding = new BasePadding(root);

        ViewCompat.setOnApplyWindowInsetsListener(
            root, (view, insets) -> {

                int top = insets.getInsets(Type.statusBars()).top;
                setHeight(topStrip, top);

                int barsMask =
                    Type.navigationBars() | Type.displayCutout() | Type.ime();
                Insets bars = insets.getInsets(barsMask);
                setWidth(cutLeft,  bars.left);
                setWidth(cutRight, bars.right);

                view.setPadding(
                    basePadding.left + bars.left,
                    basePadding.top + top,
                    basePadding.right + bars.right,
                    basePadding.bottom + bars.bottom
                );

                Insets navigation = insets.getInsets(Type.navigationBars());
                boolean sideBar = navigation.left > 0 || navigation.right > 0;
                boolean isLightNavigation = !sideBar && defaultLightNavigation;

                // tested only on emulator, it doesn't have any effect
                controller.setAppearanceLightNavigationBars(isLightNavigation);

                return insets;
            }
        );

        boolean isLightStatusBar = ColorUtils.calculateLuminance(color) > 0.5;
        controller.setAppearanceLightStatusBars(isLightStatusBar);

        window.setNavigationBarContrastEnforced(false);

        ViewCompat.requestApplyInsets(decor);
    }
}
