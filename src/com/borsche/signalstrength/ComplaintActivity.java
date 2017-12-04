package com.borsche.signalstrength;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ComplaintActivity extends Activity implements Observer {

	// UI Control members.
	private ListView mListViewMain = null;
	private ProgressDialog progressDialog;

	private Timer timer;

	@SuppressWarnings("unused")
	private static ConfigureLog4j configLog4j = new ConfigureLog4j();
	private Logger logger;

	private QoeDataManager mQoeDataManager = null;

	// User Information.
	private int mComplaintType = 0;

	// Server Information
	private final String DEFAULT_SERVER_IP_ADDRESS = "222.128.13.73";
	private final int DEFAULT_SERVER_PORT = 8088;
	private final int DEFAULT_PERIOD = 15 * 60;

	private String mServerIpAddress = DEFAULT_SERVER_IP_ADDRESS;
	private int mServerPort = DEFAULT_SERVER_PORT;
	private int mPeriod = DEFAULT_PERIOD;
	private ImageButton button_back;
	private TextView title;

	private final int CUSTOM_COMPLAINT_INDEX = 6;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.complaint);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_back.setOnClickListener(back);

		title = (TextView) findViewById(R.id.TextView_Title);
		title.setText(R.string.complaint_name);

		logger = Logger.getLogger("SignalStrengthActivity");
		logger.debug("SignalStrengthActivity::onCreate");

		try {
			readPreferences();

			this.mListViewMain = (ListView) this
			.findViewById(R.id.listViewMain);
			// this.mListViewMain.setAdapter(new ArrayAdapter<String>(this,
			// android.R.layout.simple_list_item_1, CONST_ITEMS));
			this.mListViewMain.setAdapter(new SimpleAdapter(this, getData(),
					R.layout.list_item2, new String[] { "img_pre", "text",
			"img" }, new int[] { R.id.img_pre, R.id.text,
				R.id.img }));

			this.mListViewMain.setOnItemClickListener(mListItemClickListener);

			if (mQoeDataManager == null) {
				mQoeDataManager = new QoeDataManager(this);
				mQoeDataManager.addObserver(this);
				mQoeDataManager.setServerIpAndPort(mServerIpAddress,
						mServerPort);
				mQoeDataManager.setPeriod(mPeriod);
				mQoeDataManager.start1();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			ComplaintActivity.this.finish();
		}
	};

	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < COUNTRIES.length; i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("text", COUNTRIES[i]);
			map.put("img", R.drawable.alarm1);
			map.put("img_pre", R.drawable.alarm2);
			list.add(map);
		}

		return list;
	}

	static final String[] COUNTRIES = new String[] { "通话质量不好", "通话中有掉话",
		"网络信号不好", "接通速率较慢", "上网速度较慢", "网页打不开", "自定义投诉"

	};

	// 储存
	private void readPreferences() {
		SharedPreferences settings = getSharedPreferences("SETTING", 0);
		mServerIpAddress = settings.getString("SERVER_IPADDRESS",
				DEFAULT_SERVER_IP_ADDRESS);
		mServerPort = settings.getInt("SERVER_PORT", DEFAULT_SERVER_PORT);
		mPeriod = settings.getInt("PERIOD", DEFAULT_PERIOD);
	}

	// finish动作
	@Override
	public void onDestroy() {
		super.onDestroy();

		logger.debug("SignalStrengthActivity::onDestroy");

		try {
			if (this.isFinishing()) {
				if (this.mQoeDataManager != null) {
					mQoeDataManager.stop();
				}
				NetworkStateManager.instance(null).Close();//
				this.timer.cancel();
			}
			mQoeDataManager = null;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		logger = null;
	}

	Button.OnClickListener mListener = new Button.OnClickListener() {
		public void onClick(View arg0) {
			if (NetworkStateManager.instance(ComplaintActivity.this)
					.isConnectedWithInternet()) {
				if (ComplaintActivity.this.mQoeDataManager != null) {
					ComplaintActivity.this.mQoeDataManager
					.manualPostData(mComplaintType);

				}
			} else {
				Toast.makeText(
						ComplaintActivity.this,
						ComplaintActivity.this
						.getString(R.string.network_unavailable),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/** LISTIVEW ITEM CLICK LISTENER IMPLEMENTATION **/
	public OnItemClickListener mListItemClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			logger.debug("List item " + String.valueOf(arg2) + " is clicked");

			if (arg2 == CUSTOM_COMPLAINT_INDEX) {
				logger.debug("haha8");
				LayoutInflater inflater = LayoutInflater
				.from(ComplaintActivity.this);
				final View layout = inflater.inflate(R.layout.customcomplaint,
						(ViewGroup) findViewById(R.id.CustomComplaintDialog));

				AlertDialog.Builder builder = new AlertDialog.Builder(
						ComplaintActivity.this);
				builder.setTitle(R.string.custom_complaint).setIcon(
						android.R.drawable.ic_dialog_info).setView(layout)
						.setPositiveButton(R.string.text_ok,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								EditText editText = (EditText) layout
								.findViewById(R.id.editTextComplaintText);
								String compliantText = null;
								try {
									compliantText = URLEncoder.encode(
											editText.getText()
											.toString(),
											"UTF-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
									compliantText = null;
								}
								if (ComplaintActivity.this.mQoeDataManager != null) {
									ComplaintActivity.this.mQoeDataManager
									.manualPostData(255,
											compliantText);
								}
							}
						}).setNegativeButton(R.string.text_cancel,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
							}
						});

				AlertDialog alertDlg = builder.create();
				alertDlg.show();
				return;
			}

			switch (arg2) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
				mComplaintType = arg2 + 1;
				logger.debug("haha9");
				if (NetworkStateManager.instance(ComplaintActivity.this)
						.isConnectedWithInternet()) {
					logger.debug("haha99");
					AlertDialog.Builder builder = new AlertDialog.Builder(
							ComplaintActivity.this);
					builder
					.setMessage(
							ComplaintActivity.this
							.getString(R.string.confirm_send_complaint))
							.setPositiveButton(R.string.text_ok,
									new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog,
										int which) {
									if (ComplaintActivity.this.mQoeDataManager != null) {
										logger.debug("haha98");
										ComplaintActivity.this.mQoeDataManager
										.manualPostData(mComplaintType);
									}
								}
							}).setNegativeButton(R.string.text_cancel,
									new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialog, int id) {
									logger.debug("haha97");
									dialog.cancel();
								};
							});
					logger.debug("haha96");
					AlertDialog alert = builder.create();
					alert.show();
				} else {
					Toast.makeText(ComplaintActivity.this,
							R.string.network_unavailable, Toast.LENGTH_SHORT)
							.show();
				}
				break;
			default:
				break;
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST + 1, 5, R.string.setting).setIcon(
				android.R.drawable.ic_menu_set_as);
		menu.add(Menu.NONE, Menu.FIRST + 2, 5, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);
		return true;
	}
