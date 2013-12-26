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

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class MainActivity extends Activity 
	implements OnSeekBarChangeListener, OnClickListener {
	private String atcmdBinary;
	private Integer suAvail;
	private SeekBar seekBar;
	private ProgressBar infBar;
	private Button applyButton;
	private TextView logText;
	private int gotoVolume, currentVolume;
	private SharedPreferences pref;
	static final String CURR_VOL_KEY = "current_volume";
	static final String PREF_NAME = "com.github.caifatcmd";
	static final int MAX_VOL = 50;
/*	
	private class SUTask extends AsyncTask<String, Void, Integer> {
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
		@Override
		
		protected void onPreExecute() {
			infBar.setVisibility(View.VISIBLE);
		}
		protected void onPostExecute(Integer res) {
			infBar.setVisibility(View.INVISIBLE);
		}
	}
	*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	suAvail = 0;
    	currentVolume = 0;
    	gotoVolume = 0;
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        logText = (TextView)findViewById(R.id.log_txt);
        applyButton = (Button)findViewById(R.id.apply_button);
		seekBar = (SeekBar)findViewById(R.id.volume_bar);
		infBar = (ProgressBar)findViewById(R.id.sutask_progress);
        
		applyButton.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
        /*applyButton.setEnabled(false);
        seekBar.setEnabled(false);*/
        infBar.setVisibility(View.INVISIBLE);

        pref = getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_MULTI_PROCESS);
		currentVolume = pref.getInt(CURR_VOL_KEY, MAX_VOL);
		seekBar.setProgress(currentVolume);
		logText.setText("Current Volume: " + currentVolume);
		Log.d("CallVol", "Starting with volume " + currentVolume);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    /*
    // Run the AT command
    protected Integer runATCmd(Integer volume) {
    	Integer retVal = 0, vol;
    	// Map to inverted meaning
    	if (volume != 0)
    		vol = MAX_VOL - volume + 1;
    	else
    		vol = 0;
    	
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
*/
	@Override
	public void onProgressChanged(SeekBar bar, int prog, boolean fromUser) {
		if (fromUser) {
			Log.d("CallVolume", "Updating local volume to " + prog);
			gotoVolume = prog;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		Intent changeVol;
		
		changeVol = new Intent(getApplicationContext(), VolumeService.class);
		changeVol.putExtra(CURR_VOL_KEY, gotoVolume);
		//v.setEnabled(false);
		startService(changeVol);
		currentVolume = gotoVolume;
		logText.setText("Current Volume: " + currentVolume);
	}
}
