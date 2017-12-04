package com.borsche.signalstrength;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import org.apache.log4j.Logger;

public class QoeDataManager extends Observable implements Observer {
	
	private ComplaintDatabase db;
	private CellInfoManager cellManager;
	private WifiInfoManager wifiManager;
	private LocationManager locationManager;
	private BestLocationListener locationListener;
	private Location mLastLocation;
	private Context context;
    private static final int STATE_IDLE = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_FXIED = 2;
    private static final int MESSAGE_NEXT_LOOP = 1;
    private static final int REASON_CELL_CHANGED = 100;
    private static final int REASON_LOCATION_CHANGED = 101;
    private static final int REASON_MANUAL_POST = 102;
    private static final int REASON_NEXT_LOOP_TIMEOUT = 103;
   // private int mLoopPeriod = 15 * 60 * 1000;
    private int mLoopPeriod = 15 * 1000;
    private int mState;
    private MyLooper looper;
    private SendDataTask task;
    private Logger logger;
    private String mServerIpAddress;
    private int mServerPort;
    public static String feedinfo;
    public static double Threshold_Max = -90;
	
	public QoeDataManager(Context paramContext){
		db = new ComplaintDatabase(paramContext);
		System.out.println("haha168");
		cellManager = new CellInfoManager(paramContext){

			@Override
			public void onCellChanged() {
				System.out.println("haha166");
				System.out.println("haha+"+mState);
				if(mState == STATE_FXIED){
					System.out.println("haha167");
					// Only send after location fixed.
					Message msg = new Message();
					msg.what = MESSAGE_NEXT_LOOP;
					msg.arg1 = 0;
					msg.arg2 = REASON_CELL_CHANGED;
					looper.sendMessage(msg);
				}
			}
			
		};
		wifiManager = new WifiInfoManager(paramContext);
		locationManager = (LocationManager)paramContext.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new BestLocationListener(cellManager, wifiManager, paramContext);
		context = paramContext;
		logger = Logger.getLogger("QoeDataManager");
		mLastLocation = null;
		mState = STATE_IDLE;
	}
	
	public void setServerIpAndPort(String ipAddress, int port){
		mServerIpAddress = ipAddress;
		mServerPort = port;
	}
	
	public void setPeriod(int period){
		mLoopPeriod = period * 1000;
	}
	
	public void manualPostData(int complainType){
		manualPostData(complainType, null);
	}
	
	public int sendCount() {
		 System.out.println("test5"+locationListener.sendCount());
		return locationListener.sendCount();
	}
	
	public void manualPostData(int complainType, String complaintText){
	 if(mState == STATE_IDLE || looper == null)
	 {	 return;}
		Message msg = new Message();
		msg.what = MESSAGE_NEXT_LOOP;
		msg.arg1 = complainType;
		msg.arg2 = REASON_MANUAL_POST;
		if(complaintText != null){
			msg.obj = complaintText;
		}
		looper.sendMessage(msg);
	}
	
	public void start(){
		System.out.println("haha199");
		if(mState <= STATE_IDLE){
			System.out.println("haha130");
			locationListener.register(locationManager, true, context);
			locationListener.addObserver(this);}
		looper = new MyLooper();
		}
	
	public void start1(){
		System.out.println("haha163");
			if(mState <= STATE_IDLE){
				System.out.println("haha164");
				locationListener.addObserver(this);
				System.out.println("haha165");
				looper = new MyLooper();
				System.out.println("haha1690");
			}
	}
	
	public void stop(){
		locationListener.unregister(locationManager);
		if(this.task != null){
			this.task.cancel(true);
		}
		mState = STATE_IDLE;
		looper = null;
	}

	public void update(Observable observable, Object data) {
		System.out.println("haha171");
		if(mLastLocation == null){
			System.out.println("haha172");
			if(mState <= STATE_IDLE){
				System.out.println("haha173");
				mState = STATE_READY;
			}
		}
		mLastLocation = (Location)data;
		if(mState != STATE_FXIED){
			System.out.println("haha174");
			mState = STATE_FXIED;
			logger.debug("First location update, location fixed");
			setChanged();
			System.out.println("QOEYangduo");
			notifyObservers();
			Message msg = new Message();
			msg.what = MESSAGE_NEXT_LOOP;
			msg.arg1 = 0;
			msg.arg2 = REASON_LOCATION_CHANGED;
			looper.sendMessage(msg);
		}
	}
	
	protected String getReasonString(int reason){
		switch(reason){
		case REASON_CELL_CHANGED:
			return "CELL CHANGED";
		case REASON_LOCATION_CHANGED:
			return "LOCATION CHANGED";
		case REASON_MANUAL_POST:
			return "MANUAL POST";
		case REASON_NEXT_LOOP_TIMEOUT:
			return "NEXT LOOP TIMEOUT";
		default:
			return "UNKNOWN";
		}
	}
	
    protected boolean isConnectedWithInternet() {
		System.out.println("haha175");
        ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conManager.getActiveNetworkInfo();
        if (networkInfo != null) {
               return networkInfo.isAvailable();
        }
        return false;
    }
	
