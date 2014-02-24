package com.mridang.profilic;

import java.lang.reflect.Method;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.Uri;
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
	private ProfilesReceiver objProfilesReceiver;

	/*
	 * This class is the receiver for getting hotspot toggle events
	 */
	private class ProfilesReceiver extends BroadcastReceiver {

		/*
		 * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
		 */
		@Override
		public void onReceive(Context ctxContext, Intent ittIntent) {

			onUpdateData(0);

		}

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onInitialize(boolean)
	 */
	@Override
	protected void onInitialize(boolean booReconnect) {

		super.onInitialize(booReconnect);

		if (objProfilesReceiver != null) {
			try {

				Log.d("ProfilicWidget", "Unregistered any existing status receivers");
				unregisterReceiver(objProfilesReceiver);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		IntentFilter itfIntents = new IntentFilter("android.app.profiles.PROFILES_STATE_CHANGED");
		itfIntents.addAction("android.intent.action.PROFILE_SELECTED");
		itfIntents.addAction("android.intent.action.PROFILE_UPDATED");

		objProfilesReceiver = new ProfilesReceiver();
		registerReceiver(objProfilesReceiver, itfIntents);
		Log.d("ProfilicWidget", "Registered the status receiver");

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("ProfilicWidget", "Created");
		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d("ProfilicWidget", "Getting the currently activated profile");
		ExtensionData edtInformation = new ExtensionData();		
		setUpdateWhenScreenOn(false);

		try {

			Log.d("ProfilicWidget", "Checking if profiles are on");
			if (Settings.System.getInt(getContentResolver(), "system_profiles_enabled", 1) == 1) { 

				Log.d("ProfilicWidget", "Profiles are activated");
				Object o = getSystemService("profile");

				Log.d("ProfilicWidget", "Fetching the profile name and ringer mode");
				Class<?> ProfileManager = Class.forName("android.app.ProfileManager");
				Class<?> Profile = Class.forName("android.app.Profile");

				final Method getActiveProfile = ProfileManager.getDeclaredMethod("getActiveProfile");
				final Method getName = Profile.getDeclaredMethod("getName");
				final String strName = (String) getName.invoke(getActiveProfile.invoke(o));

				switch (((AudioManager)getSystemService(Context.AUDIO_SERVICE)).getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
					Log.d("ProfilicWidget", "Ringer is off");
					edtInformation.status(getString(
							R.string.current_profile, strName));
					edtInformation.expandedBody(getString(R.string.ringer_silent));
					break;

				case AudioManager.RINGER_MODE_VIBRATE:
					Log.d("ProfilicWidget", "Vibration is on");
					edtInformation.status(getString(
							R.string.current_profile, strName));
					edtInformation.expandedBody(getString(R.string.ringer_vibrate));
					break;

				case AudioManager.RINGER_MODE_NORMAL:
					Log.d("ProfilicWidget", "Phone is silent");
					edtInformation.status(getString(
							R.string.current_profile, strName));
					edtInformation.expandedBody(getString(R.string.ringer_normal));
					break;
				}

				edtInformation.clickIntent(new Intent("android.settings.PROFILES_SETTINGS"));
				edtInformation.visible(true);

			} else {
				Log.d("ProfilicWidget", "Profiles are disabled");
			}

			if (new Random().nextInt(5) == 0) {

				PackageManager mgrPackages = getApplicationContext().getPackageManager();

				try {

					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);

				} catch (NameNotFoundException e) {

					Integer intExtensions = 0;
					Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
					String strPackage;

					for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {

						strPackage = info.serviceInfo.applicationInfo.packageName;
						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0); 

					}

					if (intExtensions > 1) {

						edtInformation.visible(true);
						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
						setUpdateWhenScreenOn(true);

					}

				}

			} else {
				setUpdateWhenScreenOn(false);
			}

		} catch (NoSuchMethodException e) {
			edtInformation.visible(false);
			Log.e("ProfilicWidget", "Possibly not using Cyanogenmod", e);
			Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
		} catch (ClassNotFoundException e) {
			edtInformation.visible(false);
			Log.e("ProfilicWidget", "Possibly not using Cyanogenmod", e);
			Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e("ProfilicWidget", "Encountered an error", e);
			BugSenseHandler.sendException(e);
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

		if (objProfilesReceiver != null) {

			try {

				Log.d("ProfilicWidget", "Unregistered the status receiver");
				unregisterReceiver(objProfilesReceiver);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Log.d("ProfilicWidget", "Destroyed");
		BugSenseHandler.closeSession(this);

	}

}