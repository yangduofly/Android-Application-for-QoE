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

	
	public long getTotalRxBytes(){  //获取总的接受字节数，包含Mobile和WiFi等  
        return TrafficStats.getTotalRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalRxBytes()/1024);  
    }  
    public long getTotalTxBytes(){  //总的发送字节数，包含Mobile和WiFi等  
        return TrafficStats.getTotalTxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getTotalTxBytes()/1024);  
    }  
    public long getMobileRxBytes(){  //获取通过Mobile连接收到的字节总数，不包含WiFi  
        return TrafficStats.getMobileRxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getMobileRxBytes()/1024);  
    }  
    public long getMobileTxBytes(){  //获取通过Mobile连接收到的字节总数，不包含WiFi  
        return TrafficStats.getMobileTxBytes()==TrafficStats.UNSUPPORTED?0:(TrafficStats.getMobileTxBytes()/1024);  
    }  
    public static long getUidRxBytes (int uid)
    {  //获取通过Mobile连接收到的字节总数，不包含WiFi  
        return TrafficStats.getUidRxBytes(uid)==TrafficStats.UNSUPPORTED?0:(TrafficStats.getUidRxBytes(uid)/1024);  
    }  
    public static long getUidTxBytes (int uid)
    {  //获取通过Mobile连接收到的字节总数，不包含WiFi  
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
		
			// 包名就是程序在进程中的名字 packagename
			String packagename = runningAppProcessInfo.processName;

			process.setPackagename(packagename);
			// memoryinfo
			long memsize = am.getProcessMemoryInfo(new int[] { uid })[0]
					.getTotalPrivateDirty() * 1024;//字节需要转化为M

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
					pricessinfos.add(process); //只加用户进程到list里面
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

	// 判断是系统应用还是三方应用
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
