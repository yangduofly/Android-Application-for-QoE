package com.borsche.signalstrength;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


//import com.yangduo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class CollectionActivity extends Activity implements Observer {

	private ImageButton button_back;
	private TextView title;
	private Logger logger;
	private Timer timer;
	public MyThread thread = null;
	boolean flag;// = true;
	boolean flag_start = true;
	private QoeDataManager mQoeDataManager = null;
	private static ConfigureLog4j configLog4j = new ConfigureLog4j();

	private int mComplaintType = 0;

	private final String DEFAULT_SERVER_IP_ADDRESS = "222.128.13.73";
	private final int DEFAULT_SERVER_PORT = 8088;
	private final int DEFAULT_PERIOD = 15 ;//* 60;
	private String mServerIpAddress = DEFAULT_SERVER_IP_ADDRESS;
	private int mServerPort = DEFAULT_SERVER_PORT;
	private int mPeriod = DEFAULT_PERIOD;
	private int historyCount, Count;
	private int recLen = 0;
	private TextView textView1;
	private TextView textView2;
	private TextView textView3;
	private TextView textView4;
	private TextView textView5;
	private TextView textView6;
	
	public SharedPreferences myShare = null;
	public final static String MYSAHRE_MAIN = "myshare";
	public final static String TEXT_MIN = "textmin";
	public final static String TEXT_MAX = "textmax";
    public static String TEXTMIN_DEFAULT = "10";
    public static String TEXTMAX_DEFAULT = "60";

	private Button button_start;
	private Button button_stop;
	private Button button_delete;
	private  SharedPreferences count;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.collection);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_start = (Button) findViewById(R.id.Button_Start);

		button_back.setOnClickListener(back);
		button_start.setOnClickListener(starting);
		
		button_delete = (Button) findViewById(R.id.Button_Delete);
		button_delete.setOnClickListener(delete);

		title = (TextView) findViewById(R.id.TextView_Title);
		title.setText(R.string.collection_name);

		textView1 = (TextView) findViewById(R.id.starttime);
		textView2 = (TextView) findViewById(R.id.holdingtime);
		textView3 = (TextView) findViewById(R.id.mincollecttime);
		textView4 = (TextView) findViewById(R.id.maxcollecttime);
		textView5 = (TextView) findViewById(R.id.sendcount);
		textView6 = (TextView) findViewById(R.id.historycount);
		count = getSharedPreferences("sendCount",
				Context.MODE_WORLD_READABLE);
		historyCount = count.getInt("sendCount", 0);
		textView6.setText(historyCount + "��");
		
		myShare = getSharedPreferences(MYSAHRE_MAIN,  Context.MODE_WORLD_READABLE); 
		CellLocationManager.DELAY_TIME = myShare.getString(TEXT_MAX,TEXTMAX_DEFAULT );

	};

	private Button.OnClickListener starting = new Button.OnClickListener() {

		public void onClick(View v) {

			onCollect(flag_start);
	
			if (flag_start) {
				button_start.setText("ֹͣ");
			} else {
				button_start.setText("��ʼ");
			}
			flag_start = !flag_start;
		}

		  /*�ɼ�����*/
		private void onCollect(boolean start) {
			   /*���ɼ�����ʼ��ʱ���Լ��ɼ���������Ϣ*/
			if (start) {
				flag = true;
				autopost();
				// TODO Auto-generated method stub
				recLen = 0;

				thread = new MyThread();

				new Thread(thread).start();
				/*��ӡ�ɼ���ʼʱ��*/
				SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd   HH:mm:ss  a");
				String date = sDateFormat.format(new java.util.Date());
				textView1.setText(date);
				/*��ӡ��С�����ɼ�ʱ����*/
				String timemin = myShare.getString(TEXT_MIN,TEXTMIN_DEFAULT );
				String timemax = CellLocationManager.DELAY_TIME;
				textView3.setText(timemin + "��");
				textView4.setText(timemax + "��");
				
			}
			else {
				if (thread != null) {
					// autostop();
					mQoeDataManager.stop();
					mQoeDataManager = null;
					thread.exit();
					SharedPreferences usState = getSharedPreferences(
							"sendCount", Context.MODE_WORLD_READABLE);
					SharedPreferences.Editor editor = usState.edit();
					historyCount = historyCount + Count;
					editor.putInt("sendCount", historyCount);
					editor.commit();
					Count = 0;
					textView6.setText(historyCount + "��");
				}

			}
		}
	};

	private Button.OnClickListener delete = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Editor editor = count.edit();
			editor.remove("sendCount");
			editor.commit();
			historyCount = count.getInt("sendCount", 0);
			textView6.setText(historyCount + "��");
		}
	};

	final Handler handler = new Handler() { // handle
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				/**����仯�ɼ�����ʱ��**/
				recLen++;
				int recLen_h,
				recLen_hy,
				recLen_m,
				recLen_s,
				count;
				count = 0;
				recLen_h = recLen / 3600;
				recLen_hy = recLen % 3600;
				recLen_m = recLen_hy / 60;
				recLen_s = recLen_hy % 60;
				/*��ӡ�ɼ�����ʱ��*/
				textView2.setText(recLen_h + "ʱ" + recLen_m + "��" + recLen_s
						+ "��");
				if (mQoeDataManager != null) {
					count = mQoeDataManager.sendCount();
					/*��ӡ���βɼ�����*/
					textView5.setText(count + "��");
					/*��ӡ��ʷ�ɼ�����*/
					textView6.setText(historyCount + "��");
					Count = count;
				}

			}
			super.handleMessage(msg);
		}
	};

	public class MyThread implements Runnable { // thread
		// @Override
		public void exit() {
			flag = false;
			while (!flag)
				;
		}

		public void run() {
			recLen = 0;
			while (flag) {
				try {
					Thread.sleep(1000); // sleep 1000ms
					Message message = new Message();
					message.what = 1;
					handler.sendMessage(message);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			}
			flag = true;
		}
	}
	/*�Զ��ϴ�����*/
	public void autopost() {
		try {
			readPreferences();

			if (mQoeDataManager == null) {

				mQoeDataManager = new QoeDataManager(this);
				mQoeDataManager.addObserver(this);
				mQoeDataManager.setServerIpAndPort(mServerIpAddress,
						mServerPort);
				mQoeDataManager.setPeriod(mPeriod);
				System.out.println("---post---");
				/*��ʼ�ɼ���Ϣ*/
				mQoeDataManager.start();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			// TODO Auto-generated method stub

		}
	}
	/*��ֹ�ϴ�����*/
	public void autostop() { // �Զ��ϴ���������
		try {
			mQoeDataManager.stop();

		} catch (Exception e) {
			logger.error(e.getMessage());
			// TODO Auto-generated method stub

		}
	}

	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (flag_start) {
				CollectionActivity.this.finish();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this);
				builder.setTitle("����ֹͣ�ɼ�����");
				builder.show();
			}
		
		}
	};
	/*���ӿں���*/
	private void readPreferences() {
		SharedPreferences settings = getSharedPreferences("SETTING", 0);
		mServerIpAddress = settings.getString("SERVER_IPADDRESS",
				DEFAULT_SERVER_IP_ADDRESS);
		mServerPort = settings.getInt("SERVER_PORT", DEFAULT_SERVER_PORT);
		mPeriod = settings.getInt("PERIOD", DEFAULT_PERIOD);
	}

	public void update(Observable observable, Object data) {
		// TODO Auto-generated method stub

	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			if (flag_start) {
				CollectionActivity.this.finish();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(CollectionActivity.this);
				builder.setTitle("����ֹͣ�ɼ�����");
				builder.show();
			}
				
		}

		return super.onKeyDown(keyCode, event);
	}


}