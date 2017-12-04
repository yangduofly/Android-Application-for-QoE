package com.borsche.signalstrength;

	import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
	import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
	import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
	public class Map extends MapActivity
	{
		private MapView	 mMapView;
		private MapController mMapController; 
		private MyLocationOverlay myposition;
		//����LocationManager
		private LocationManager locationManager;
		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.map);
			//ȡ��LocationManagerʵ��
	        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			mMapView = (MapView) findViewById(R.id.MapView01);
			
			//����Ϊ��ͨģʽ
			mMapView.setTraffic(true);
			//����Ϊ����ģʽ
			mMapView.setSatellite(false); 
			//����Ϊ�־�ģʽ
			mMapView.setStreetView(false);
			//ȡ��MapController����(����MapView)
			mMapController = mMapView.getController(); 
			mMapView.setEnabled(true);
			mMapView.setClickable(true);
			//���õ�ͼ֧������
			mMapView.setBuiltInZoomControls(true); 
	        
			//���ñ���(1-21)
			mMapController.setZoom(12); 
			//���Overlay��������ʾ��ע��Ϣ
	        myposition = new MyLocationOverlay();
	        List<Overlay> list = mMapView.getOverlays();
	        list.add(myposition);
	        //����Criteria�������̣�����Ϣ
	        Criteria criteria =new Criteria();
	        //����Ҫ��
	        criteria.setAccuracy(Criteria.ACCURACY_FINE);
	        criteria.setAltitudeRequired(false);
	        criteria.setBearingRequired(false);
	        criteria.setCostAllowed(false);
	        criteria.setPowerRequirement(Criteria.POWER_LOW);
	        //ȡ��Ч����õ�criteria
	        String provider=locationManager.getBestProvider(criteria, true);
	        //�õ�������ص���Ϣ
	        Location location=locationManager.getLastKnownLocation(provider);
	        //��������
	        updateWithNewLocation(location);
	        //ע��һ�������Եĸ��£�3s����һ��
	        //locationListener����������λ��Ϣ�ĸı�
	        
	        
	        locationManager.requestLocationUpdates(provider, 3000, 0,locationListener);
	        
	        
		}
		private void updateWithNewLocation(Location location) 
		{
			if(location!=null)
		    {    
		    	//Ϊ���Ʊ�־������������
		        myposition.setLocation(location);
		        //ȡ��γ�Ⱥ;���
		        Double geoLat=location.getLatitude()*1E6;
		        Double geoLng=location.getLongitude()*1E6;
		        //����ת��Ϊint��
		        GeoPoint point=new GeoPoint(geoLat.intValue(),geoLng.intValue());
		        //��λ��ָ������
		        mMapController.animateTo(point);
		     }
			else
			{   
				myposition.setLocation(location);
				Double geoLat=39.9*1E6;
				Double geoLng=116.3*1E6;
				GeoPoint point=new GeoPoint(geoLat.intValue(),geoLng.intValue());
				mMapController.animateTo(point);
			}
		}
		private final LocationListener locationListener=new LocationListener()
	    {
	    	//������ı�ʱ�����˺���
	        public void onLocationChanged(Location location)
	        {
	        	updateWithNewLocation(location);
	        }
	        //Provider��disableʱ�����˺���������GPS���ر� 
	        public void onProviderDisabled(String provider)
	        {
	        	updateWithNewLocation(null);
	        }
	        //Provider��enableʱ�����˺���������GPS����
	        public void onProviderEnabled(String provider){}
	        //Provider��ת̬�ڿ��á���ʱ�����ú��޷�������״ֱ̬���л�ʱ�����˺���
	        public void onStatusChanged(String provider,int status,Bundle extras){}
	    };
		protected boolean isRouteDisplayed()
		{
			return false;
		}
		class MyLocationOverlay extends Overlay
		{
			Location mLocation;
			//�ڸ�������ʱ�����ø����꣬һ�߻�ͼ
			public void setLocation(Location location)
			{
				mLocation = location;
			}
			@Override
			public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
			{
				if(mLocation!=null)
				{	
				super.draw(canvas, mapView, shadow);
				Paint paint = new Paint();
				Point myScreenCoords = new Point();
				// ����γ��ת����ʵ����Ļ����
				GeoPoint tmpGeoPoint = new GeoPoint((int)(mLocation.getLatitude()*1E6),(int)(mLocation.getLongitude()*1E6));
				mapView.getProjection().toPixels(tmpGeoPoint, myScreenCoords);
				paint.setARGB(255, 255, 0, 0);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home);
				canvas.drawBitmap(bmp, myScreenCoords.x-32, myScreenCoords.y-45, paint);
				canvas.drawText("  �ҵ�λ��", myScreenCoords.x, myScreenCoords.y, paint);
				}
				return true;
			}
		}
		public boolean onCreateOptionsMenu(Menu menu)
		{
			//Ϊmenu�������
			super.onCreateOptionsMenu(menu);
			menu.add(0, 1, 0, "��ͨ").setIcon(R.drawable.traffic1);
			menu.add(0, 2, 0,"����").setIcon(R.drawable.satellite);
			menu.add(0, 3, 0, "�־�").setIcon(R.drawable.streetview);
			return true;
		}

		/*����menu���¼�*/
		public boolean onOptionsItemSelected(MenuItem item)
		{
			//�õ���ǰѡ�е�MenuItem��ID,
			int item_id = item.getItemId();

			switch (item_id)
			{
			case 1:
		
				mMapView.setTraffic(true);
			
				mMapView.setSatellite(false); 
				
				mMapView.setStreetView(false);
				break;
	    	case 2:
	    		mMapView.setTraffic(false);
	    		
				mMapView.setSatellite(true); 
				
				mMapView.setStreetView(false);
				break;
			case 3:
		        mMapView.setTraffic(false);
	    		
				mMapView.setSatellite(false); 
				
				mMapView.setStreetView(true);
				break;
			
			}
			return true;
		} 
		
		public boolean onKeyDown(int keyCode, KeyEvent event) {

			if (keyCode == KeyEvent.KEYCODE_BACK) {

		
				Map.this.finish();
			
					
			}

			return super.onKeyDown(keyCode, event);
		}
	}
