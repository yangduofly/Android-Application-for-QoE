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
	// ����SensorManager
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
		// ȡ��TabHost����
		mTabHost = getTabHost();

		/* ΪTabHost��ӱ�ǩ */
		// �½�һ��newTabSpec(newTabSpec)
		// �������ǩ��ͼ��(setIndicator)
		// ��������(setContent)
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("",
				getResources().getDrawable(R.drawable.network1)).setContent(
						R.id.view1));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("",
				getResources().getDrawable(R.drawable.wifi)).setContent(
						R.id.view2));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("",
				getResources().getDrawable(R.drawable.surrounding)).setContent(
						R.id.view3));

		// ����TabHost�ı�����ɫ
		mTabHost.setBackgroundColor(Color.DKGRAY);
		// ����TabHost�ı���ͼƬ��Դ
		// mTabHost.setBackgroundResource(R.drawable.bg0);

		// ���õ�ǰ��ʾ��һ����ǩ
		mTabHost.setCurrentTab(0);

		// ��ǩ�л��¼�����setOnTabChangedListener
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
		
		
		mRegisteredSensor = false;// ȡ��SensorManaerʵ��
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
				/**����仯�ɼ�����ʱ��**/
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
		String string = "�ź�ǿ�ȣ� " + SignalStrength;
		mytextview2.setText(string);
		
		mytextview2 = (TextView) findViewById(R.id.nb_SignalStrength);
		String NbSignal = mycell.getNbSignal();
		 string =NbSignal;
		mytextview2.setText(string);
		
		mytextview = (TextView) findViewById(R.id.NetworkType);
		int NetworkType = mycell.getNetworkType();
		NetworkType(NetworkType);
		mytextview.setText("С�����ͣ�" +string1);
		

		mytextview = (TextView) findViewById(R.id.CellId);
		int id = mycell.getCid();
		string = "С��ID:      " + id;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.CellLac);
		int lac = mycell.getLac();
		string = "С��LAC:   " + lac;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.Latitude);
		double latitude = mycell.getLatitude();
		string = "��ǰγ��: " + latitude;
		mytextview.setText(string);

		mytextview = (TextView) findViewById(R.id.Longitude);
		double longitude = mycell.getLongitude();
		string = "��ǰ����: " + longitude;
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
			string1 = "��������δ֪";
			break;
		case 1:
			string1 = "GPRS����";
			break;
		case 2:
			string1 = "EDGE����";
			break;
		case 3:
			string1 = "UMTS����";
			break;
		case 4:
			string1 = "CDMA����";
			break;
		case 5:
			string1 = "EVDO����";
			break;
		case 6:
			string1 = "EVDO-A����";
			break;
		case 7:
			string1 = "1xRTT����";
			break;
		case 8:
			string = "HSDPA����";
			break;
		case 9:
			string1 = "HSUPA����";
			break;
		case 10:
			string1 = "HSPA����";
			break;
		case 15:
			string1 = "HSPAP����";
			break;

		default : string1 ="δ֪";
		}
	}
	private void SetWifiInfo() {
		
		mytextview = (TextView) findViewById(R.id.WifiSignalStrength);
		int dbm = myWifiInfo.getdBm();
		string = "�ź�ǿ��: " +dbm+" dBm";
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiSSID);
		String ssid = myWifiInfo.getSSID();
		string = "WLAN����: " +ssid;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiBSSID);
		String bssid = myWifiInfo.getBSSID();
		string = "������ַ: " +bssid;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiMAC);
		String mac= myWifiInfo.getMacAddress();
		string = "MAC��ַ: " +mac;
		mytextview.setText(string);
		
		mytextview = (TextView) findViewById(R.id.WifiSpeed);
		int speed= myWifiInfo.getLinkSpeed();
		string = "�����ٶ�: " +speed+" Mbps";
		mytextview.setText(string);
		
		

	}
	private void SetSrdInfo() {
		mytextview = (TextView) findViewById(R.id.Light);
		string = "����ǿ��:      " + light+" lux";
		mytextview.setText(string);
		
		
		int x = mRecorder.getMaxAmplitude();//�õ���ǰ�õ�¼��ʱ��������
		if (x != 0) {
			int f = (int) (14.14 * Math.log(x) / Math.log(10));//�����ת��Ϊ�ֱ�ֵ
			mytextview = (TextView) findViewById(R.id.Noise);
			string = "��������:      " + f+"   db";
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

		// ����SensorManager��һ���б�(Listener)
		// ��������ָ������ΪTYPE_LIGHT(���ȸ�Ӧ��)
		List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_LIGHT);

		if (sensors.size() > 0) {
			Sensor sensor = sensors.get(0);
			// ע��SensorManager
			// this->����sensor��ʵ��
			// ���մ��������͵��б�
			// ���ܵ�Ƶ��
			mRegisteredSensor = mSensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	protected void onPause() {
		if (mRegisteredSensor) {
			// ���������registerListener
			// ����������ҪunregisterListener��ж��\ȡ��ע��
			mSensorManager.unregisterListener(this);
			mRegisteredSensor = false;
		}
		super.onPause();
	}

	// ����׼�ȷ����ı�ʱ
	// sensor->������
	// accuracy->��׼��
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// ���������ڱ��ı�ʱ����
	public void onSensorChanged(SensorEvent event) {
		// �������ȸ�Ӧ��������
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			// ���ݲ�ͬ�Ĵ���������ֵ�趨����
		light=event.values[0];
		
		
		}
	}
}