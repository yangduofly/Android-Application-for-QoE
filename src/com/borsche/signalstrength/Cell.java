package com.borsche.signalstrength;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class Cell {
	private TelephonyManager telephonyManager;
	private PhoneStateListener phoneStateListener;
	private int lastSignal, lac, cid, nb_Type,type;
	private long time;
	private Logger logger;
	private String mcc, mnc, nb_lac, nb_cid,nb_Signal;
	private LocationManager locationManager;
	private double Latitude, Longitude;
	private String strengthValue;

	public Cell(Context paramContext) {
		telephonyManager = (TelephonyManager) paramContext
		.getSystemService(Context.TELEPHONY_SERVICE);
		locationManager = (LocationManager) paramContext
		.getSystemService(Context.LOCATION_SERVICE);
		start();
	}
	public void start()
	{
		InitPhoneStateListener();
		
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_CELL_LOCATION);
		if (telephonyManager.getCellLocation() != null) {
			// ��ȡ��ǰ��վ��Ϣ
			phoneStateListener.onCellLocationChanged(telephonyManager
					.getCellLocation());
		}
	
		Criteria criteria = new Criteria();
		// ����Ҫ��
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(false);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// ȡ��Ч����õ�criteria
		String provider = locationManager.getBestProvider(criteria, true);
		// �õ�������ص���Ϣ
		if (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) 
		{	Location location = locationManager.getLastKnownLocation(provider);
		// ��������
		updateWithNewLocation(location);
		// ע��һ�������Եĸ��£�3000ms����һ��
		// locationListener����������λ��Ϣ�ĸı�
		locationManager.requestLocationUpdates(provider, 3000, 0,
				locationListener);}
	};

	private void InitPhoneStateListener() {
		Log.d("mydebug", "3");
		phoneStateListener = new PhoneStateListener() {
			ArrayList<CellIDInfo> CellID = new ArrayList<CellIDInfo>();

			@Override
			public void onCellLocationChanged(CellLocation location) {
				// TODO Auto-generated method stub
				type = telephonyManager.getNetworkType();
				CellIDInfo currentCell = new CellIDInfo();
				if (type == TelephonyManager.NETWORK_TYPE_GPRS
						|| type == TelephonyManager.NETWORK_TYPE_EDGE
						|| type == TelephonyManager.NETWORK_TYPE_HSDPA
						|| type == TelephonyManager.NETWORK_TYPE_UMTS
						|| type == 15) {// gsm����
					GsmCellLocation gsm = ((GsmCellLocation) telephonyManager
							.getCellLocation());
					if (gsm == null) {
						logger.error("GsmCellLocation is null!!!");
					}

					lac = gsm.getLac();
					cid = gsm.getCid();

					String mccMnc = telephonyManager.getNetworkOperator();
					if (mccMnc != null && mccMnc.length() >= 5) {
						mcc = mccMnc.substring(0, 3);
						mnc = mccMnc.substring(3, 5);
					}
					time = System.currentTimeMillis();
					currentCell.cellId = gsm.getCid();
					currentCell.mobileCountryCode = mcc;
					currentCell.mobileNetworkCode = mnc;
					currentCell.locationAreaCode = lac;
					currentCell.networktype=type;
					currentCell.radioType = "gsm";

					CellID.add(currentCell);

					List<NeighboringCellInfo> list = telephonyManager
					.getNeighboringCellInfo();
					int size = list.size();
					if (list.size() == 0) {
						nb_Type=0;
						nb_lac ="δ֪";
						nb_cid = "δ֪";
						nb_Signal="δ֪";
					} else {
						for (int i = 0; i < size; i++) {

							CellIDInfo info = new CellIDInfo();
							info.cellId = list.get(i).getCid();
							info.locationAreaCode = list.get(i).getLac();
							info.networktype=list.get(i).getNetworkType();
						
							CellID.add(info);

						}		
						int Signal=list.get(1).getRssi()*2-113;
						nb_Signal=Signal+"  dBm";
						nb_lac = CellID.get(1).locationAreaCode + "";
						nb_cid = CellID.get(1).cellId + "";
						nb_Type=CellID.get(1).networktype;
					}
					
				} else if (type == TelephonyManager.NETWORK_TYPE_EVDO_A
						|| type == TelephonyManager.NETWORK_TYPE_CDMA
						|| type == TelephonyManager.NETWORK_TYPE_1xRTT) {// ����CDMA������
					try {
						Class cdmaClass = Class
						.forName("android.telephony.cdma.CdmaCellLocation");
						CdmaCellLocation cdma = (CdmaCellLocation) location;

						/** ��ȡmcc��mnc */

						setTime(System.currentTimeMillis());

						lac = cdma.getNetworkId();
						mcc = telephonyManager.getNetworkOperator().substring(
								0, 3);
						mnc = String.valueOf(cdma.getSystemId());
						cid = cdma.getBaseStationId();
						currentCell.cellId = cid;
						currentCell.mobileCountryCode = mcc;
						currentCell.mobileNetworkCode = mnc;
						currentCell.locationAreaCode = lac;
						currentCell.networktype=type;
						currentCell.radioType = "cdma";

						CellID.add(currentCell);

						List<NeighboringCellInfo> list = telephonyManager
						.getNeighboringCellInfo();
						int size = list.size();
						if (list.size() == 0) {
							nb_Signal="δ֪";
							nb_Type=0;
							nb_lac = "δ֪";
							nb_cid ="δ֪";
						} else {
							for (int i = 0; i < size; i++) {

								CellIDInfo info = new CellIDInfo();
								info.cellId = list.get(i).getCid();
								info.locationAreaCode = list.get(i).getLac();
								info.networktype=list.get(i).getNetworkType();

								CellID.add(info);
							}
							int Signal=list.get(1).getRssi()*2-113;
							nb_Signal=Signal+"  dBm";
							nb_lac = CellID.get(1).locationAreaCode + "";
							nb_cid = CellID.get(1).cellId + "";
							nb_Type=CellID.get(1).networktype;
						}
					} catch (ClassNotFoundException classnotfoundexception) {
					}
				}// end CDMA����

				super.onCellLocationChanged(location);
			}// end onCellLocationChanged

			@Override
			public void onServiceStateChanged(ServiceState serviceState) {
				// TODO Auto-generated method stub
				super.onServiceStateChanged(serviceState);
			}

			/*
			 * @Override public void onSignalStrengthsChanged(SignalStrength
			 * signalStrength) { // TODO Auto-generated method stub int asu =
			 * signalStrength.getGsmSignalStrength(); lastSignal = -113 + 2 *
			 * asu; // �ź�ǿ�� super.onSignalStrengthsChanged(signalStrength);
			 * Log.d("mydebug", "2"); }
			 */
			@Override
			public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				super.onSignalStrengthsChanged(signalStrength);

				if (signalStrength.isGsm()) {
					if (signalStrength.getGsmSignalStrength() != 99) {
						lastSignal = signalStrength.getGsmSignalStrength() * 2 - 113;
						strengthValue = lastSignal + "  dBm";
					} else

					{
						lastSignal = signalStrength.getGsmSignalStrength();
						strengthValue = "��ǰ�ź�״̬������";
					}

				} else {
					lastSignal = signalStrength.getCdmaDbm();
					strengthValue = lastSignal + "  dBm";
				}

			}

		};

	}// end InitPhoneStateListener

	/** ʹ��network_provider��ȡ����λ�� */
	/*
	 * private Location callGear(ArrayList<CellIDInfo> cellID) {
	 * Log.d("mydebug", "1"); if (cellID == null) return null; DefaultHttpClient
	 * client = new DefaultHttpClient(); HttpPost post = new
	 * HttpPost("http://www.google.com/loc/json"); JSONObject holder = new
	 * JSONObject(); try { holder.put("version", "1.1.0"); holder.put("host",
	 * "maps.google.com"); holder.put("home_mobile_country_code",
	 * cellID.get(0).mobileCountryCode); holder.put("home_mobile_network_code",
	 * cellID.get(0).mobileNetworkCode); holder.put("radio_type",
	 * cellID.get(0).radioType); holder.put("request_address", true); if
	 * ("460".equals(cellID.get(0).mobileCountryCode))
	 * holder.put("address_language", "zh_CN"); else
	 * holder.put("address_language", "en_US"); JSONObject data, current_data;
	 * JSONArray array = new JSONArray(); current_data = new JSONObject();
	 * current_data.put("cell_id", cellID.get(0).cellId);
	 * current_data.put("location_area_code", cellID.get(0).locationAreaCode);
	 * current_data.put("mobile_country_code", cellID.get(0).mobileCountryCode);
	 * current_data.put("mobile_network_code", cellID.get(0).mobileNetworkCode);
	 * current_data.put("age", 0); array.put(current_data); if (cellID.size() >
	 * 2) { for (int i = 1; i < cellID.size(); i++) { data = new JSONObject();
	 * data.put("cell_id", cellID.get(i).cellId); data.put("location_area_code",
	 * cellID.get(i).locationAreaCode); data.put("mobile_country_code",
	 * cellID.get(i).mobileCountryCode); data.put("mobile_network_code",
	 * cellID.get(i).mobileNetworkCode); data.put("age", 0); array.put(data); }
	 * } holder.put("cell_towers", array); StringEntity se = new
	 * StringEntity(holder.toString()); Log.e("Location send",
	 * holder.toString()); post.setEntity(se); HttpResponse resp =
	 * client.execute(post); HttpEntity entity = resp.getEntity();
	 * 
	 * BufferedReader br = new BufferedReader(new InputStreamReader(entity
	 * .getContent())); StringBuffer sb = new StringBuffer(); String result =
	 * br.readLine(); while (result != null) { Log.e("Locaiton receive",
	 * result); sb.append(result); result = br.readLine(); } if (sb.length() <=
	 * 1) return null; data = new JSONObject(sb.toString()); data = (JSONObject)
	 * data.get("location");
	 * 
	 * Location loc = new Location(LocationManager.NETWORK_PROVIDER);
	 * loc.setLatitude((Double) data.get("latitude")); loc.setLongitude((Double)
	 * data.get("longitude"));
	 * loc.setAccuracy(Float.parseFloat(data.get("accuracy").toString()));
	 * loc.setTime(GetUTCTime()); Log.d("mydebug", "4");
	 * 
	 * return loc; } catch (JSONException e) { return null; } catch
	 * (UnsupportedEncodingException e) { e.printStackTrace(); } catch
	 * (ClientProtocolException e) { e.printStackTrace(); } catch (IOException
	 * e) { e.printStackTrace(); } Location loc = new
	 * Location(LocationManager.NETWORK_PROVIDER); // loc.setLatitude((Double)
	 * cellID.get(0).get("latitude")); // loc.setLongitude((Double)
	 * cellID.get(0).get("longitude")); Latitude=loc.getLatitude();
	 * Longitude=loc.getLongitude(); return null; } public long GetUTCTime() {
	 * Calendar cal = Calendar.getInstance(Locale.CHINA); int zoneOffset =
	 * cal.get(java.util.Calendar.ZONE_OFFSET); int dstOffset =
	 * cal.get(java.util.Calendar.DST_OFFSET);
	 * cal.add(java.util.Calendar.MILLISECOND, -(zoneOffset + dstOffset));
	 * return cal.getTimeInMillis(); }
	 */
	/** ʹ��cellID��ȡ����λ�� */
	private void updateWithNewLocation(Location location) {
		if (location != null) {
			// Ϊ���Ʊ�־������������
			// ȡ��γ�Ⱥ;���
		
			Latitude = location.getLatitude() ;
			Longitude = location.getLongitude() ;
			// ����ת��Ϊint��

		} else {
			// myposition.setLocation(location);
			// Latitude=39.9*1E6;
			// Longitude=116.3*1E6;

		}
	}

	private final LocationListener locationListener = new LocationListener() {
		// ������ı�ʱ�����˺���
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		// Provider��disableʱ�����˺���������GPS���ر�
		public void onProviderDisabled(String provider) {
			updateWithNewLocation(null);
		}

		// Provider��enableʱ�����˺���������GPS����
		public void onProviderEnabled(String provider) {
		}

		// Provider��ת̬�ڿ��á���ʱ�����ú��޷�������״ֱ̬���л�ʱ�����˺���
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	public void setTime(long time) {
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public int getCid() {
		return cid;
	}

	public int getLac() {
		return lac;
	}

	public double getLatitude() {
		return Latitude;
	}

	public double getLongitude() {
		return Longitude;
	}

	public int getNetworkType() {
		return type;
	}

	public String getSignalStrength() {
		return strengthValue;
	}
	public int  getNbType() {
		return nb_Type;
	}
	public String getNbcid() {
		return nb_cid;
	}
	public String getNblac() {
		return nb_lac;
	}
	
	public String getNbSignal() {
		return nb_Signal;
	}
}
