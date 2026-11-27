package com.markusborg.ui;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/**
 * Base class for screens with the classic action bar.
 *
 * <p>Apps targeting Android 16 (API 36) are always drawn edge-to-edge: the window extends
 * behind the status and navigation bars, and AppCompat's action bar then overlaps the top
 * of the content. This class pads the action bar + content area by the system bar insets,
 * so the layout sits where it did before edge-to-edge, and keeps dark bars behind the
 * system icons like the pre-edge-to-edge look.</p>
 */
public abstract class SystemBarsActivity extends AppCompatActivity {

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        View decor = getWindow().getDecorView();
        View content = findViewById(android.R.id.content);

        // The content keeps the theme's window background; only the system bar areas
        // around it turn dark, with light icons on top.
        content.setBackground(themeWindowBackground());
        decor.setBackgroundColor(Color.BLACK);
        WindowInsetsControllerCompat bars = WindowCompat.getInsetsController(getWindow(), decor);
        bars.setAppearanceLightStatusBars(false);
        bars.setAppearanceLightNavigationBars(false);

        // Pad AppCompat's root (action bar + content) so neither is drawn under a bar.
        View appRoot = findViewById(androidx.appcompat.R.id.decor_content_parent);
        if (appRoot == null) {
            appRoot = content;
        }
        ViewCompat.setOnApplyWindowInsetsListener(appRoot, (v, insets) -> {
            Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        ViewCompat.requestApplyInsets(appRoot);
    }

    private Drawable themeWindowBackground() {
        TypedArray a = getTheme().obtainStyledAttributes(new int[] {android.R.attr.windowBackground});
        try {
            return a.getDrawable(0);
        } finally {
            a.recycle();
        }
    }
}
