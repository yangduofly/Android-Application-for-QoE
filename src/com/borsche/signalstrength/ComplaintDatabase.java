package com.borsche.signalstrength;

import org.apache.log4j.Logger;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class ComplaintDatabase extends SQLiteOpenHelper  {

    private final static String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/QoeImprovment/qoe.db";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "complaint_table";
    
    private Logger 	logger = Logger.getLogger("ComplaintDatabase");
    
	public ComplaintDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
        		"id INTEGER primary key AUTOINCREMENT, " +
        		  "os_type text NOT NULL, " +
        		  "os_version text NOT NULL, " +
        		  "manufacturer text NOT NULL, " +
        		  "model text NOT NULL, " +
        		  "time_stamp INTEGER NOT NULL, " +
        		  "cell_id INTEGER NOT NULL, " +
        		  "mobile_country_code text NOT NULL, " +
        		  "mobile_network_code text NOT NULL, " +
        		  "location_area_code INTEGER NOT NULL, " +
        		  "network_type INTEGER NOT NULL, " +
        		  "signal_strength_dbm INTEGER NOT NULL, " +
        		  "longtitude REAL NOT NULL, " +
        		  "latitude REAL NOT NULL, " +
        		  "neighboring_cells text, " +
        		  "accuracy INTEGER NOT NULL, " +
        		  "imei text NOT NULL, " +
        		  "complaint_type INTEGER NOT NULL, " +
        		  "imsi text NOT NULL, " +
        		  "complaint_text text" +
        		  ");";
        
        try{
        	db.execSQL(sql);
        } catch (SQLException ex) {
        	logger.error("Error when create table", ex);
        }
        	
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
        try{
        	db.execSQL(sql);
        } catch (SQLException ex) {
        	logger.error("Error when drop table", ex);
        }
        onCreate(db);
	}
	
	public QoeComplaint[] selectall(){
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		
		int count = cursor.getCount();
		QoeComplaint[] complaintList = new QoeComplaint[count];
		int index = 0;
		while(cursor.moveToNext()){
			QoeComplaint complaint = new QoeComplaint();
			complaint.setTimeStamp(cursor.getLong(5));
			complaint.setCellId(cursor.getInt(6));
			complaint.setMCC(cursor.getInt(7));
			complaint.setMNC(cursor.getInt(8));
			complaint.setCellLac(cursor.getInt(9));
			complaint.setNetworkType(cursor.getInt(10));
			complaint.setSignalStrength(cursor.getInt(11));
			complaint.setLongtitude(cursor.getDouble(12));
			complaint.setLatitude(cursor.getDouble(13));
			complaint.setNeiboringCells(cursor.getString(14));
			complaint.setAccuracy(cursor.getInt(15));
			complaint.setIMEI(cursor.getString(16));
			complaint.setComplaintType(cursor.getInt(17));
			complaint.setIMSI(cursor.getString(18));
			complaint.setComplaintText(cursor.getString(19));
			
			complaintList[index++] = complaint;
		}
		
		return complaintList;
	}
	
    public long insert(QoeComplaint complaint) {
        SQLiteDatabase db = this.getWritableDatabase();
        long row = db.insert(TABLE_NAME, null, complaint.toContentValues());
        return row;
    }
 
    public void clear() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
    }
}
