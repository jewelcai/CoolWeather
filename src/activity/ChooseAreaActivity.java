package activity;

import java.util.ArrayList;
import java.util.List;

import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import model.*;

import com.example.coolweather.R;





import db.CoolWeatherDB;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseAreaActivity extends ActionBarActivity {
	public static final int LEVEL_PROVINCE=0;
	public static final int LEVEL_CITY=1;
	public static final int LEVEL_COUNTY=2;
  private ProgressDialog progressDialog;
  private ListView listView;
  private ArrayAdapter<String> adapter;
  private TextView titleText;
  private List<String> dataList= new ArrayList<String>();
  private CoolWeatherDB coolweatherDb ;
  private int currentLevel;
  private List<Province> provinceList;
  private List<City> cityList;
  private List<County> countyList;
  private Province selectedProvince;
  private City selectedCity;
  //private County selectedCounty;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if(prefs.getBoolean("city_selected", false))
		{
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return ;
			
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText= (TextView) findViewById(R.id.title_text);
		adapter= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		coolweatherDb= CoolWeatherDB.getInstance(this);
		listView.setOnItemClickListener( new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel==LEVEL_PROVINCE)
				{
					selectedProvince= provinceList.get(position);
					queryCities();
				}else if(currentLevel==LEVEL_CITY){
					selectedCity=cityList.get(position);
					queryCounties();
					
				}else if(currentLevel==LEVEL_COUNTY)
				{
					String countyCode = countyList.get(position).getCountyCode();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("county_code", countyCode);
					startActivity(intent);
					finish();
					
				}
			}
		});
		queryProvinces();
		
	}
   private void queryProvinces()
   {
	   provinceList= coolweatherDb.loadProvince();
	   if(provinceList.size()>0)
	   {
		  dataList.clear(); 
		  for(Province province:provinceList)
		  {
			  dataList.add(province.getProvinceName());
			  
		  }
		   adapter.notifyDataSetChanged();
		   listView.setSelection(0);
		   titleText.setText("ÖÐ¹ú");
		   currentLevel= LEVEL_PROVINCE;
		   
	   }else{
		   queryFromServer(null,"province");
		   
	   }
	   
   }
   private void queryCities()
   {
	   cityList = coolweatherDb.loadCities(selectedProvince.getId());
	   if(cityList.size()>0)
	   {
		   dataList.clear();
		   for(City city:cityList)
		   {
			   dataList.add(city.getCityName());
			   
			   
		   }
		   adapter.notifyDataSetChanged();
		   listView.setSelection(0);
		   titleText.setText(selectedProvince.getProvinceName());
		   currentLevel=LEVEL_CITY;
	   }else{
		   queryFromServer(selectedProvince.getProvinceCode(),"city");
		   
	   }
	   
   }
   
   private void queryCounties()
   {
	   countyList = coolweatherDb.loadCounties(selectedCity.getId());
	   if(countyList.size()>0)
	   {
		   dataList.clear();
		   for(County county:countyList)
		   {
			   dataList.add(county.getCountyName());
			   
		   }
		   adapter.notifyDataSetChanged();
		   listView.setSelection(0);
		   titleText.setText(selectedCity.getCityName());
		   currentLevel= LEVEL_COUNTY;
		   
		   
	   }else{
		   queryFromServer(selectedCity.getCityCode(),"county");
		   
	   }
	   
	   
   }
   
   private void queryFromServer(final String code,final String type)
   {
	   String address;
	   if(!TextUtils.isEmpty(code))
	   {
		   address= "http://www.weather.com.cn/data/list3/city"+code+".xml";
	   }else{
		   address="http://www.weather.com.cn/data/list3/city.xml";
		   
	   }
	   showProgressDialog();
	   HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
		
		@Override
		public void onFinish(String response) {
			// TODO Auto-generated method stub
			boolean result =false;
			if(type.equals("province"))
			{
				result= Utility.handleProvinceResponse(coolweatherDb, response);
			}else if(type.equals("city")) {
				
				result= Utility.handleCitiesResponse(coolweatherDb, response, selectedProvince.getId());
			}else if(type.equals("county"))
			{
				result = Utility.handleCountyResponse(coolweatherDb, response, selectedCity.getId());
				
			}
			
			if(result)
			{
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
					closeProgressDialog();
					if(type.equals("province"))
					{
						queryProvinces();
						
					}else if(type.equals("city"))
					{
						queryCities();
					}else if(type.equals("county"))
					{
						queryCounties();
					}
					}
				});
				
			}
		}
		
		@Override
		public void onError(Exception e) {
			// TODO Auto-generated method stub
			runOnUiThread( new Runnable() {
				public void run() {
			     		closeProgressDialog();
					Toast.makeText(ChooseAreaActivity.this, "failure", Toast.LENGTH_SHORT).show();
				}
			});
		}
	});
   }
   
 private void showProgressDialog()
 {
	 if(progressDialog==null)
	 {
		 progressDialog = new ProgressDialog(this);
		 progressDialog.setMessage("Loading");
		 progressDialog.setCanceledOnTouchOutside(false);
	 }
	 progressDialog.show();
 }
 
 private void closeProgressDialog()
 {
	 if(progressDialog!=null)
	 {
		 progressDialog.dismiss();
	 }
	 
 }
 
 public void onBackPressed()
 {
	 if(currentLevel==LEVEL_COUNTY)
	 {
		 queryCities();
	 }
	 else if(currentLevel==LEVEL_CITY)
	 {
		 queryProvinces();
	 }
	 else{
		 finish();
	 }
 }

}
