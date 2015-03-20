package com.mridang.profilic;

import java.lang.reflect.Method;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class ProfilicWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {

		IntentFilter itfIntents = new IntentFilter("android.app.profiles.PROFILES_STATE_CHANGED");
		itfIntents.addAction("android.intent.action.PROFILE_SELECTED");
		itfIntents.addAction("android.intent.action.PROFILE_UPDATED");
		return itfIntents;

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return null;
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int intReason) {

		Log.d(getTag(), "Getting the currently activated profile");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			Log.d(getTag(), "Checking if profiles are on");
			if (Settings.System.getInt(getContentResolver(), "system_profiles_enabled", 1) == 1) {

				Log.d(getTag(), "Profiles are activated");
				@SuppressWarnings("ResourceType") Object o = getSystemService("profile");

				Log.d(getTag(), "Fetching the profile name and ringer mode");
				Class<?> ProfileManager = Class.forName("android.app.ProfileManager");
				Class<?> Profile = Class.forName("android.app.Profile");

				final Method getActiveProfile = ProfileManager.getDeclaredMethod("getActiveProfile");
				final Method getName = Profile.getDeclaredMethod("getName");
				final String strName = (String) getName.invoke(getActiveProfile.invoke(o));

				switch (((AudioManager) getSystemService(Context.AUDIO_SERVICE)).getRingerMode()) {
				case AudioManager.RINGER_MODE_SILENT:
					Log.d(getTag(), "Ringer is off");
					edtInformation.expandedBody(getString(R.string.ringer_silent));
					break;

				case AudioManager.RINGER_MODE_VIBRATE:
					Log.d(getTag(), "Vibration is on");
					edtInformation.expandedBody(getString(R.string.ringer_vibrate));
					break;

				case AudioManager.RINGER_MODE_NORMAL:
					Log.d(getTag(), "Phone is silent");
					edtInformation.expandedBody(getString(R.string.ringer_normal));
					break;
				}

				edtInformation.expandedTitle(getString(R.string.current_profile, strName));
				edtInformation.status(getString(R.string.current_profile, strName));
				edtInformation.clickIntent(new Intent("android.settings.PROFILES_SETTINGS"));
				edtInformation.visible(true);

			} else {
				Log.d(getTag(), "Profiles are disabled");
			}

		} catch (NoSuchMethodException | ClassNotFoundException e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Possibly not using Cyanogenmod", e);
			Toast.makeText(getApplicationContext(), R.string.no_cyanogenmod, Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}