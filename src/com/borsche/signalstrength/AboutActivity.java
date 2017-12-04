package com.borsche.signalstrength;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

//import com.yangduo.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity {

	private ImageButton button_back;
	private Button button_check,button_introduction;
	private TextView title;
	private Logger logger;
	private String mLatestSwVersion;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.about);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_back.setOnClickListener(back);

		button_check = (Button) findViewById(R.id.Button_Check);
		button_check.setOnClickListener(check);
		
		button_introduction = (Button) findViewById(R.id.Button_Introduction);
		button_introduction.setOnClickListener(introduction);
		
		title = (TextView) findViewById(R.id.TextView_Title);
		title.setText(R.string.about_name);

	};

	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub

			AboutActivity.this.finish();

		}
	};

	private Button.OnClickListener check = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
		
			if (!checkIfLatestSwVersion()) {
				upgradeSw();
			}
			else {
				AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
				builder.setTitle("当前已是最新版本");
				builder.show();
			}
		}
	};
	
	private Button.OnClickListener introduction = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
		
	
				AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
				builder.setTitle("这里是：使用说明。。。");
				builder.show();
			
		}
	};

	private boolean checkIfLatestSwVersion() {
		boolean retValue = true;
		try {
			DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet(
			"http://www.borsche.net/download/androidapp/version");
			HttpResponse response = localDefaultHttpClient.execute(httpGet);
			int nStateCode = response.getStatusLine().getStatusCode();
			if (nStateCode / 100 == 2) {
				{HttpEntity httpEntity = response.getEntity();System.out.println("test5");
				byte[] arrayOfByte = EntityUtils.toByteArray(httpEntity);
				httpEntity.consumeContent();
				String strResponse = new String(arrayOfByte, "UTF-8");
				mLatestSwVersion = strResponse.split("=")[1].trim();}
				if (!mLatestSwVersion.equalsIgnoreCase(this
						.getString(R.string.application_version)))		
					retValue = false;
			}

		} catch (Exception e) {
			retValue = true;
		}
		return retValue;
	}

	private void upgradeSw() {
		AlertDialog.Builder builder = new AlertDialog.Builder(
				AboutActivity.this);
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
						AboutActivity.this.startActivity(intent);
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

}