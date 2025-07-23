package vn.edu.fpt.musicplayer;

import android.app.Activity;
import androidx.core.view.WindowCompat;

public class EdgeToEdgeHelper {
    public static void enable(Activity activity) {
        WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
    }
}