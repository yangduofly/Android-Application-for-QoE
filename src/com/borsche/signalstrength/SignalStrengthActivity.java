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
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class SignalStrengthActivity extends Activity {

	// UI Control members.
	private ProgressDialog progressDialog;

	private Timer timer;
	private ImageButton button_compliant, button_network, button_traffic,
	button_setting, button_collect, button_about, button_yd;
	@SuppressWarnings("unused")
	private static ConfigureLog4j configLog4j = new ConfigureLog4j();
	private Logger logger;

	// User Information.

	// Server Information

	private final int LOCATION_FIX_TIMOUT = 3 * 1000;

	private String mLatestSwVersion;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.maintitle);

		// setContentView(R.layout.main);

		logger = Logger.getLogger("SignalStrengthActivity");
		logger.debug("SignalStrengthActivity::onCreate");

		try {
			checkNetworkStatus();
			if (!checkIfLatestSwVersion()) {
				upgradeSw();
			}
			waitForFirstLocationFix();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		findView();
		setListensers();
		setView();
	}

	private void findView() {
		button_compliant = (ImageButton) findViewById(R.id.Button_Complaint);
		button_network = (ImageButton) findViewById(R.id.Button_Network);
		button_traffic = (ImageButton) findViewById(R.id.Button_Traffic);
		button_setting = (ImageButton) findViewById(R.id.Button_Setting);
		button_collect = (ImageButton) findViewById(R.id.Button_Collect);
		button_about = (ImageButton) findViewById(R.id.Button_About);
	}

	private void setListensers() {
		button_compliant.setOnClickListener(compliant);
		button_network.setOnClickListener(network);
		button_traffic.setOnClickListener(traffic);
		button_setting.setOnClickListener(setting);
		button_about.setOnClickListener(about);
		button_collect.setOnClickListener(collect);
	}

	@SuppressWarnings("deprecation")
	private void setView() {
		DisplayMetrics displaysMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaysMetrics);
	
		float  width = (float) (displaysMetrics.widthPixels / 100.0);
	
		float height = (float) (displaysMetrics.heightPixels / 100.0);
		AbsoluteLayout.LayoutParams params1 = new AbsoluteLayout.LayoutParams(
				 (int) (width  *35), (int) (width * 41), (int)(width * 10), (int)(height * 5));
		AbsoluteLayout.LayoutParams params2 = new AbsoluteLayout.LayoutParams(
				(int) (width *35), (int) (width * 41), (int)(width * 55), (int)(height * 5));
		AbsoluteLayout.LayoutParams params3 = new AbsoluteLayout.LayoutParams(
				(int) (width *35), (int) (width * 41), (int)(width * 10), (int)(height * 5+width * 43));
		AbsoluteLayout.LayoutParams params4 = new AbsoluteLayout.LayoutParams(
				(int) (width *35), (int) (width * 41), (int)(width * 55), (int)(height * 5+width * 43));
		AbsoluteLayout.LayoutParams params5 = new AbsoluteLayout.LayoutParams(
				(int) (width *35), (int) (width * 41), (int)(width * 10), (int)(height * 5+width * 86));
		AbsoluteLayout.LayoutParams params6 = new AbsoluteLayout.LayoutParams(
				(int) (width *35), (int) (width * 41), (int)(width * 55), (int)(height * 5+width * 86));
		
		
		button_compliant.setLayoutParams(params1);
		button_collect.setLayoutParams(params2);
		button_network.setLayoutParams(params3);
		button_traffic.setLayoutParams(params4);
		button_setting.setLayoutParams(params5);
		button_about.setLayoutParams(params6);
	}

	private Button.OnClickListener compliant = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this,
					ComplaintActivity.class);
			startActivity(intent);
		}
	};
	private Button.OnClickListener collect = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this,
					CollectionActivity.class);
			startActivity(intent);
		}
	};
	private Button.OnClickListener network = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this, NetworkActivity.class);
			startActivity(intent);
		}
	};
	private Button.OnClickListener traffic = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this, TrafficActivity.class);
			startActivity(intent);
		}
	};
	private Button.OnClickListener setting = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this, SettingActivity.class);
			startActivity(intent);
		}
	};
	private Button.OnClickListener about = new Button.OnClickListener() {

		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(SignalStrengthActivity.this, AboutActivity.class);
			startActivity(intent);
		}
	};

	private void checkNetworkStatus() {
		if (!NetworkStateManager.instance(this).isConnectedWithInternet()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					SignalStrengthActivity.this);
			builder.setIcon(android.R.drawable.ic_dialog_info).setMessage(
					R.string.title_network_fail).setPositiveButton(
							R.string.setting, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									Intent intent = new Intent();
									intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
									intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									SignalStrengthActivity.this.startActivity(intent);
								}
							}).setNegativeButton(R.string.exit,
									new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									SignalStrengthActivity.this.finish();
									System.exit(0);
								}
							});

			AlertDialog alertDlg = builder.create();
			alertDlg.show();
		} else {
			logger.info("Network connection OK");
		}
	}

	private boolean checkIfLatestSwVersion() {
		boolean retValue = true;
		try {
			DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(
			"http://www.borsche.net/download/androidapp/version");
			HttpResponse response = localDefaultHttpClient.execute(httpGet);
			int nStateCode = response.getStatusLine().getStatusCode();
			logger.info("nStateCode=" + Integer.toString(nStateCode));
			if (nStateCode / 100 == 2) {
				HttpEntity httpEntity = response.getEntity();
				byte[] arrayOfByte = EntityUtils.toByteArray(httpEntity);
				httpEntity.consumeContent();
				String strResponse = new String(arrayOfByte, "UTF-8");
				mLatestSwVersion = strResponse.split("=")[1].trim();
				logger.info("mLatestSwVersion=" + mLatestSwVersion);
				if (!mLatestSwVersion.equalsIgnoreCase(this
						.getString(R.string.application_version)))
					retValue = false;
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			retValue = true;
		}
		return retValue;
	}

	private void upgradeSw() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				SignalStrengthActivity.this);
		builder.setIcon(android.R.drawable.ic_dialog_info).setMessage(
				R.string.title_sw_upgrade).setPositiveButton(R.string.text_ok,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(
								Intent.ACTION_VIEW,
								Uri
								.parse("http://www.borsche.net/download/androidapp/"
										+ mLatestSwVersion
										+ "/SignalStrength.apk"));
						intent.setClassName("com.android.browser",
						"com.android.browser.BrowserActivity");
						SignalStrengthActivity.this.startActivity(intent);
					}
				}).setNegativeButton(R.string.text_cancel,
						new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});

		AlertDialog alertDlg = builder.create();
		alertDlg.show();
	}

	private void waitForFirstLocationFix() {
		// Setup a timer if location fix take too long time.
		this.timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {

				Looper.prepare();

				// If location fix timeout then pop up a error message box.
				if (progressDialog != null && progressDialog.isShowing())
					progressDialog.dismiss();
				if (!isGpsOpen()) {
					AlertDialog.Builder builder = new AlertDialog.Builder(
							SignalStrengthActivity.this);
					builder.setIcon(android.R.drawable.ic_dialog_info)
					.setMessage(R.string.location_fix_fail)
					.setPositiveButton(R.string.setting,
							new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int which) {
							Intent intent = new Intent();
							intent
							.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							intent
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							SignalStrengthActivity.this
							.startActivity(intent);
						}
					}).setNegativeButton(R.string.exit,
							new DialogInterface.OnClickListener() {
						public void onClick(
								DialogInterface dialog,
								int which) {
							SignalStrengthActivity.this
							.finish();
							System.exit(0);
						}
					});

					AlertDialog alertDlg = builder.create();
					alertDlg.show();
				}
				Looper.loop();

			}
		};

		progressDialog = ProgressDialog
		.show(
				SignalStrengthActivity.this,
				SignalStrengthActivity.this
				.getString(R.string.wait_for_moment),
				SignalStrengthActivity.this.getString(R.string.loading),
				true);
		timer.schedule(task, LOCATION_FIX_TIMOUT);
	}

	private boolean isGpsOpen() {// 检测GPS是否已经开启
		LocationManager alm = (LocationManager) this
		.getSystemService(Context.LOCATION_SERVICE);
		if (alm
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			Toast.makeText(this, "GPS已经开启", Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return false;
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			new AlertDialog.Builder(this).setMessage(
					this.getText(R.string.sure_exit_app).toString())
					.setPositiveButton(R.string.text_ok,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
							SignalStrengthActivity.this.finish();
							System.exit(0);
						}
					}).setNegativeButton(R.string.text_cancel,
							new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int which) {
						}
					}).setNeutralButton(R.string.running_background,
							new DialogInterface.OnClickListener() {
						// Running in background as press back button.
						public void onClick(DialogInterface dialog,
								int which) {
							Intent intent = new Intent(
									Intent.ACTION_MAIN);
							intent
							.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
							intent.addCategory(Intent.CATEGORY_HOME);
							startActivity(intent);
						}
					}).create().show();
		}

		return super.onKeyDown(keyCode, event);
	}

	public void update(Observable observable, Object data) {
		if (progressDialog != null && progressDialog.isShowing())
			progressDialog.dismiss();

		this.timer.cancel();
	}

	// public void setButton_yd(ImageButton button_yd) {
	// this.button_yd = button_yd;
	// }

	// public ImageButton getButton_yd() {
	// return button_yd;
	// }
};