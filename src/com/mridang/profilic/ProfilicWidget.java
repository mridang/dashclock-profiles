package com.mridang.profilic;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class ProfilicWidget extends DashClockExtension {

	/* This is the instance of the receiver that deals with profile status */
	private ToggleReceiver objReceiver;

	/*
	 * This class is the receiver for getting hotspot toggle events
	 */
	private class ToggleReceiver extends BroadcastReceiver {

		/*
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context context, Intent intent) {

			onUpdateData(1);

		}

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
	 */
	@Override
	protected void onInitialize(boolean booReconnect) {

		super.onInitialize(booReconnect);

		if (objReceiver != null) {
			try {

				Log.d("ProfilicWidget", "Unregistered any existing status receivers");
				unregisterReceiver(objReceiver);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		objReceiver = new ToggleReceiver();
		registerReceiver(objReceiver, new IntentFilter("android.app.profiles.PROFILES_STATE_CHANGED"));
		Log.d("ProfilicWidget", "Registered the status receiver");

	}

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

		Log.d("ProfilicWidget", "Checking if profiles are on");
		if (Settings.System.getInt(getContentResolver(), "system_profiles_enabled", 1) == 1) { 

			Log.d("ProfilicWidget", "Profiles are activated");
			Object o = getSystemService("profile");
			try {

				Log.d("ProfilicWidget", "Fetching the profile name and ringer mode");
				Class<?> ProfileManager = Class.forName("android.app.ProfileManager");
				Class<?> Profile = Class.forName("android.app.Profile");

				Method getActiveProfile = ProfileManager.getDeclaredMethod("getActiveProfile", null);
				Method getName = Profile.getDeclaredMethod("getName", null);

				switch (((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
					Log.d("ProfilicWidget", "Ringer is off");
					edtInformation.visible(true);
					edtInformation
					.status(getString(R.string.current_profile)
							+ " \u2014 " + (String) getName
							.invoke(getActiveProfile
									.invoke(o)));
					edtInformation.expandedBody(getString(R.string.ringer_silent));
					edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
					break;

				case AudioManager.RINGER_MODE_VIBRATE:
					Log.d("ProfilicWidget", "Vibration is on");
					edtInformation.visible(true);
					edtInformation
					.status(getString(R.string.current_profile)
							+ " \u2014 " + (String) getName
							.invoke(getActiveProfile
									.invoke(o)));
					edtInformation.expandedBody(getString(R.string.ringer_vibrate));
					edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
					break;

				case AudioManager.RINGER_MODE_NORMAL:
					Log.d("ProfilicWidget", "Ringer is normal");
					edtInformation.visible(true);
					edtInformation
					.status(getString(R.string.current_profile)
							+ " \u2014 " + (String) getName
							.invoke(getActiveProfile
									.invoke(o)));
					edtInformation.expandedBody(getString(R.string.ringer_normal));
					edtInformation.clickIntent(new Intent("android.intent.action.PROFILE_PICKER"));
					break;
				}

			} catch (NoSuchMethodException e) {
				Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
			} catch (ClassNotFoundException e) {
				Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e("ProfilicWidget", "Encountered an error", e);
				BugSenseHandler.sendException(e);
			}

		} else {
			Log.d("ProfilicWidget", "Profiles are disabled");
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