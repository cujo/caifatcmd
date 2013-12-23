package com.github.caifatcmd;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        TextView txt = (TextView)findViewById(R.id.status_txt);
        String err = initATIntf();
        
        txt.setText(err);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    // Native AT command interface
    public native String initATIntf();
    public native String sendATCmd(String atCmd);
    
    static {
        System.loadLibrary("at-cmd");
    }
}
