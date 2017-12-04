	

package com.borsche.signalstrength;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TrafficActivity extends Activity {
	private ImageButton button_back;

	private TextView title;
	private TextView textview, mytextview;
	private int historyCount;
	private TrafficStatsManager trafficStatsManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.traffic);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_back.setOnClickListener(back);
		

		title = (TextView) findViewById(R.id.TextView_Title);
		title.setText(R.string.traffic_name);

		trafficStatsManager = new TrafficStatsManager(this);

		SetText();

	};

	private void SetText() {


		mytextview = (TextView) findViewById(R.id.MobileRxBytes);
		long MobileRx = trafficStatsManager.getMobileRxBytes();
		mytextview.setText("手机接口接收数据：" + MobileRx + " KB");

		mytextview = (TextView) findViewById(R.id.MobileTxBytes);
		long MobileTx = trafficStatsManager.getMobileTxBytes();
		mytextview.setText("手机接口发送数据：" + MobileTx + " KB");

		mytextview = (TextView) findViewById(R.id.TotalRxBytes);
		long WifiRx = trafficStatsManager.getTotalRxBytes()- trafficStatsManager.getMobileRxBytes();
		mytextview.setText("Wifi接口接收的数据：" + WifiRx + " KB");

		mytextview = (TextView) findViewById(R.id.TotalTxBytes);
		long WifiTx = trafficStatsManager.getTotalTxBytes()-trafficStatsManager.getMobileTxBytes();;
		mytextview.setText("Wifi接口发送的数据: " + WifiTx + " KB");
		
		
		LinearLayout mProcessLayout=(LinearLayout) findViewById(R.id.ProcessLayout);
		for (int i = 0; i < trafficStatsManager.getProcessSize(); i++) {
   
			int uid=trafficStatsManager.getProcess().get(i).getUid();
			long traffic=trafficStatsManager.getUidRxBytes(uid)+trafficStatsManager.getUidTxBytes(uid);
		
			TextView	mytextview1 = new TextView(this);
			
			Drawable icon= trafficStatsManager.getProcess().get(i).getIcon();
			icon.setBounds(0, 0, 50,50);
			mytextview1.setCompoundDrawables(icon, null,null,null);
			
			mytextview1.setTextSize(15);
			mytextview1.setTextColor(Color.BLACK);
			mytextview1.setText(trafficStatsManager.getProcess().get(i).getAppname()
					+ "  本次所耗流量  " +traffic +  "  KB" );
			mytextview1.setPadding(0, 10, 0, 0);
			mProcessLayout.addView(mytextview1);   
			  

		}

	}

	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			TrafficActivity.this.finish();
		}
	};
	

}