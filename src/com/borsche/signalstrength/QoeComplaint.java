package com.borsche.signalstrength;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;

public class QoeComplaint {
	private final String os_type = "Android";
	private final String os_version =android.os.Build.VERSION.RELEASE;
	private final String manufacturer = android.os.Build.MANUFACTURER;
	private final String model = android.os.Build.MODEL;
	private long time_stamp = 0;
	private String imei = "";
	private String imsi = "";
	private int network_type = 0;
	private int mobile_country_code = 0;
	private int mobile_network_code = 0;
	private int cell_id = 0;
	private int location_area_code = 0;
	private int signal_strength_dbm = 0;
	private String neighboring_cells = "";
	private double latitude = 0.0f;
	private double longtitude = 0.0f;
	private int accuracy = 0;
	private int complaint_type = 0;
	private String complaint_text = "";
	
	public void setTimeStamp(long ts){
		this.time_stamp = ts;
	}
	
	public void setIMEI(String in){
		this.imei = in;
	}
	
	public void setIMSI(String in){
		this.imsi = in;
	}
	
	public void setNetworkType(int nt){
		this.network_type = nt;
	}
	
	public void setMCC(int mcc){
		this.mobile_country_code = mcc;
	}
	
	public void setMNC(int mnc){
		this.mobile_network_code = mnc;
	}
	
	public void setCellId(int cellId){
		this.cell_id = cellId;
	}
	
	public void setCellLac(int lac){
		this.location_area_code = lac;
	}
	
	public void setSignalStrength(int dbm){
		this.signal_strength_dbm = dbm;
	}
	
	public void setNeiboringCells(String neiboring){
		this.neighboring_cells = neiboring;
	}
	
	public void setLatitude(double lat){
		this.latitude = lat;
	}
	
	public void setLongtitude(double lng){
		this.longtitude = lng;
	}
	
	public void setAccuracy(int acu){
		this.accuracy = acu;
	}
	
	public void setComplaintText(String text){
		this.complaint_text = text;
	}
	
	public void setComplaintType(int type){
		this.complaint_type = type;
	}
	
	public JSONObject toJsonObject(){
		JSONObject objectToSend = new JSONObject();
		try{
			objectToSend.put("os_type", this.os_type);
			objectToSend.put("os_version", this.os_version);
			objectToSend.put("manufacturer", this.manufacturer);
			objectToSend.put("model", this.model);
			objectToSend.put("time_stamp_low", this.time_stamp & 0xFFFF);
			objectToSend.put("time_stamp_high", this.time_stamp >> 16);
			objectToSend.put("imei", this.imei);
			objectToSend.put("imsi", this.imsi);
			objectToSend.put("network_type", this.network_type);
			objectToSend.put("mobile_country_code", this.mobile_country_code);
			objectToSend.put("mobile_network_code", this.mobile_network_code);
			objectToSend.put("cell_id", this.cell_id);
			objectToSend.put("location_area_code", this.location_area_code);
			objectToSend.put("signal_strength_dbm", this.signal_strength_dbm);
			objectToSend.put("neighboring_cells", this.neighboring_cells);
			objectToSend.put("latitude", this.latitude);
			objectToSend.put("longtitude", this.longtitude);
			objectToSend.put("accuracy", this.accuracy);
			objectToSend.put("complaint_type", this.complaint_type);
			objectToSend.put("complaint_text", this.complaint_text);
		}catch (JSONException ex){
			objectToSend = null;
		}
		
		return objectToSend;
	}
	
	public ContentValues toContentValues(){
		ContentValues cv = new ContentValues();
		cv.put("os_type", this.os_type);
		cv.put("os_version", this.os_version);
		cv.put("manufacturer", this.manufacturer);
		cv.put("model", this.model);
		cv.put("time_stamp", this.time_stamp);
		cv.put("imei", this.imei);
		cv.put("imsi", this.imsi);
		cv.put("network_type", this.network_type);
		cv.put("mobile_country_code", this.mobile_country_code);
		cv.put("mobile_network_code", this.mobile_network_code);
		cv.put("cell_id", this.cell_id);
		cv.put("location_area_code", this.location_area_code);
		cv.put("signal_strength_dbm", this.signal_strength_dbm);
		cv.put("neighboring_cells", this.neighboring_cells);
		cv.put("latitude", this.latitude);
		cv.put("longtitude", this.longtitude);
		cv.put("accuracy", this.accuracy);
		cv.put("complaint_type", this.complaint_type);
		cv.put("complaint_text", this.complaint_text);
		
		return cv;
	}
}
