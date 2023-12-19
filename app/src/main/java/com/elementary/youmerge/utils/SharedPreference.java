package com.elementary.youmerge.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Random;


public class SharedPreference {
    private static final String SHARED_PREFERENCES_KEY = "UserSharedPrefs";

    public static void saveSharedPrefValue(Context mContext, String key, String value) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        Editor edit = userSharedPrefs.edit();
        edit.putString(key, scrambleText(value));
        edit.commit();
    }

    public static void saveSimpleString(Context mContext, String key, String value) {
        try {
            SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
            Editor edit = userSharedPrefs.edit();
            edit.putString(key, value);
            edit.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveBoolean(Context mContext, String key, boolean value) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        Editor edit = userSharedPrefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static String getSimpleString(Context mContext, String key) {
        try {
            SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
            return userSharedPrefs.getString(key, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static boolean getBoolean(Context mContext, String key) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        return userSharedPrefs.getBoolean(key, false);
    }

    public static void saveBoolSharedPrefValue(Context mContext, String key, boolean value) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        Editor edit = userSharedPrefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static void saveInt(Context mContext, String key, int value) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        Editor edit = userSharedPrefs.edit();
        edit.putInt(key, value);
        edit.commit();
    }

    /****************************
     * @param cxt
     * @param key
     * @return
     ****************************/
    public static boolean getBoolSharedPrefValue(Context cxt, String key, boolean defaultValue) {
        SharedPreferences userSharedPrefs = cxt.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        return userSharedPrefs.getBoolean(key, defaultValue);
    }

    public static String getSharedPrefValue(Context mContext, String key) {
        SharedPreferences userSharedPrefs = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        String value = userSharedPrefs.getString(key, null);
        return value;
    }

    public static SharedPreferences getSharedPref(Context cxt) {
        return cxt.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
    }

    public static int getInt(Context cxt, String shared_pref_key, int defaultValue) {
        SharedPreferences userSharedPrefs = cxt.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
        return userSharedPrefs.getInt(shared_pref_key, defaultValue);
    }

    /******************************
     // * @param mContext
     * @return
     *****************************/
//    public static boolean isUserLoggedIn(Context mContext) {
//        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
//        return sharedPreferences.getBoolean(Constants.isLoggedIn, false);
//    }

//    public static UserModel getUserDetails(Context mContext) {
//        String val = getSharedPrefValue(mContext, Constants.USERDEFAULT_USER_DATA);
//        Gson gson = new Gson();
//        UserModel user = gson.fromJson(val, UserModel.class);
//        try {
//            /*JSONObject obj = new JSONObject(val);
//            if (obj.has("result"))
//            {
//                obj = obj.getJSONObject("result");
//                user.getId();
//                user.price    = obj.getDouble("price");
//                user.currency = obj.getString("currency");
//            }*/
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return user;
//    }
//
//    public static void saveLoginDefaults(Context mContext, User jsonData) {
//        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
//        Editor editor = sharedPreferences.edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(jsonData);
//        editor.putString(Constants.USERDEFAULT_USER_DATA, json);
//        editor.putBoolean(Constants.USERDEFAULT_ISLOGGEDIN, true);
//        editor.commit();
//        //ApiManager.getInstance().mUser = UserModel.initWithCurrentDeviceValues(mContext);
//    }

//    public static void logoutDefaults(Context mContext) {
//        //app.mDbHelper.purgeEntireDatabase();
//        SharedPreferences sharedPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_KEY, Activity.MODE_PRIVATE);
//        Editor editor = sharedPreferences.edit();
//        String fcmToken = SharedPreference.unScrambleText(SharedPreference.getSharedPrefValue(mContext, NotificationConfig.FCM_ID));
//        //String languageCode = SharedPreference.unScrambleText(SharedPreference.getSharedPrefValue(mContext, Constants.Pref_Language));
//        //Boolean firstInstall = SharedPreference.getBoolSharedPrefValue(mContext, Constants.App_First_Install, false);
//        editor.clear();
//        editor.apply();
//        editor.putString(NotificationConfig.FCM_ID, scrambleText(fcmToken));
//        //editor.putString(Constants.Pref_Language, scrambleText(languageCode));
//        //editor.putBoolean(Constants.App_First_Install, firstInstall);
//        editor.commit();
//    }
    private static String scrambleText(String text) {
        try {
            Random r = new Random();
            String prefix = String.valueOf(r.nextInt(90000) + 10000);
            String suffix = String.valueOf(r.nextInt(90000) + 10000);
            text = prefix + text + suffix;

            byte[] bytes = text.getBytes("UTF-8");
            byte[] newBytes = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                newBytes[i] = (byte) (bytes[i] - 1);
            }
            return new String(newBytes, "UTF-8");
        } catch (Exception e) {
            return text;
        }
    }

    public static String unScrambleText(String text) {
        try {
            byte[] bytes = text.getBytes("UTF-8");
            byte[] newBytes = new byte[bytes.length];
            for (int i = 0; i < bytes.length; i++) {
                newBytes[i] = (byte) (bytes[i] + 1);
            }
            String textVal = new String(newBytes, "UTF-8");
            return textVal.substring(5, textVal.length() - 5);
        } catch (Exception e) {
            return text;
        }
    }
}
