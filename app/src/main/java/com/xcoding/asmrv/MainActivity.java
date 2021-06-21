package com.xcoding.asmrv;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {



    //variables declaration
    WebView webView;
    Activity activity ;
    private ProgressDialog _progDailog;
    SharedPreferences myPref;
    SharedPreferences.Editor editorPref;
    String _timer_interval;
    EditText minute_interval;
    public Handler handler=null;
    public static Runnable runnable=null;
    int controller;
    private Timer _timer;
    private TimerTask _timeOut;
    int cDown=0;

    //-----------------------------------

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //we keep the screen on until app closes
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //--------------------------------------

        //load youtube on web view
        activity=this;
        _progDailog = ProgressDialog.show(activity, "Loading","Please wait...", true);
        _progDailog.setCancelable(false);

        webView = (WebView) findViewById(R.id.webViewYT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                _progDailog.show();
                view.loadUrl(url);

                return true;
            }
            @Override
            public void onPageFinished(WebView view, final String url) {
                _progDailog.dismiss();
            }
        });

        webView.loadUrl("https://www.youtube.com/results?search_query=asmr");
        //---------------------------------------


        //reading interval timer from shared pref from last session and set on textview
        minute_interval = findViewById(R.id.minute_text);
        myPref = getSharedPreferences("prefID", Context.MODE_PRIVATE);
        _timer_interval= myPref.getString("time_str","");
        Log.d("c","load_timer_data: "+_timer_interval);
        minute_interval.setText(_timer_interval);
        //-----------------------------------------------------


        //Start timer button
        Button start_button = findViewById(R.id  .start_button);
        start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _timer_interval = minute_interval.getText().toString();
                int inter = Integer.parseInt(_timer_interval);
                if (inter >= 1) {
                    if(controller==0) {
                        editorPref = myPref.edit();
                        editorPref.putString("time_str", _timer_interval); //we save the interval in shared pref for future use
                        editorPref.apply();
                        Toast.makeText(MainActivity.this, "Timer is set for: " + _timer_interval + " minutes and started!", Toast.LENGTH_LONG).show();
                        _onStartTimer(inter);
                        controller = 1;
                    }else{
                        Toast.makeText(MainActivity.this, "Timer is already running!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Timer interval must be bigger than 1 minute!", Toast.LENGTH_LONG).show();
                }
            }
        });
        //-----------------------------------------------------

        //Stop timer button
        Button stop_button = findViewById(R.id.stop_button);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (controller == 1) {
                    handler.removeCallbacks(runnable); // we stop the timer
                    Toast.makeText(MainActivity.this, "Timer was stopped!", Toast.LENGTH_LONG).show();
                    Button start_button = findViewById(R.id  .start_button);
                    start_button.setText("START");
                    controller=0;
                }else{
                    Toast.makeText(MainActivity.this, "Timer is not started!", Toast.LENGTH_LONG).show();
                }
            }
        });
        //-----------------------------------------------------

    }

        //timer function for count down
    private void _onStartTimer(int interval){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cDown = interval + 1;
                handler= new Handler();
                int finalInterval = interval*60000;
                runnable = new Runnable() {
                    @SuppressLint("InvalidWakeLockTag")
                    @Override
                    public void run() {
                        //handler.postDelayed(runnable, 1);
                        handler.removeCallbacks(runnable);
                        Toast.makeText(MainActivity.this, "ASMRV has stopped!", Toast.LENGTH_LONG).show();
                        PowerManager pm =  (PowerManager)getSystemService(Context.POWER_SERVICE);
                        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"My Tag");
                        wl.acquire();
                        System.exit(0);
                    }
                };
                handler.postDelayed(runnable, finalInterval);
                _timer= new Timer();
                Button start_button = findViewById(R.id  .start_button);
                _timeOut = new TimerTask() {
                    @Override
                    public void run() {
                        cDown--;
                        String cD= String.valueOf(cDown);
                        start_button.setText(cD);
                        if(cDown==0){
                            _timer.cancel();
                            start_button.setText("START");
                        }
                    }
                };
                _timer.scheduleAtFixedRate(_timeOut,0,60000);
            }
        });
    }
    //--------------------------
}