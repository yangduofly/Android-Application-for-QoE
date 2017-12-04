package com.borsche.signalstrength;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class NetworkStateManager {

	private static NetworkStateManager mInstance = null;
	private NetworkStateManagerBroadcastReceiver mReceiver = null;
	ConnectivityManager mConnectivityManager = null;
	private Context mContext = null;
	//private boolean mInternetConnected = false;
	
	private NetworkStateManager(Context context){
		
		mConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
			
		mReceiver = new NetworkStateManagerBroadcastReceiver();
		mContext = context.getApplicationContext();
		mContext.registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}
	
	public static NetworkStateManager instance(Context context){
		if(mInstance == null){
			mInstance = new NetworkStateManager(context);
		}

		return mInstance;
	}
	
	public boolean isConnectedWithInternet(){
		NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
		if(networkInfo != null){
			return networkInfo.isAvailable();
		}
		
		return false;
	}
	
	public void Close(){
		mContext.unregisterReceiver(mReceiver);
	}
	
	private class NetworkStateManagerBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION) ){
	              NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
	              if (networkInfo != null) {
                     if(networkInfo.isAvailable()){
                    	 Toast.makeText(mContext, R.string.network_connected, Toast.LENGTH_SHORT).show();
                     }else{
                    	 Toast.makeText(mContext, R.string.network_disconnected, Toast.LENGTH_SHORT).show();
                     }
	              }
			}
		}
		
	}
	
	

}
