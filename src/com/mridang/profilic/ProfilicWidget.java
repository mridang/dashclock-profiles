package com.mridang.profilic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;
import com.mridang.profilic.R;

/*
 * This class is the main class that provides the widget
 */
public class ProfilicWidget extends DashClockExtension {

    /*
     * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
     */
    public void onCreate() {

        super.onCreate();
        Log.d("ProfilicWidget", "Created");
        BugSenseHandler.initAndStartSession(this, "4a2e84b0");

    }

    /*
     * @see
     * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
     * (int)
     */
    @Override
    protected void onUpdateData(int arg0) {

        setUpdateWhenScreenOn(true);

        Log.d("ProfilicWidget", "Getting the currently activated profile");
        ExtensionData edtInformation = new ExtensionData();
        edtInformation.visible(false);

    Object o = getSystemService("profile");
    try {

      Class<?> ProfileManager = Class.forName("android.app.ProfileManager");
      Class<?> Profile = Class.forName("android.app.Profile");
      try {

        Method getActiveProfile = ProfileManager.getDeclaredMethod("getActiveProfile", null);
        Method getName = Profile.getDeclaredMethod("getName", null);
        
        try {
          
          switch (((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode()) {
          case AudioManager.RINGER_MODE_SILENT:
            edtInformation.visible(true);
            edtInformation
                .status(getString(R.string.current_profile)
                    + " \u2014 " + (String) getName
                        .invoke(getActiveProfile
                            .invoke(o)));
            edtInformation.expandedBody(getString(R.string.ringer_silent));
            break;

          case AudioManager.RINGER_MODE_VIBRATE:
            edtInformation.visible(true);
            edtInformation
                .status(getString(R.string.current_profile)
                    + " \u2014 " + (String) getName
                        .invoke(getActiveProfile
                            .invoke(o)));
            edtInformation.expandedBody(getString(R.string.ringer_vibrate));
            break;

          case AudioManager.RINGER_MODE_NORMAL:
            edtInformation.visible(true);
            edtInformation
                .status(getString(R.string.current_profile)
                    + " \u2014 " + (String) getName
                        .invoke(getActiveProfile
                            .invoke(o)));
            edtInformation.expandedBody(getString(R.string.ringer_normal));
            break;
          }

        } catch (IllegalAccessException e) {
          e.printStackTrace();
        } catch (InvocationTargetException e) {
          e.printStackTrace();
        }

      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }     
      
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
      //BugSenseHandler.sendException(e);
    }

        edtInformation.icon(R.drawable.ic_dashclock);
        publishUpdate(edtInformation);
        Log.d("ProfilicWidget", "Done");

    }

    /*
     * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
     */
    public void onDestroy() {

        super.onDestroy();
        Log.d("ProfilicWidget", "Destroyed");
        BugSenseHandler.closeSession(this);

    }

}