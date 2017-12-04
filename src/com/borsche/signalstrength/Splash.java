package com.borsche.signalstrength;

	import android.app.Activity;
	import android.content.Intent;
	import android.os.Bundle;
	import android.os.Handler;

	public class Splash extends Activity {
	  
	   /**
	    * ����ʱ��
	    */
	    private final int SPLASH_DISPLAY_LENGHT = 2000;
	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.splash);
	        /**
	         * ʹ��handler������
	         */
	        new Handler().postDelayed(new Runnable(){

	         public void run() {
	             Intent mainIntent = new Intent(Splash.this,SignalStrengthActivity.class);
	             Splash.this.startActivity(mainIntent);
	                 Splash.this.finish();
	         }
	          
	        }, SPLASH_DISPLAY_LENGHT);
	    }
	}