	private QoeComplaint createNewComplaint(int complaintType, String complaintText) {
		System.out.println("haha176");
		
		QoeComplaint complaint = new QoeComplaint();
		complaint.setTimeStamp(System.currentTimeMillis());
		complaint.setIMEI(QoeDataManager.this.cellManager.imei());
		complaint.setIMSI(QoeDataManager.this.cellManager.imsi());
		complaint.setNetworkType(QoeDataManager.this.cellManager.networkType());
		complaint.setMCC(QoeDataManager.this.cellManager.mcc());
		complaint.setMNC(QoeDataManager.this.cellManager.mnc());
		complaint.setCellId(QoeDataManager.this.cellManager.cid());
		complaint.setCellLac(QoeDataManager.this.cellManager.lac());
		complaint.setSignalStrength(CellInfoManager.dBm(QoeDataManager.this.cellManager.asu()));
		JSONArray neighborCellArray = QoeDataManager.this.cellManager.neighborCellInfo();
		if(neighborCellArray != null){
			complaint.setNeiboringCells(QoeDataManager.this.cellManager.neighborCellInfo().toString());
		}
		String strLatitude = new java.text.DecimalFormat("#.00000").format(QoeDataManager.this.mLastLocation.getLatitude());
		complaint.setLatitude(Double.parseDouble(strLatitude));
		String strLogtitude = new java.text.DecimalFormat("#.00000").format(QoeDataManager.this.mLastLocation.getLongitude());
		complaint.setLongtitude(Double.parseDouble(strLogtitude));
		if(QoeDataManager.this.mLastLocation.hasAccuracy()){
			complaint.setAccuracy((int)QoeDataManager.this.mLastLocation.getAccuracy());
		}
		complaint.setComplaintType(complaintType);
		complaint.setComplaintText(complaintText);
		
		return complaint;
	}

	private void postComplaintUdp(QoeComplaint complaint) {
		System.out.println("haha152");
		JSONObject objectToSend = complaint.toJsonObject();
		QoeDataManager.this.logger.debug(objectToSend);
		
		// Check the network status then try to send the json object
		if(NetworkStateManager.instance(context).isConnectedWithInternet()){
			// Send JSON data to server via UDP package.
			System.out.println("haha153");
		    DatagramSocket s = null;  
		    try {
		        s = new DatagramSocket();  
		    } catch (SocketException e) {  
		    	QoeDataManager.this.logger.error(e.getMessage());
		    }
		    
		    InetAddress address = null;
		    try {  
		        address = InetAddress.getByName(mServerIpAddress);
				System.out.println("haha154");
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				//!!!!!!!!!!!!!!!!!!!!!!!!!!
				//!!!!!!!!!!!!!!!!!!!!!!!!
				//!!!!!!!!!!!!!!!!!!!!!!!!!!!
		        byte[] buffer = objectToSend.toString().getBytes();
		        System.out.println("haha+"+buffer+"Bytes");
		        DatagramPacket p = new DatagramPacket(buffer, buffer.length, address, mServerPort);  
		        s.send(p);
		    } catch (UnknownHostException e) {
		    	QoeDataManager.this.logger.error(e.getMessage());
		    } catch (IOException e) {  
		    	QoeDataManager.this.logger.error(e.getMessage());
		    }
		}else{
			System.out.println("haha155");
			logger.error("Not connected with Internet");
		}
	}

	private class MyLooper extends Handler {
		public void handleMessage(Message paramMessage) {
			System.out.println("haha136");
			if(paramMessage == null || mState != STATE_FXIED || QoeDataManager.this.looper != this)
				{System.out.println("haha142");
				return;}
			System.out.println("haha143");
		//	logger.debug("haha12");
			switch(paramMessage.what){
				default:
					System.out.println("haha144");
					break;
				case MESSAGE_NEXT_LOOP:
					if(QoeDataManager.this.task != null)
					QoeDataManager.this.task.cancel(true);
					QoeDataManager.this.task = new QoeDataManager.SendDataTask(paramMessage.arg1, paramMessage.obj);
					QoeDataManager.this.logger.info(new StringBuilder().append("Post JSON to server, reason: ").append(getReasonString(paramMessage.arg2)));
					System.out.println("haha145");
					QoeDataManager.this.task.execute();

					break;
			}
		}
	}
	
    private class SendDataTask extends UserTask<Void, Void, Void> {
    	//logger.debug("hah");
    	//System.out.println("");
    	private int mComplaintType = 0;
    	private String mComplaintText = null;
        public SendDataTask(int complaintType, Object complaintText) {
			System.out.println("haha146");
        	mComplaintType = complaintType;
        	mComplaintText = (String)complaintText;
        }
        public Void doInBackground(Void... paramVoid) {
			System.out.println("haha147");
				
			QoeComplaint complaint = createNewComplaint(mComplaintType, mComplaintText);
			//logger.debug("haha103");
			//Toast.makeText(QoeDataManager.this.context, R.string.test_QOE, Toast.LENGTH_SHORT).show();
			if(CellInfoManager.dBm(QoeDataManager.this.cellManager.asu()) < Threshold_Max){
				System.out.println("haha148");
				db.insert(complaint);
			}else{
				postComplaintUdp(complaint);
				System.out.println("haha149");
				QoeComplaint[] complaintList = db.selectall();
				if(complaintList != null && complaintList.length > 0){
					System.out.println("haha150");
					for(int i=0; i<complaintList.length; i++){
						postComplaintUdp(complaintList[i]);
					}
				}
				
				db.clear();
			}

			return null;
        }
        public void onPostExecute(Void paramVoid) {
			System.out.println("haha151");
	        QoeDataManager.this.locationListener.updateLastKnownLocation(locationManager);
			Message msg = new Message();
			//logger.debug("haha104");
			msg.what = MESSAGE_NEXT_LOOP;
			msg.arg1 = 0;
			msg.arg2 = REASON_NEXT_LOOP_TIMEOUT;
			QoeDataManager.this.looper.sendMessageDelayed(msg, mLoopPeriod);
	        if(mComplaintType > 0){
				Toast.makeText(QoeDataManager.this.context, R.string.successful_sendout, Toast.LENGTH_SHORT).show();
        	}
        }
    }
}
