package com.borsche.signalstrength;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class SettingActivity extends Activity {
	
	private EditText ipAddressEdit = null;
	private EditText portAddressEdit = null;
	private EditText periodEdit = null;
	private TextView title;
	private Button okBtn = null;
	private Button cancelBtn = null;
    CellLocationManager celllocation ;
	
	private ImageButton button_back;
	private ImageButton button_save;
	private Button button_clear;
	//private EditText editTextmin;
	//private EditText editTextmax;
    /**ʹ��SharedPreferences ���������ȡ����**/
    SharedPreferences myShare = null;
	/**SharedPreferences�д������ݵ����ݱ�������**/
	public final static String MYSAHRE_MAIN = "myshare";
	public final static String TEXT_MIN = "textmin";
	public final static String TEXT_MAX = "textmax";
	public final static String TEXT_THRESHOLD = "threshold";
	public final static String TEXT_DATABASEMAX = "database";
    /**SharedPreferences�д������ݵ�·��**/
    public final static String DATA_URL = "/data/data/";
    public final static String SHARED_MAIN_XML = "ydmain.xml";

    public static String TEXTMIN_DEFAULT = "10";
    public static String TEXTMAX_DEFAULT = "60";
    public static String THRESHOLD_DEFAULT = "-90";
    public static String DATABASEMAX_DEFAULT = "200";
	public final static int mytext = 0;
	
    private Logger logger;
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.setting);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.settingtitle);
     
		button_back = (ImageButton) findViewById(R.id.Button_Back);
		button_back.setOnClickListener(back); 
		
		button_save = (ImageButton) findViewById(R.id.Button_Save);

		
		button_clear = (Button) findViewById(R.id.Button_Clear);

	    title = (TextView)findViewById(R.id.TextView_Title); 
		title.setText(R.string.setting_name);  
		
		/**�õ�������SHARED_MAIN ��SharedPreferences����**/
		myShare = getSharedPreferences(MYSAHRE_MAIN, Context.MODE_WORLD_READABLE) ;
		/**�õ�SharedPreferences�б������ֵ �ڶ�������Ϊ���SharedPreferences��û�б���͸�һ��Ĭ��ֵ**/
		 String text_min = myShare.getString(TEXT_MIN,TEXTMIN_DEFAULT );
		 String text_max = myShare.getString(TEXT_MAX, TEXTMAX_DEFAULT);
		 final String text_threshold = myShare.getString(TEXT_THRESHOLD, THRESHOLD_DEFAULT);
		 String text_datebase = myShare.getString(TEXT_DATABASEMAX, DATABASEMAX_DEFAULT);
		final EditText editTextmin = (EditText)findViewById(R.id.editTextmin);
		final EditText editTextmax = (EditText)findViewById(R.id.editTextmax);
		final EditText editTextthreshold = (EditText)findViewById(R.id.editTextThreshold);
		final EditText editTextdatabase = (EditText)findViewById(R.id.editTextdatabasemax);
		editTextmin.setHint(text_min);	
		editTextmax.setHint(text_max);
		editTextthreshold.setHint(text_threshold);
		editTextdatabase.setHint(text_datebase);


		/**����ύ��ť**/
		button_save.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/**�õ��û��������Ϣ**/
				String text_min = editTextmin.getText().toString();
				/**���û�����룬��ֵĬ��ֵ**/
				if("".equals(editTextmin.getText().toString().trim()))
					{
					text_min = TEXTMIN_DEFAULT;
					}
				String text_max = editTextmax.getText().toString();	
				if("".equals(editTextmax.getText().toString().trim()))
					{
					text_max = TEXTMAX_DEFAULT;
					}
				String text_threshold = editTextthreshold.getText().toString();
				/**���û�����룬��ֵĬ��ֵ**/
				if("".equals(editTextthreshold.getText().toString().trim()))
					{
					text_threshold = THRESHOLD_DEFAULT;
					}
				String text_datebase = editTextdatabase.getText().toString();	
				if("".equals(editTextdatabase.getText().toString().trim()))
					{
					text_datebase = DATABASEMAX_DEFAULT;
					}

				/**��ʼ������SharedPreferences**/	
				Editor editor = myShare.edit();
				editor.putString(TEXT_MIN, text_min);
				editor.putString(TEXT_MAX, text_max);
				editor.putString(TEXT_THRESHOLD, text_threshold);
				editor.putString(TEXT_DATABASEMAX, text_datebase);
				/**put��ϱ���Ҫcommit()�����޷�����**/
				editor.commit();
				ShowDialog("����SharedPreferences�ɹ�");
				Change_Threshold();

			}

			private void Change_Threshold() {
				// TODO Auto-generated method stub
				String threshold = editTextthreshold.getText().toString();
				if("".equals(editTextthreshold.getText().toString().trim()))
					{
						threshold= TEXTMAX_DEFAULT;
					}
				QoeDataManager.Threshold_Max = StrToInt(threshold);
				System.out.println("zxp2:"+QoeDataManager.Threshold_Max);//logger.debug("zxp:");//+new_threshold);
				
			}

		});
		
		button_clear.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				/**��ʼ���SharedPreferences�б��������**/
				Editor editor = myShare.edit();
				editor.remove(TEXT_MIN);
				editor.remove(TEXT_MAX);
				editor.remove(TEXT_THRESHOLD);
				editor.remove(TEXT_DATABASEMAX);
				editor.commit();
				ShowDialog("���SharedPreferences���ݳɹ�");	
				editTextmin.setHint(TEXTMIN_DEFAULT);
				editTextmax.setHint(TEXTMAX_DEFAULT);
				editTextthreshold.setHint(THRESHOLD_DEFAULT);
				editTextdatabase.setHint(DATABASEMAX_DEFAULT);
			}
		});

    }

	private Button.OnClickListener back = new Button.OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub
			SettingActivity.this.finish();
		}
	};
	
	public void ShowDialog(String string) {
		AlertDialog.Builder builder = new AlertDialog.Builder(SettingActivity.this);
		//builder.setIcon(R.drawable.icon);
		builder.setTitle(string);
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int whichButton) {
			//finish();
		    }
		});
		builder.show();
	    }
	/**��String����ת��ΪInt����**/
	public static int StrToInt(String value) { 
	    try { 
	        return Integer.valueOf(value); 
	    } catch (Exception e) { 
	        return 0; 
	    } 
	} 
	  
}
