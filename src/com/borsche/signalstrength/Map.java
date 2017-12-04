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
		//定义LocationManager
		private LocationManager locationManager;
		/** Called when the activity is first created. */
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.map);
			//取得LocationManager实例
	        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
			mMapView = (MapView) findViewById(R.id.MapView01);
			
			//设置为交通模式
			mMapView.setTraffic(true);
			//设置为卫星模式
			mMapView.setSatellite(false); 
			//设置为街景模式
			mMapView.setStreetView(false);
			//取得MapController对象(控制MapView)
			mMapController = mMapView.getController(); 
			mMapView.setEnabled(true);
			mMapView.setClickable(true);
			//设置地图支持缩放
			mMapView.setBuiltInZoomControls(true); 
	        
			//设置倍数(1-21)
			mMapController.setZoom(12); 
			//添加Overlay，用于显示标注信息
	        myposition = new MyLocationOverlay();
	        List<Overlay> list = mMapView.getOverlays();
	        list.add(myposition);
	        //设置Criteria（服务商）的信息
	        Criteria criteria =new Criteria();
	        //经度要求
	        criteria.setAccuracy(Criteria.ACCURACY_FINE);
	        criteria.setAltitudeRequired(false);
	        criteria.setBearingRequired(false);
	        criteria.setCostAllowed(false);
	        criteria.setPowerRequirement(Criteria.POWER_LOW);
	        //取得效果最好的criteria
	        String provider=locationManager.getBestProvider(criteria, true);
	        //得到坐标相关的信息
	        Location location=locationManager.getLastKnownLocation(provider);
	        //更新坐标
	        updateWithNewLocation(location);
	        //注册一个周期性的更新，3s更新一次
	        //locationListener用来监听定位信息的改变
	        
	        
	        locationManager.requestLocationUpdates(provider, 3000, 0,locationListener);
	        
	        
		}
		private void updateWithNewLocation(Location location) 
		{
			if(location!=null)
		    {    
		    	//为绘制标志的类设置坐标
		        myposition.setLocation(location);
		        //取得纬度和经度
		        Double geoLat=location.getLatitude()*1E6;
		        Double geoLng=location.getLongitude()*1E6;
		        //将其转换为int型
		        GeoPoint point=new GeoPoint(geoLat.intValue(),geoLng.intValue());
		        //定位到指定坐标
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
	    	//当坐标改变时触发此函数
	        public void onLocationChanged(Location location)
	        {
	        	updateWithNewLocation(location);
	        }
	        //Provider被disable时触发此函数，比如GPS被关闭 
	        public void onProviderDisabled(String provider)
	        {
	        	updateWithNewLocation(null);
	        }
	        //Provider被enable时触发此函数，比如GPS被打开
	        public void onProviderEnabled(String provider){}
	        //Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
	        public void onStatusChanged(String provider,int status,Bundle extras){}
	    };
		protected boolean isRouteDisplayed()
		{
			return false;
		}
		class MyLocationOverlay extends Overlay
		{
			Location mLocation;
			//在更新坐标时，设置该坐标，一边画图
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
				// 将经纬度转换成实际屏幕坐标
				GeoPoint tmpGeoPoint = new GeoPoint((int)(mLocation.getLatitude()*1E6),(int)(mLocation.getLongitude()*1E6));
				mapView.getProjection().toPixels(tmpGeoPoint, myScreenCoords);
				paint.setARGB(255, 255, 0, 0);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.home);
				canvas.drawBitmap(bmp, myScreenCoords.x-32, myScreenCoords.y-45, paint);
				canvas.drawText("  我的位置", myScreenCoords.x, myScreenCoords.y, paint);
				}
				return true;
			}
		}
		public boolean onCreateOptionsMenu(Menu menu)
		{
			//为menu添加内容
			super.onCreateOptionsMenu(menu);
			menu.add(0, 1, 0, "交通").setIcon(R.drawable.traffic1);
			menu.add(0, 2, 0,"卫星").setIcon(R.drawable.satellite);
			menu.add(0, 3, 0, "街景").setIcon(R.drawable.streetview);
			return true;
		}

		/*处理menu的事件*/
		public boolean onOptionsItemSelected(MenuItem item)
		{
			//得到当前选中的MenuItem的ID,
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
