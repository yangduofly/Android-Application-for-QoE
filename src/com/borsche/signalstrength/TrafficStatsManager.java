package com.borsche.signalstrength;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.widget.Toast;

public class TrafficStatsManager {

	private Context mContext = null;
	private String mappname;
	private int muid,mSize;
	List<Process> mPricessinfos;
	//private boolean mInternetConnected = false;
	
	public TrafficStatsManager(Context context){
	ProcessInfo mProcessInfo= new ProcessInfo(context);
	//System.out.println("---traffic---"+ mProcessInfo.processProvider().size());
	 mPricessinfos=mProcessInfo.processProvider();

	
		mSize=mPricessinfos.size();
	}
	public List<Process> getProcess() {
		return  mPricessinfos;
	}
	public int getProcessSize() {
		return mSize;
	}

	
	public long getTotalRxBytes(){  //��ȡ�ܵĽ����ֽ���������Mobile��WiFi��  
        return TrafficStats.getTotalRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalRxBytes()/1024);  
    }  
    public long getTotalTxBytes(){  //�ܵķ����ֽ���������Mobile��WiFi��  
        return TrafficStats.getTotalTxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalTxBytes()/1024);  
    }  
    public long getMobileRxBytes(){  //��ȡͨ��Mobile�����յ����ֽ�������������WiFi  
        return TrafficStats.getMobileRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getMobileRxBytes()/1024);  
    }  
    public long getMobileTxBytes(){  //��ȡͨ��Mobile�����յ����ֽ�������������WiFi  
        return TrafficStats.getMobileTxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getMobileTxBytes()/1024);  
    }  
    public static long getUidRxBytes (int uid)
    {  //��ȡͨ��Mobile�����յ����ֽ�������������WiFi  
        return TrafficStats.getUidRxBytes(uid)==TrafficStats.UNSUPPORTED?0:(TrafficStats.getUidRxBytes(uid)/1024);  
    }  
    public static long getUidTxBytes (int uid)
    {  //��ȡͨ��Mobile�����յ����ֽ�������������WiFi  
        return TrafficStats.getUidTxBytes(uid)==TrafficStats.UNSUPPORTED?0:(TrafficStats.getUidTxBytes(uid)/1024);  
    }  

public class ProcessInfo {

	private Context context;

	public ProcessInfo(Context context) {
		this.context = context;
		processProvider();
		
	}

	public List<Process> processProvider() {
		List<Process> pricessinfos = new ArrayList<Process>();

		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);

		PackageManager pm = context.getPackageManager();

		List<RunningAppProcessInfo> runnings = am.getRunningAppProcesses();

		for (RunningAppProcessInfo runningAppProcessInfo : runnings) {
			
			Process process = new Process();
			// uid
			int uid = runningAppProcessInfo.uid;

			process.setUid(uid);
		
			// �������ǳ����ڽ����е����� packagename
			String packagename = runningAppProcessInfo.processName;

			process.setPackagename(packagename);
			// memoryinfo
			long memsize = am.getProcessMemoryInfo(new int[] { uid })[0]
					.getTotalPrivateDirty() * 1024;//�ֽ���Ҫת��ΪM

			process.setMemsize(memsize);

			try {
				ApplicationInfo info = pm.getApplicationInfo(packagename, 0);
				// icon
				Drawable icon = info.loadIcon(pm);

				process.setIcon(icon);
				// appname
				String appname = info.loadLabel(pm).toString();

				process.setAppname(appname);
				// userprocess
				if (filterApp(info)) {
					process.setUserprocess(true);
					pricessinfos.add(process); //ֻ���û����̵�list����
				} else {
					process.setUserprocess(false);
				}

			} catch (Exception e) {
				process.setIcon(context.getResources().getDrawable(R.drawable.btn));
				process.setUserprocess(false);
				process.setAppname(packagename);
				e.printStackTrace();
			}
		
		}
		
		return pricessinfos;
	}

	// �ж���ϵͳӦ�û�������Ӧ��
	public boolean filterApp(ApplicationInfo info) {
		if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
			return true;
		} else if ((info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
			return true;
		}
		return false;
	}

}





}
