package com.borsche.signalstrength;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.util.Log;
import android.widget.Toast;
import org.apache.log4j.Logger;

public class CellLocationManager {
	public static int CHECK_INTERVAL = 5000;
	public static boolean ENABLE_WIFI = false;
	private static final int STATE_COLLECTING = 2;
	private static final int STATE_IDLE = 0;
	private static final int STATE_READY = 1;
	private static final int STATE_SENDING = 3;
	private static final int MESSAGE_INITIALIZE = 1;
	private static final int MESSAGE_COLLECTING_CELL = 2;
	private static final int MESSAGE_COLLECTING_WIFI = 5;
	private static final int MESSAGE_BEFORE_FINISH = 10;
	private static final int MESSAGE_RE_REQUEST = 20;
	private int accuracy, bid;
	private CellInfoManager cellInfoManager;
	private Context context;
	private int[] aryGsmCells;
	private double latitude, longitude;
	private MyLooper looper;
	private boolean paused, waiting4WifiEnable, disableWifiAfterScan;
	private final BroadcastReceiver receiver;
	private long startScanTimestamp, timestamp;
	private int state, sendCount;
	private CellLocationTask task;
	private WifiInfoManager wifiManager;
	public static String DELAY_TIME = SettingActivity.TEXTMAX_DEFAULT;
	public String string_min = null;
	public String string_max = null;
	private Logger logger;

	public CellLocationManager(Context context,
			CellInfoManager cellinfomanager, WifiInfoManager wifiinfomanager) {
		receiver = new CellLocationManagerBroadcastReceiver();
		this.context = context.getApplicationContext();
		cellInfoManager = cellinfomanager;
		wifiManager = wifiinfomanager;
		logger = Logger.getLogger(CellLocationManager.class);
	}

	private void debug(Object paramObject) {
		logger.debug(paramObject);
		// String str = String.valueOf(paramObject);
		// Toast.makeText(this.context, str, Toast.LENGTH_SHORT).show();
	}

	public int accuracy() {
		return this.accuracy;
	}

	public double latitude() {
		return this.latitude;
	}

	public double longitude() {
		return this.longitude;
	}

	public int sendCount() {
		return sendCount;
	}

	public void onLocationChanged() {
	}

	public void pause() {
		if (state > 0 && !paused) {
			looper.removeMessages(MESSAGE_BEFORE_FINISH);
			paused = true;
		}
	}
	
	public int StrToInt(String value) { 
	    try { 
	        return Integer.valueOf(value); 
	    } catch (Exception e) { 
	        return 0; 
	    } 
	} 
	public void requestUpdate() {
		if (state != STATE_READY) {
			return;
		}

		debug("Cell Location requestUpdate");

		boolean bStartScanSuccessful = false;
		CellLocation.requestLocationUpdate();// 重新定位
		state = STATE_COLLECTING;
		looper.sendEmptyMessage(MESSAGE_INITIALIZE);
		if (wifiManager.wifiManager().isWifiEnabled()) {
			bStartScanSuccessful = wifiManager.wifiManager().startScan();
		} else {
			startScanTimestamp = System.currentTimeMillis();
			if (!ENABLE_WIFI || !wifiManager.wifiManager().setWifiEnabled(true)) {
				int nDelay = 0;
				if (ENABLE_WIFI && !bStartScanSuccessful)
					nDelay = 8000;
				looper.sendEmptyMessageDelayed(MESSAGE_COLLECTING_WIFI, nDelay);
				debug("CELL UPDATE");
			} else {
				waiting4WifiEnable = true;
			}
		}
	}

	public void resume() {
		if (state > 0 && paused) {
			paused = false;
			looper.removeMessages(MESSAGE_BEFORE_FINISH);
			looper.sendEmptyMessage(MESSAGE_BEFORE_FINISH);
		}
	}