/*
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST + 1: {
			Intent intent = new Intent(this, SettingActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("SERVER_IPADDRESS", mServerIpAddress);
			bundle.putInt("SERVER_PORT", mServerPort);
			bundle.putInt("PERIOD", mPeriod);
			intent.putExtra("SETTING", bundle);
			this.startActivityForResult(intent, 0);
			return true;
		}

		case Menu.FIRST + 2: {
			LayoutInflater inflater = LayoutInflater
			.from(ComplaintActivity.this);
			final View layout = inflater.inflate(R.layout.about,
					(ViewGroup) findViewById(R.id.AboutDialog));

			AlertDialog.Builder builder = new AlertDialog.Builder(
					ComplaintActivity.this);
			builder.setTitle(R.string.about).setIcon(
					android.R.drawable.ic_menu_info_details).setView(layout)
					.setPositiveButton(R.string.text_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					});

			AlertDialog alertDlg = builder.create();
			alertDlg.show();
			return true;
		}
		default:
			break;
		}

		return false;
	}

	// 处理返回的数据
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (resultCode) {
		case RESULT_OK:
			this.mServerIpAddress = data.getCharSequenceExtra(
			"SERVER_IPADDRESS").toString();
			this.mServerPort = data.getIntExtra("SERVER_PORT",
					this.DEFAULT_SERVER_PORT);
			this.mQoeDataManager.setServerIpAndPort(mServerIpAddress,
					mServerPort);
			this.mPeriod = data.getIntExtra("PERIOD", this.DEFAULT_PERIOD);
			this.mQoeDataManager.setPeriod(this.mPeriod);

			SharedPreferences settings = getSharedPreferences("SETTING", 0);
			settings.edit()
			.putString("SERVER_IPADDRESS", this.mServerIpAddress)
			.putInt("SERVER_PORT", this.mServerPort).putInt("PERIOD",
					this.mPeriod).commit();
			break;
		default:
			break;
		}
	}

	// To suppress orientation change event.
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		/*
		 * if (this.getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_LANDSCAPE) { } else if
		 * (this.getResources().getConfiguration().orientation ==
		 * Configuration.ORIENTATION_PORTRAIT) { }
		
	}
	 */
	public void update(Observable observable, Object data) {
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();

		// this.timer.cancel();
	}

};
