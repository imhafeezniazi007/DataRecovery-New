package com.example.datarecovery.utils;

import android.app.Activity;
import android.content.Intent;

import com.example.datarecovery.R;

public class ThemesUtil {

    public static int sTheme = 0;
    public final static int THEME_MATERIAL_LIGHT = 0;
    public final static int THEME_DARK_THEME = 1;
    public final static int THEME_ONE_THEME = 2;
    public final static int THEME_TWO_THEME = 3;
    public final static int THEME_THREE_THEME = 4;
    public final static int THEME_FOUR_THEME = 5;
    public final static int THEME_FIVE_THEME = 6;

    public static void changeToTheme(Activity activity, int theme) {
        sTheme = theme;
        activity.finish();
        activity.startActivity(new Intent(activity, activity.getClass()));
        activity.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    public static void onActivityCreateSetTheme(Activity activity) {
        switch (sTheme) {
            default:
            case THEME_MATERIAL_LIGHT:
                activity.setTheme(R.style.LightTheme);
                break;
            case THEME_DARK_THEME:
                activity.setTheme(R.style.DarkTheme);
                break;
            case THEME_ONE_THEME:
                activity.setTheme(R.style.OneTheme);
                break;
            case THEME_TWO_THEME:
                activity.setTheme(R.style.TwoTheme);
                break;
            case THEME_THREE_THEME:
                activity.setTheme(R.style.ThreeTheme);
                break;
            case THEME_FOUR_THEME:
                activity.setTheme(R.style.FourTheme);
                break;
            case THEME_FIVE_THEME:
                activity.setTheme(R.style.FiveTheme);
                break;
        }
    }
}
