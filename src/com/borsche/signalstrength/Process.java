package com.borsche.signalstrength;

import java.util.List;

import android.graphics.drawable.Drawable;

public class Process {
	private String packagename;
	private String appname;
	private long memsize;// bytes
	private Drawable icon;
	private boolean userprocess;
	private boolean checked;
	private int uid;
	public String getPackagename() {
		return packagename;
	}

	public void setPackagename(String packagename) {
		this.packagename = packagename;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public long getMemsize() {
		return memsize;
	}

	public void setMemsize(long memsize) {
		this.memsize = memsize;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public boolean isUserprocess() {
		return userprocess;
	}

	public void setUserprocess(boolean userprocess) {
		this.userprocess = userprocess;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}
}