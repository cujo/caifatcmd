package com.github.caifatcmd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import java.lang.reflect.Method;

public class VolumeBroadcastReceiver extends BroadcastReceiver {

	static String TAG="CallVolReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			final int type = extras.getInt("android.media.EXTRA_VOLUME_STREAM_TYPE");
			final int value = extras.getInt("android.media.EXTRA_VOLUME_STREAM_VALUE");
			//Log.d(TAG, "type=" + type + ", value=" + value);
			if (type == 0) {
				final int steps = getMaxSteps();
				final int volume = (int)((float)MainActivity.MAX_VOL * (float)value / (float)steps);
				Log.d(TAG, "value=" + value + "/" + steps + ", volume=" + volume);
				Intent changeVol;
				changeVol = new Intent(context.getApplicationContext(), VolumeService.class);
				changeVol.putExtra(MainActivity.CURR_VOL_KEY, volume);
				context.getApplicationContext().startService(changeVol);
			}
		}
	}

	private int getMaxSteps()
	{
		int steps = 5;
		try {
			Class clazz = null;
			clazz = Class.forName("android.os.SystemProperties");
			Method method = clazz.getDeclaredMethod("getInt", String.class, int.class);
			steps = (int)(Integer)method.invoke(null, "ro.config.vc_call_vol_steps", steps);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}
		return steps;
	}
}