	public void start() {
		if (state <= STATE_IDLE) {
			logger.info("Starting...");
			context.registerReceiver(receiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			context.registerReceiver(receiver, new IntentFilter(
					WifiManager.WIFI_STATE_CHANGED_ACTION));
			looper = new MyLooper();
			state = STATE_READY;
			paused = false;
			waiting4WifiEnable = false;
			disableWifiAfterScan = false;
			debug("CELL LOCATION START");
			requestUpdate();
		}
	}

	public void stop() {
		if (state > STATE_IDLE) {
			context.unregisterReceiver(receiver);
			debug("CELL LOCATION STOP");
			looper = null;
			state = STATE_IDLE;
			if (disableWifiAfterScan) {
				disableWifiAfterScan = false;
				wifiManager.wifiManager().setWifiEnabled(false);
			}
		}
	}

	public long timestamp() {
		return this.timestamp;
	}

	// protected boolean isConnectedWithInternet() {
	// ConnectivityManager conManager = (ConnectivityManager)
	// context.getSystemService(Context.CONNECTIVITY_SERVICE);
	// NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
	// if (networkInfo != null) {
	// return networkInfo.isAvailable();
	// }
	// return false;
	// }
	private class MyLooper extends Handler {
		private float fCellScore;
		private JSONArray objCellTowersJson;

		public void handleMessage(Message paramMessage) {
			logger.debug("haha1");
			if (CellLocationManager.this.looper != this)
				return;
			boolean flag = true;
			switch (paramMessage.what) {
			default:
				break;
			case MESSAGE_INITIALIZE:
				logger.debug("haha2");
				logger.debug("MESSAGE_INITIALIZE");
				this.objCellTowersJson = null;
				this.fCellScore = 1.401298E-045F;
			case MESSAGE_COLLECTING_CELL:
				logger.debug("MESSAGE_COLLECTING_CELL");
				if (CellLocationManager.this.state != CellLocationManager.STATE_COLLECTING)
					break;
				logger.debug("haha3");
				JSONArray objCellTowers = CellLocationManager.this.cellInfoManager
						.cellTowers();
				float fCellScore = CellLocationManager.this.cellInfoManager
						.score();
				if (objCellTowers != null) {
					float fCurrentCellScore = this.fCellScore;
					if (fCellScore > fCurrentCellScore) {
						this.objCellTowersJson = objCellTowers;
						this.fCellScore = fCellScore;
					}
				}
				this.sendEmptyMessageDelayed(MESSAGE_COLLECTING_CELL, 600L);
				break;
			case MESSAGE_COLLECTING_WIFI:
				logger.debug("haha4");
				logger.debug("MESSAGE_COLLECTING_WIFI");
				if (CellLocationManager.this.state != CellLocationManager.STATE_COLLECTING)
					break;
				this.removeMessages(MESSAGE_COLLECTING_CELL);
				this.removeMessages(MESSAGE_BEFORE_FINISH);
				// if (CellLocationManager.this.disableWifiAfterScan &&
				// CellLocationManager.this.wifiManager.wifiManager().setWifiEnabled(true))
				// CellLocationManager.this.disableWifiAfterScan = false;
				CellLocationManager.this.state = CellLocationManager.STATE_SENDING;
				if (CellLocationManager.this.task != null)
					CellLocationManager.this.task.cancel(true);
				int[] aryCell = null;
				if (CellLocationManager.this.cellInfoManager.isGsm())
					aryCell = CellLocationManager.this.cellInfoManager
							.dumpCells();
				int nBid = CellLocationManager.this.cellInfoManager.bid();
				CellLocationManager.this.task = new CellLocationManager.CellLocationTask(
						aryCell, nBid);
				JSONArray[] aryJsonArray = new JSONArray[2];
				aryJsonArray[0] = this.objCellTowersJson;
				aryJsonArray[1] = CellLocationManager.this.wifiManager
						.wifiTowers();
				if (this.objCellTowersJson != null)
					logger.info("CellTownerJSON: "
							+ this.objCellTowersJson.toString());
				if (aryJsonArray[1] != null)
					logger
							.info("WIFITownerJSON: "
									+ aryJsonArray[1].toString());
				CellLocationManager.this.debug("Post json");
				CellLocationManager.this.task.execute(aryJsonArray);

				break;
			case MESSAGE_BEFORE_FINISH:
				logger.debug("haha5");
				logger.debug("MESSAGE_BEFORE_FINISH");
				if (CellLocationManager.this.state != CellLocationManager.STATE_READY
						|| CellLocationManager.this.paused)
					break;
				// L7
				if (CellLocationManager.this.disableWifiAfterScan
						&& CellLocationManager.this.wifiManager.wifiManager()
								.setWifiEnabled(false))
					CellLocationManager.this.disableWifiAfterScan = false;
				if (!CellLocationManager.this.cellInfoManager.isGsm()) {

					// L9
					if (CellLocationManager.this.bid == CellLocationManager.this.cellInfoManager
							.bid()) {
						flag = true;
					} else {
						flag = false;
					}
					// L14
					if (flag) {
						int DELAY_TIME_INT = StrToInt(DELAY_TIME)*1000;
						/**DELAY_TIME_INT以后，进入requestUpdate()函数**/
						CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_RE_REQUEST,DELAY_TIME_INT);
					} else {
						this.sendEmptyMessageDelayed(MESSAGE_BEFORE_FINISH,
								CellLocationManager.CHECK_INTERVAL);
					}
				} else {
					// L8
					if (CellLocationManager.this.aryGsmCells == null
							|| CellLocationManager.this.aryGsmCells.length == 0) {
						int DELAY_TIME_INT = StrToInt(DELAY_TIME)*1000;
						/**DELAY_TIME_INT以后，进入requestUpdate()函数**/
						CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_RE_REQUEST,DELAY_TIME_INT);
						logger.debug("haha115");
						// L10
						flag = true;
					} else {
						logger.debug("haha116");
						int[] aryCells = CellLocationManager.this.cellInfoManager
								.dumpCells();
						if (aryCells != null && aryCells.length != 0) {
							// L13
							int nFirstCellId = CellLocationManager.this.aryGsmCells[0];
							if (nFirstCellId == aryCells[0]) {
								// L16
								int cellLength = CellLocationManager.this.aryGsmCells.length / 2;
								List<Integer> arraylist = new ArrayList<Integer>(
										cellLength);
								List<Integer> arraylist1 = new ArrayList<Integer>(
										aryCells.length / 2);
								int nIndex = 0;
								int nGSMCellLength = CellLocationManager.this.aryGsmCells.length;
								while (nIndex < nGSMCellLength) {
									// goto L18
									arraylist
											.add(CellLocationManager.this.aryGsmCells[nIndex]);
									nIndex += 2;
								}
								// goto L17
								nIndex = 0;
								while (nIndex < aryCells.length) {
									// goto L20
									arraylist1.add(aryCells[nIndex]);
									nIndex += 2;
								}
								// goto L19
								int nCounter = 0;
								for (Iterator<Integer> iterator = arraylist
										.iterator(); iterator.hasNext();) {
									// goto L22
									if (arraylist1.contains(iterator.next()))
										nCounter++;
								}
								// goto L21
								int k4 = arraylist.size() - nCounter;
								int l4 = arraylist1.size() - nCounter;
								if (k4 + l4 > nCounter)
									flag = true;
								else
									flag = false;
								if (flag) {
									StringBuilder stringbuilder = new StringBuilder(
											"MAJOR NEIGHBORING CELL CHANGED: ");
									stringbuilder.append(k4).append(" + ");
									stringbuilder.append(l4).append(" > ");
									stringbuilder.append(nCounter);
									CellLocationManager.this
											.debug(stringbuilder.toString());

									// Fix bug to request another location
									// update when major neighboring cell
									// changed.
									/**DELAY_TIME_INT以后，进入requestUpdate()函数**/
									int DELAY_TIME_INT2 = StrToInt(DELAY_TIME)*1000;
									CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_RE_REQUEST,DELAY_TIME_INT2);

								} else {
									CellLocationManager.this
											.debug("MINOR NEIGHBORING CELL CHANGED");
									// fix bug to delay and send another message
									// later.
									this.sendEmptyMessageDelayed(
											MESSAGE_BEFORE_FINISH,
											CellLocationManager.CHECK_INTERVAL);
								}
								break;
							} else {
								// L15
								flag = true;
								CellLocationManager.this
										.debug("PRIMARY CELL CHANGED");
								// goto L14
								if (flag) {
									/**DELAY_TIME_INT以后，进入requestUpdate()函数**/
									int DELAY_TIME_INT3 = StrToInt(DELAY_TIME)*1000;
									CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_RE_REQUEST,DELAY_TIME_INT3);
								} else {
									this.sendEmptyMessageDelayed(
											MESSAGE_BEFORE_FINISH,
											CellLocationManager.CHECK_INTERVAL);
								}
							}
						} else {
							// L12
							flag = true;
							// goto L14
							if (flag) {
								/**DELAY_TIME_INT以后，进入requestUpdate()函数**/
								int DELAY_TIME_INT4 = StrToInt(DELAY_TIME)*1000;
								CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_RE_REQUEST,DELAY_TIME_INT4);
							} else {
								this.sendEmptyMessageDelayed(
										MESSAGE_BEFORE_FINISH,
										CellLocationManager.CHECK_INTERVAL);
							}
						}
					}
				}
			break;
			case MESSAGE_RE_REQUEST:
				logger.debug("loger-request()");
				state = STATE_READY;
				requestUpdate();
			break;
			}
		}
	}

	class CellLocationTask extends UserTask<JSONArray, Void, Void> {
		int accuracy;
		int bid;
		int[] cells;
		double lat;
		double lng;
		long time;

		public CellLocationTask(int[] aryCell, int bid) {
			this.time = System.currentTimeMillis();
			this.cells = aryCell;
			this.bid = bid;
		}

		public Void doInBackground(JSONArray[] paramArrayOfJSONArray) {
			try {
				logger.debug("haha6");
				sendCount++;
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("version", "1.1.0");
				jsonObject.put("host", "maps.google.com");
				jsonObject.put("address_language", "zh_CN");
				jsonObject.put("request_address", true);
				jsonObject.put("radio_type", "gsm");
				jsonObject.put("carrier", "HTC");
				JSONArray cellJson = paramArrayOfJSONArray[0];
				jsonObject.put("cell_towers", cellJson);
				// JSONArray wifiJson = paramArrayOfJSONArray[1];
				// jsonObject.put("wifi_towers", wifiJson);
				String strJson = jsonObject.toString();

				// Check the network status then try to send the json object for
				// location.
				if (NetworkStateManager.instance(context)
						.isConnectedWithInternet()) {
					try {
						DefaultHttpClient localDefaultHttpClient = new DefaultHttpClient();
						HttpPost localHttpPost = new HttpPost(
								"http://www.google.com/loc/json");
						StringEntity objJsonEntity = new StringEntity(strJson);
						localHttpPost.setEntity(objJsonEntity);
						HttpResponse objResponse = localDefaultHttpClient
								.execute(localHttpPost);
						int nStateCode = objResponse.getStatusLine()
								.getStatusCode();
						HttpEntity httpEntity = objResponse.getEntity();
						byte[] arrayOfByte = null;
						if (nStateCode / 100 == 2)
							arrayOfByte = EntityUtils.toByteArray(httpEntity);
						httpEntity.consumeContent();
						if (arrayOfByte != null) {
							String strResponse = new String(arrayOfByte,
									"UTF-8");
							jsonObject = new JSONObject(strResponse);
							this.lat = jsonObject.getJSONObject("location")
									.getDouble("latitude");
							this.lng = jsonObject.getJSONObject("location")
									.getDouble("longitude");
							this.accuracy = jsonObject
									.getJSONObject("location").getInt(
											"accuracy");
						}
					} catch (IOException ex) {
						logger.error("CellLocationManager: " + ex.getMessage());
					}
				} else {
					logger
							.error("CellLocationManager: Not connected with Internet");
				}
			} catch (Exception localException) {
				logger.error("CellLocationManager: "
						+ localException.getMessage());
				return null;
			}
			return null;
		}

		public void onPostExecute(Void paramVoid) {
			logger.debug("haha7");
			if (CellLocationManager.this.state != CellLocationManager.STATE_SENDING
					|| CellLocationManager.this.task != this)
				return;

			if ((this.lat != 0.0D) && (this.lng != 0.0D)) {
				CellLocationManager.this.timestamp = this.time;
				CellLocationManager.this.latitude = this.lat;
				CellLocationManager.this.longitude = this.lng;
				CellLocationManager.this.accuracy = this.accuracy;
				CellLocationManager.this.aryGsmCells = this.cells;
				CellLocationManager.this.bid = this.bid;
				StringBuilder sb = new StringBuilder("CELL LOCATION DONE: (");
				sb.append(this.lat).append(",").append(this.lng).append(")");
				CellLocationManager.this.debug(sb.toString());
				CellLocationManager.this.state = STATE_READY;
				CellLocationManager.this.looper.sendEmptyMessageDelayed(
						MESSAGE_BEFORE_FINISH, CHECK_INTERVAL);
				CellLocationManager.this.onLocationChanged();
			} else {
				CellLocationManager.this.task = null;
				CellLocationManager.this.state = CellLocationManager.STATE_READY;
				CellLocationManager.this.looper.sendEmptyMessageDelayed(
						MESSAGE_BEFORE_FINISH, CHECK_INTERVAL);
			}
		}
	}

	private class CellLocationManagerBroadcastReceiver extends
			BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			// access$0 state
			// 1 debug
			// access$2 loop
			// 3 startScanTimestamp
			// 4 disableWifiAfterScan
			// 5 wifimanager
			if (CellLocationManager.this.state != CellLocationManager.STATE_COLLECTING)
				return;
			String s = intent.getAction();
			if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(s)) { // goto
				// _L4;
				// else
				// goto
				// _L3
				// _L3:
				CellLocationManager.this.debug("WIFI SCAN COMPLETE");
				CellLocationManager.this.looper
						.removeMessages(MESSAGE_COLLECTING_WIFI);
				long lInterval = System.currentTimeMillis()
						- CellLocationManager.this.startScanTimestamp;
				if (lInterval > 4000L)
					CellLocationManager.this.looper.sendEmptyMessageDelayed(
							MESSAGE_COLLECTING_WIFI, 4000L);
				else
					CellLocationManager.this.looper
							.sendEmptyMessage(MESSAGE_COLLECTING_WIFI);
			} else {
				// _L4:
				if (!CellLocationManager.this.waiting4WifiEnable)
					return;
				String s1 = intent.getAction();
				if (!WifiManager.WIFI_STATE_CHANGED_ACTION.equals(s1))
					return;
				int wifiState = intent.getIntExtra(
						WifiManager.EXTRA_WIFI_STATE, 4);
				// _L5:
				if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
					// boolean flag2 =
					// CellLocationManager.this.wifiManager.wifiManager().startScan();
					// _L8:
					CellLocationManager.this.disableWifiAfterScan = true;
					CellLocationManager.this.paused = false;
					// int i = flag2 ? 1 : 0;
					// int nDelay = i != 0 ? 8000 : 0;
					// CellLocationManager.this.looper.sendEmptyMessageDelayed(MESSAGE_COLLECTING_WIFI,
					// nDelay);
					CellLocationManager.this.debug("WIFI ENABLED");
				}
			}
		}
	}

}
