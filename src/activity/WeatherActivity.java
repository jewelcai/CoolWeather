package activity;
import util.HttpCallbackListener;
import util.HttpUtil;
import util.Utility;
import android.widget.*;

import com.example.coolweather.R;


import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class WeatherActivity extends ActionBarActivity {
   private LinearLayout weatherInfoLayout;
   private TextView cityNameText;
   private TextView publishText;
   private TextView weatherDespText;
   private TextView temp1Text;
   private TextView temp2Text;
   private TextView currentDateText;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		weatherInfoLayout= (LinearLayout) findViewById(R.id.weather_info_layout);
		cityNameText= (TextView) findViewById(R.id.city_name);
		publishText = (TextView) findViewById(R.id.publish_text);
		weatherDespText= (TextView) findViewById(R.id.weather_desp);
		temp1Text = (TextView) findViewById(R.id.temp1);
		temp2Text= (TextView) findViewById(R.id.temp2);
		currentDateText = (TextView) findViewById(R.id.current_date);
		String countyCode = getIntent().getStringExtra("county_code");
		
		if(!TextUtils.isEmpty(countyCode))
		{
			
			publishText.setText("Syching");
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			cityNameText.setVisibility(View.INVISIBLE);
			//查询天气代码
			queryWeatherCode(countyCode);
			
		}else{
			//没有县级代号就直接显示本地的天气
			showWeather();
		}
	}
	private void queryWeatherCode(String countyCode)//query weather code
	{
		String address = "http://www.weather.com.cn/data/list3/city"
				+countyCode+ ".xml";
		queryFromServer(address,"countyCode");
		
	}
	
   // weather code get , query weather info
	
	private void queryWeatherInfo(String weatherCode)
	{
		String address= "http://www.weather.com.cn/data/cityinfo"
				+ weatherCode+".html";
		queryFromServer(address,"weatherCode");
		
	}
	
	/**
	 * query weather code or weather info based on address and type
	 */
	private void queryFromServer(final String address, final String type)
	{
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish( final String response) {
				// TODO Auto-generated method stub
				if(type.equals("countyCode"))
				{
					if(!TextUtils.isEmpty(response))
					{
						String [] array = response.split("\\|");
						if(array!=null&&array.length==2)
						{
							
							String weatherCode = array[1];
							queryWeatherInfo(weatherCode);
						}
					}
					
				}else if (type.equals("weatherCode"))
				{
					Utility.handleWeatherResponse(WeatherActivity.this, response);
					runOnUiThread(new  Runnable() {
						public void run() {
							showWeather();
						}
					});
					
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						publishText.setText("failure");
					}
				});
			}
		});
		
	}
	
	private void showWeather()
	{
		SharedPreferences prefs = 
				PreferenceManager.getDefaultSharedPreferences(this);
		cityNameText.setText(prefs.getString("city_name", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		publishText.setText("today"+prefs.getString("publisn_time", "")+"published");
		currentDateText.setText(prefs.getString("current_date", ""));
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		
	}
	
}
