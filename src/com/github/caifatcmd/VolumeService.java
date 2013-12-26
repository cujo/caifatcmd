/*   
   Copyright 2013 Narendra Acharya

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.github.caifatcmd;

import java.util.List;
import java.util.concurrent.ExecutionException;
import eu.chainfire.libsuperuser.Shell;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class VolumeService extends IntentService {
	SharedPreferences pref;
	private int MAX_VOL = 50;
	private String atcmdBinary;
	private int gotoVolume, currentVolume;
	//private boolean serviceActive = false;

	// Thread calling SU
	static public class SUTask extends AsyncTask<String, Void, Integer> {
		// Below method switches on cmds passed
		// "0": Check su and return 0 or 1
		// any other string: Executed in su shell
		@Override
		protected Integer doInBackground(String... cmds) {
			Integer retVal = 0;
			String prog = "0", vol = "0";
			if (cmds.length == 2) {
				prog = cmds[0];
				vol = cmds[1];
			}
			
			Log.d("SUTask", "Running command: " + prog + ", arg: " + vol);
			if (prog == "0") {
				boolean suAvail = Shell.SU.available();
				if (suAvail) retVal = 1;
			} else {
				// Execute filtered set of commands
				if (vol.matches("[0-9]+")) {
					List<String> shellRet;
					shellRet = Shell.SU.run(prog + " " + vol);
					for (String i : shellRet) {
						Log.d("SUTask", "Return string: " + i);
						if (i.matches("ERROR")) {
							retVal = -1;
						} else if (i.matches("^RX.*")) {
							String[] parts = i.split(":");
							if (parts.length == 3) {
								if (parts[2].startsWith(" "))
									parts[2] = parts[2].substring(1);
								retVal = Math.abs(Integer.parseInt(parts[2]));
							} else
								retVal = -1;
						} else {
							retVal = 0;
						}
					}
				}
			}
			
			return retVal;
		}
	}
	
	public VolumeService() {
		super("CallVolService");
		// TODO Auto-generated constructor stub
	}
	
    // Run the AT command
    protected Integer runATCmd(Integer volume) {
    	Integer retVal = 0, vol;
    	// Map to inverted meaning
    	if (volume != 0)
    		vol = MAX_VOL - volume + 1;
    	else
    		vol = 0;
    	
    	Log.d("CallVolService", "Calling AT command for " + volume);
        SUTask task = new SUTask();
        task.execute(atcmdBinary, Integer.toString(vol));
        try {
        	//while (task.getStatus() != AsyncTask.Status.FINISHED)
			//	TimeUnit.MILLISECONDS.sleep(1);
			retVal = task.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return retVal;
    }


	//@Override
	//public int onStartCommand(Intent intent, int flags, int startId) {
    @Override
    public void onCreate() {
		pref = getApplicationContext().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS);
    	//pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		atcmdBinary = getApplicationInfo().dataDir + "/lib/" + "libfake.so";
		
		// Get current volume
		Integer retVal = 0; // = runATCmd(0);
		currentVolume = MainActivity.MAX_VOL; // - retVal + 1;
        gotoVolume = pref.getInt(MainActivity.CURR_VOL_KEY, currentVolume);
        Log.d("CallVolService", "Stored volume at " + gotoVolume);
        
        if (gotoVolume != currentVolume)
        	retVal = runATCmd(gotoVolume);

        if (retVal >= 0)
        	currentVolume = gotoVolume;
    	pref.edit().putInt(MainActivity.CURR_VOL_KEY, currentVolume).commit();
    	//pref.edit().commit();

        //serviceActive = true;
        //stopSelf();
        
        super.onCreate();
		//return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		pref = getApplicationContext().getSharedPreferences(MainActivity.PREF_NAME, Context.MODE_MULTI_PROCESS);
		// Extract volume request from intent
		gotoVolume = intent.getIntExtra(MainActivity.CURR_VOL_KEY, currentVolume);
		Log.d("CallVolService", "Changing volume to " + currentVolume);
		Integer retVal = runATCmd(gotoVolume);
        if (retVal >= 0) {
        	currentVolume = gotoVolume;
        	pref.edit().putInt(MainActivity.CURR_VOL_KEY, currentVolume).commit();
        	//pref.edit().commit();
        	Log.d("CallVolService", "Volume applied!");
        }
	}
}
