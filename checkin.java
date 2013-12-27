package com.tonylab;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class checkin extends Fragment {
	private LocationManager mLocationManager;               //宣告定位管理控制
	private ArrayList<Poi> Pois = new ArrayList<Poi>();   //建立List，屬性為Poi物件
    List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
    SimpleAdapter simpleAdapter;
    ListView listView;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {		
		View v = inflater.inflate(R.layout.checkin,container, false);

	    super.onCreate(savedInstanceState);
	    listView = (ListView) v.findViewById(android.R.id.list);
	    if (android.os.Build.VERSION.SDK_INT > 9) {
	    	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	        StrictMode.setThreadPolicy(policy);
	    }
	    //從DB取得所有餐廳資訊
	    ArrayList<JSONObject> result_a = null;
		DB db = new DB();
		try{
			ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("null","null"));
            result_a = db.DataSearch(nameValuePairs,"findnearres"); 
                              
            Log.i("log_act","size="+result_a.size()); 
            for(int i=0;i<result_a.size();i++){ 
            	Log.e("r_act","time"); 
                String rid = new JSONArray (result_a).getJSONObject(i).getString("Rid");
                String name = new JSONArray (result_a).getJSONObject(i).getString("Rname");
                String longitude = new JSONArray (result_a).getJSONObject(i).getString("Rlongitude");
                String latitude = new JSONArray (result_a).getJSONObject(i).getString("Rlatitude");
                double dlong = Double.parseDouble(longitude);
                double dlat = Double.parseDouble(latitude);
                int drid = Integer.parseInt(rid);
         	    //建立物件，並放入List裡 (建立物件需帶入名稱、緯度、經度)
 	    		Pois.add(new Poi(name, dlat, dlong, drid));	
            }
		}
		catch(Exception e){ 
	        Log.e("log_tag", "Error get data "+e.toString());                
	    }
	       				        
	      //取得定位權限
		mLocationManager = (LocationManager) v.getContext().getSystemService(Context.LOCATION_SERVICE);
 
	    //(若欲以GPS為定位抓取方式則更改成LocationManager.GPS_PROVIDER)
	    // 最後則帶入定位更新Listener。
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,10000.0f,LocationChange);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,10000.0f,LocationChange);
	    
	      listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
	    	  public void onItemClick(AdapterView<?> a, View v, int position, long id) {
	    		  Log.i("ClickItem", Integer.toString(position));
	    		  Map<String, Object> takeout = items.get(position);
	    		  String selectedRid = takeout.get("rid").toString();
	    		  Log.i("ClickItem", selectedRid);
	    		  //插入去過的資訊
	    		  DB db = new DB();
	    		  ArrayList<JSONObject> result_a = null;
	    		  try{
	    			  Log.i("where", "here");
	  				  ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		  			Login login = new Login();
		  			String account = login.account;
	                  nameValuePairs.add(new BasicNameValuePair("rid",selectedRid));
	                  nameValuePairs.add(new BasicNameValuePair("uid",account));
	                  Log.i("where", "here");
	                  result_a = db.DataSearch(nameValuePairs,"checkmode");
	                  Log.i("where", "here");
	                  if (result_a.size()==0){
	                	  Log.i("where", "0");
	                	  nameValuePairs.add(new BasicNameValuePair("rid",selectedRid));
		                  nameValuePairs.add(new BasicNameValuePair("uid",account));
		                  db.DataSearch(nameValuePairs,"checkinsert");
		                  Log.i("where", "0");
	                  }
	                  else{
	                	  Log.i("where", "1");
	                	  nameValuePairs.add(new BasicNameValuePair("rid",selectedRid));
		                  nameValuePairs.add(new BasicNameValuePair("uid",account));
		                  db.DataSearch(nameValuePairs,"checkmodified");
		                  Log.i("where", "1");
	                  }
	                  
	                  //Intent i = new Intent();
	                  ///i.setClass(checkin.this, ShowInfo.class);
	                  Log.i("[chechin.java]-Fragment","setting bundle");
	                  Bundle bundle = new Bundle();
	                  bundle.putString("r_id", selectedRid);
	                  
	                  /*Fragment resultFragment = new ShowInfo;
	    	          FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
	    	          resultFragment.setArguments(bundle);
	    	          
	    	          Log.i("[chechin.java]-fragment","sending to another fragment");
	    	          ft.replace(R.id.realtabcontent, resultFragment);
	                          //有紅字部分應該就可以成功傳輸bundle給下一頁了(吧)
	    	          ft.setTransition(FragmentTransaction.TRANSIT_NONE);
	    	          ft.addToBackStack(null);
	    	          ft.commit();*/

	                  //i.putExtras(bundle);
	                  //startActivity(i); 
	                  //finish();
	  			  }
	  			catch(Exception e){ 
	  	            Log.e("log_tag", "Error get data "+e.toString());                
	  	        }
	    	  }
	      });
	      return v;
	}
	
	public void afterLocation()
	{
		simpleAdapter = new SimpleAdapter(getActivity().getBaseContext(), items, 
	    		   R.layout.checkinlist, new String[]{"name", "distance"},
	    		   new int[]{R.id.textView_name, R.id.textView_distance});
	      listView.setAdapter(simpleAdapter);
	}

	//更新定位Listener
	public LocationListener LocationChange = new LocationListener() 
	{
	     public void onLocationChanged(Location mLocation) 
		     {
		      for(Poi mPoi : Pois)   
		      {
		          //for迴圈將距離帶入，判斷距離為Distance function
		          //需帶入使用者取得定位後的緯度、經度、景點店家緯度、經度。 
		        mPoi.setDistance(Distance(mLocation.getLongitude(),
		                                  mLocation.getLatitude(),
		                                  mPoi.getLongitude(),
		                                  mPoi.getLatitude()));
		        }
		           
		      //依照距離遠近進行List重新排列
		        DistanceSort(Pois);
	
		      //印出我的座標-經度緯度
		      Log.d("TAG", "我的座標 - 經度 : " + mLocation.getLongitude() + "  , 緯度 : " + mLocation.getLatitude() );
		      //for迴圈，印出景點店家名稱及距離，並依照距離由近至遠排列
		      //第一筆為最近的景點店家，最後一筆為最遠的景點店家
		       for(int i = 0 ; i < Pois.size() ; i++ )
		       {
		          Log.d("TAG", "地點 : " + Pois.get(i).getName() + "  , 距離為 : " + DistanceText(Pois.get(i).getDistance()) );
	
		          Map<String, Object> item = new HashMap();
		          item.put("name", Pois.get(i).getName());
		       	  item.put("distance", DistanceText(Pois.get(i).getDistance()));
		       	  item.put("rid", Pois.get(i).getRid());
		       	  items.add(item);
		       }
		       afterLocation();
		       
		       
		    }

	    public void onProviderDisabled(String provider) 
	    {
	    }
	         
	    public void onProviderEnabled(String provider) 
	    {
	    }
	         
	    public void onStatusChanged(String provider, int status,Bundle extras) 
	    {
	    }
	};

	//original privacy is "protected" ,I changed it to "public" for debugging (show,2013/12/09)
	@Override
	public void onDestroy() 
	{
	   super.onDestroy();
	   mLocationManager.removeUpdates(LocationChange);  //程式結束時停止定位更新
	}

	//帶入距離回傳字串 (距離小於一公里以公尺呈現，距離大於一公里以公里呈現並取小數點兩位)
	private String DistanceText(double distance)
	{
	   if(distance < 1000 ) return String.valueOf((int)distance) + "m" ;
	   else return new DecimalFormat("#.00").format(distance/1000) + "km" ;
	}

	//List排序，依照距離由近開始排列，第一筆為最近，最後一筆為最遠
	private void DistanceSort(ArrayList<Poi> poi)
	{
	   Collections.sort(poi, new Comparator<Poi>() 
	   {
	      @Override
	      public int compare(Poi poi1, Poi poi2) 
	      {
	          return poi1.getDistance() < poi2.getDistance() ? -1 : 1 ;
	      }
	   });
	}

	//帶入使用者及景點店家經緯度可計算出距離
	public double Distance(double longitude1, double latitude1, double longitude2,double latitude2) 
	{
	   double radLatitude1 = latitude1 * Math.PI / 180;
	   double radLatitude2 = latitude2 * Math.PI / 180;
	   double l = radLatitude1 - radLatitude2;
	   double p = longitude1 * Math.PI / 180 - longitude2 * Math.PI / 180;
	   double distance = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(l / 2), 2)
	                    + Math.cos(radLatitude1) * Math.cos(radLatitude2)
	                    * Math.pow(Math.sin(p / 2), 2)));
	   distance = distance * 6378137.0;
	   distance = Math.round(distance * 10000) / 10000;

	   return distance ;
	}

	/*** 印出的結果為 : 
	      * 
	      * 我的座標 - 經度 : 121.56024  , 緯度 : 25.03935
	      * 地點 : 台北101  , 距離為 : 626m
	      * 地點 : 台北車站  , 距離為 : 4.85km
	      * 地點 : 九份老街  , 距離為 : 31.99km
	      * 地點 : 台中車站  , 距離為 : 110.40km
	      * 地點 : 高雄85大樓  , 距離為 : 197.37km
	      * 
	*/
}
