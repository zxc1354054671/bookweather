package com.bw.bookweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.bw.bookweather.json.WeatherJson;
import com.bw.bookweather.util.HttpUtil;
import com.bw.bookweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    private static final String TAG = "AutoUpdateService---";
    public AutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingImg();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 3 * 60 * 60 * 1000;
        long triggerTime = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this, AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String weatherStr = sp.getString("weather", null);
        String countyCode;
        if (!TextUtils.isEmpty(weatherStr)) {
            //有缓存时
            WeatherJson weather = Utility.handleWeatherJsonResponse(weatherStr);
            countyCode = weather.cityJson.code;
            String url="http://t.weather.sojson.com/api/weather/city/"+countyCode;
            HttpUtil.sendRequest(url, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(getApplicationContext(), "获取天气失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                     String weatherStr = response.body().string();
                     WeatherJson weather = Utility.handleWeatherJsonResponse(weatherStr);
                    if (weather != null && "200".equals(weather.status)) {
                        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        edit.putString("weather", weatherStr);
                        edit.apply();
                    } else {
                        Toast.makeText(getApplicationContext(), "获取天气失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
//            countyCode = getIntent().getStringExtra("countyCode");
//            weatherLayout.setVisibility(View.INVISIBLE);
//            requestWeather(countyCode);
        }
    }

    public void updateBingImg() {
        String requestBingImg="http://guolin.tech/api/bing_pic";
        HttpUtil.sendRequest(requestBingImg, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
//                        }
//                    });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String responseStr = response.body().string();
                SharedPreferences.Editor edit= PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                edit.putString("bingImg", responseStr);
                edit.apply();
            }
        });
    }

}
