package com.borsche.signalstrength;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.borsche.signalstrength.CollectionActivity.MyThread;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.OnTabChangeListener;

public class NetworkActivity extends TabActivity implements SensorEventListener {

	TabHost mTabHost;
	private ImageButton button_map, button_back;
	private TextView title, mytextview, mytextview2;
	private Handler handler;
	private Cell mycell;
	private WifiInfoManager myWifiInfo;
	private MyThread thread = null;
	private boolean flag=true;
	private String string,string1;
	private boolean mRegisteredSensor;
	// 定义SensorManager
	private SensorManager mSensorManager;
	private float light;
	MediaRecorder mRecorder = null;
	static String mFileName = null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.network);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_back.setOnClickListener(back);
		button_map = (ImageButton) findViewById(R.id.Button_map);
		button_map.setOnClickListener(map);

		title = (TextView) findViewById(R.id.TextView_Title);
		title.setText(R.string.network_name);
		// 取得TabHost对象
		mTabHost = getTabHost();

		/* 为TabHost添加标签 */
		// 新建一个newTabSpec(newTabSpec)
		// 设置其标签和图标(setIndicator)
		// 设置内容(setContent)
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("",
				getResources().getDrawable(R.drawable.network1)).setContent(
						R.id.view1));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("",
				getResources().getDrawable(R.drawable.wifi)).setContent(
						R.id.view2));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("",
				getResources().getDrawable(R.drawable.surrounding)).setContent(
						R.id.view3));

		// 设置TabHost的背景颜色
		mTabHost.setBackgroundColor(Color.DKGRAY);
		// 设置TabHost的背景图片资源
		// mTabHost.setBackgroundResource(R.drawable.bg0);

		// 设置当前显示哪一个标签
		mTabHost.setCurrentTab(0);

		// 标签切换事件处理，setOnTabChangedListener
		mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
			// TODO Auto-generated method stub
			public void onTabChanged(String tabId) {
			
			}

			public void onClick(View v) {
				// TODO Auto-generated method stub

			}

		});
		mycell = new Cell(this);
		myWifiInfo = new WifiInfoManager(this);
		
	
		thread = new MyThread();
		new Thread(thread).start();
		
		
		mRegisteredSensor = false;// 取得SensorManaer实例
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		
		AudioRecordTest();
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		mRecorder.setOutputFile(mFileName);
		try {System.out.println("haha1 ");
			mRecorder.prepare();
			mRecorder.start();
		}  catch (IOException e) {System.out.println("haha2");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	
		

		 
	
	
	}
	public void AudioRecordTest() {
		mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
		mFileName += "/audiorecordtest.3gp";
	}

	final Handler totalHandler = new Handler() { // handle
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				/**处理变化采集持续时间**/
				mycell.start();
				myWifiInfo.dump();
				SetCellInfo();
				SetWifiInfo();
				SetSrdInfo();
			}
			super.handleMessage(msg);
		}

	
	};

	public class MyThread implements Runnable { // thread
		// @Override
		public void exit() {
			flag = false;
			while (!flag);
		}

		public void run() {
			while (flag) {
				try {
					Thread.sleep(1000); // sleep 1000ms
					Message message = new Message();
					message.what = 1;
					 totalHandler.sendMessage(message);
				} catch (Exception e) {
				}
			}
			flag = true;
		}
	}
	


	private void SetCellInfo() {
		mytextview2 = (TextView) findViewById(R.id.SignalStrength);
		String SignalStrength = mycell.getSignalStrength();
		String string = "信号强度： " + SignalStrength;
		mytextview2.setText(string);
		
		mytextview2 = (TextView) findViewById(R.id.nb_SignalStrength);
		String NbSignal = mycell.getNbSignal();
		 string =NbSignal;
		mytextview2.setText(string);
		
		mytextview = (TextView) findViewById(R.id.NetworkType);
		int NetworkType = mycell.getNetworkType();
		NetworkType(NetworkType);
		mytextview.setText("小区类型：" +string1);
		

		mytextview = (TextView) findViewById(R.id.CellId);
		int id = mycell.getCid();
		string = "小区ID:      " + id;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.CellLac);
		int lac = mycell.getLac();
		string = "小区LAC:   " + lac;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.Latitude);
		double latitude = mycell.getLatitude();
		string = "当前纬度: " + latitude;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.Longitude);
		double longitude = mycell.getLongitude();
		string = "当前经度: " + longitude;
		mytextview.setText(string);

		
		mytextview = (TextView) findViewById(R.id.nb_NetworkType);
		int NbType = mycell.getNbType();
		NetworkType(NbType);
		mytextview.setText(string1);
		
		mytextview = (TextView) findViewById(R.id.nb_CellId);
		String Nbcid = mycell.getNbcid();
		string =Nbcid;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.nb_CellLac);
		String Nblac = mycell.getNblac();
		string =Nblac;
		mytextview.setText(string);
		
		
	}
	private void NetworkType(int type) 
	{
		switch (type) {
		case 0:
			string1 = "网络类型未知";
			break;
		case 1:
			string1 = "GPRS网络";
			break;
		case 2:
			string1 = "EDGE网络";
			break;
		case 3:
			string1 = "UMTS网络";
			break;
		case 4:
			string1 = "CDMA网络";
			break;
		case 5:
			string1 = "EVDO网络";
			break;
		case 6:
			string1 = "EVDO-A网络";
			break;
		case 7:
			string1 = "1xRTT网络";
			break;
		case 8:
			string = "HSDPA网络";
			break;
		case 9:
			string1 = "HSUPA网络";
			break;
		case 10:
			string1 = "HSPA网络";
			break;
		case 15:
			string1 = "HSPAP网络";
			break;

		default : string1 ="未知";
		}
	}
	private void SetWifiInfo() {
		
		mytextview = (TextView) findViewById(R.id.WifiSignalStrength);
		int dbm = myWifiInfo.getdBm();
		string = "信号强度: " +dbm+" dBm";
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiSSID);
		String ssid = myWifiInfo.getSSID();
		string = "WLAN网络: " +ssid;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiBSSID);
		String bssid = myWifiInfo.getBSSID();
		string = "接入点地址: " +bssid;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiMAC);
		String mac= myWifiInfo.getMacAddress();
		string = "MAC地址: " +mac;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiSpeed);
		int speed= myWifiInfo.getLinkSpeed();
		string = "链接速度: " +speed+" Mbps";
		mytextview.setText(string);
		
		

	}
	private void SetSrdInfo() {
		mytextview = (TextView) findViewById(R.id.Light);
		string = "光线强度:      " + light+" lux";
		mytextview.setText(string);
		
		
		int x = mRecorder.getMaxAmplitude();//得到当前得到录音时的最大振幅
		if (x != 0) {
			int f = (int) (14.14 * Math.log(x) / Math.log(10));//将振幅转化为分贝值
			mytextview = (TextView) findViewById(R.id.Noise);
			string = "环境噪音:      " + f+"   db";
			mytextview.setText(string);
		}
		
	}
	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			thread.exit();
			mRecorder.stop();
		//	mRecorder.release();
	

			NetworkActivity.this.finish();
		}
	};
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			thread.exit();
			mRecorder.stop();
		//	mRecorder.release();
	
			
			NetworkActivity.this.finish();
				
		}

		return super.onKeyDown(keyCode, event);
	}
	private Button.OnClickListener map = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(NetworkActivity.this, LocationOverlayDemo.class);
			startActivity(intent);
		}
	};
	
	
	protected void onResume() {
		super.onResume();

		// 接受SensorManager的一个列表(Listener)
		// 这里我们指定类型为TYPE_LIGHT(亮度感应器)
		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);

		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			// 注册SensorManager
			// this->接收sensor的实例
			// 接收传感器类型的列表
			// 接受的频率
			mRegisteredSensor = mSensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	protected void onPause() {
		if (mRegisteredSensor) {
			// 如果调用了registerListener
			// 这里我们需要unregisterListener来卸载\取消注册
			mSensorManager.unregisterListener(this);
			mRegisteredSensor = false;
		}
		super.onPause();
	}

	// 当进准度发生改变时
	// sensor->传感器
	// accuracy->精准度
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// 当传感器在被改变时触发
	public void onSensorChanged(SensorEvent event) {
		// 接受亮度感应器的类型
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			// 根据不同的传感器返回值设定亮度
		light=event.values[0];
		
		
		}
	}
}