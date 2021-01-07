package xyz.yxy.weather;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    String responseData;
    private void request_get_weather_json(){//sendRequestWithOkHttp
        SharedPreferences pref = getSharedPreferences("weather_"+city_code,MODE_PRIVATE);
        String res_data=pref.getString("weather","");
        if(res_data!=""){
            Log.d("res_data",res_data);
            responseData=res_data;
            parseJSONWithGSON();
        }
        else {
            Toast.makeText(WeatherActivity.this,"联网更新天气",Toast.LENGTH_SHORT).show();
            request_get_weather_json_from_url();
        }
    }

    public WeatherInfo weather_data;
    public void parseJSONWithGSON(){
        Gson gson=new Gson();
        weather_data=gson.fromJson(responseData,new TypeToken<WeatherInfo>(){}.getType());
    }

    private void request_get_weather_json_from_url(){
        String url="http://t.weather.sojson.com/api/weather/city/"+city_code;
        HttpUtil.sendOkHttpRequest(url,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WeatherActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences pref = getSharedPreferences("weather_"+city_code,MODE_PRIVATE);
                                String res_data=pref.getString("weather","");
                                if(res_data!=""){
                                    Log.d("res_data",res_data);
                                    responseData=res_data;
                                    parseJSONWithGSON();
                                }
                                set_textview_text();
                                Toast.makeText(WeatherActivity.this,"请检查网络连接",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseData=response.body().string();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(System.currentTimeMillis());
                String time_now=simpleDateFormat.format(date);
                SharedPreferences.Editor editor=getSharedPreferences("weather_"+city_code,MODE_PRIVATE).edit();
                editor.putString("weather",responseData);
                editor.putString("time_last_update",time_now);
                editor.apply();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        WeatherActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                parseJSONWithGSON();
                                set_textview_text();
                                Toast.makeText(WeatherActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    String city_code;
    Button button_refresh;
    Button button_star;
    TextView textview_city_name;
    TextView textview_time;
    TextView textview_date;
    TextView textview_pm25;
    TextView textview_pm10;
    TextView textview_shidu;
    TextView textview_quality;
    TextView textview_temperature;
    TextView textview_notice;
    TextView textview_yesterday_u;
    TextView textview_yesterday_d;
    TextView textview_today_u;
    TextView textview_today_d;
    TextView textview_day2_u;
    TextView textview_day2_d;
    TextView textview_day3_u;
    TextView textview_day3_d;
    TextView textview_day4_u;
    TextView textview_day4_d;
    TextView textview_day5_u;
    TextView textview_day5_d;

    public void set_textview_text(){
        if(weather_data!=null){
            if(weather_data.status==200){
                WeatherActivity.this.setTitle(weather_data.cityInfo.parent+"  "+weather_data.cityInfo.cityId);
                textview_city_name.setText(weather_data.cityInfo.city);
                textview_time.setText("更新时间："+weather_data.cityInfo.updateTime);
                textview_date.setText("更新日期："+weather_data.date);
                textview_pm25.setText(String.valueOf("PM2.5："+weather_data.data.pm25));
                textview_pm10.setText(String.valueOf("PM10："+weather_data.data.pm10));
                textview_shidu.setText("湿度："+weather_data.data.shidu);
                textview_quality.setText("空气质量："+weather_data.data.quality);
                textview_temperature.setText(weather_data.data.wendu+"℃");
                Forecast[] forc=new Forecast[5];
                int i=0;
                for(Forecast forecast:weather_data.data.forecastList){
                    forc[i]=forecast;
                    i++;
                }
                textview_notice.setText("Tips："+forc[0].notice);

                int len=weather_data.data.yesterday.ymd.length();
                textview_yesterday_u.setText(weather_data.data.yesterday.ymd.substring(5,len)+"\n\n"+weather_data.data.yesterday.type+"\n\n"+"风力:"+"\n"+weather_data.data.yesterday.fl);
                textview_yesterday_d.setText("高温:\n"+weather_data.data.yesterday.high.replaceAll("高温 ", "")+"\n\n低温:\n"+weather_data.data.yesterday.low.replaceAll("低温 ", ""));
                textview_today_u.setText(forc[0].ymd.substring(5,len)+"\n\n"+forc[0].type+"\n\n"+"风力:"+"\n"+forc[0].fl);
                textview_today_d.setText("高温:\n"+forc[0].high.replaceAll("高温 ", "")+"\n\n低温:\n"+forc[0].low.replaceAll("低温 ", ""));
                textview_day2_u.setText(forc[1].ymd.substring(5,len)+"\n\n"+forc[1].type+"\n\n"+"风力:"+"\n"+forc[1].fl);
                textview_day2_d.setText("高温:\n"+forc[1].high.replaceAll("高温 ", "")+"\n\n低温:\n"+forc[1].low.replaceAll("低温 ", ""));
                textview_day3_u.setText(forc[2].ymd.substring(5,len)+"\n\n"+forc[2].type+"\n\n"+"风力:"+"\n"+forc[2].fl);
                textview_day3_d.setText("高温:\n"+forc[1].high.replaceAll("高温 ", "")+"\n\n低温:\n"+forc[2].low.replaceAll("低温 ", ""));
                textview_day4_u.setText(forc[3].ymd.substring(5,len)+"\n\n"+forc[3].type+"\n\n"+"风力:"+"\n"+forc[3].fl);
                textview_day4_d.setText("高温:\n"+forc[1].high.replaceAll("高温 ", "")+"\n\n低温:\n"+forc[3].low.replaceAll("低温 ", ""));
                textview_day5_u.setText(forc[4].ymd.substring(5,len)+"\n\n"+forc[4].type+"\n\n"+"风力:"+"\n"+forc[4].fl);
                textview_day5_d.setText("高温:\n"+forc[1].high.replaceAll("高温 ", "")+"\n\n低温:\n"+forc[4].low.replaceAll("低温 ", ""));
            }
            else{
                WeatherActivity.this.setTitle("error status: "+weather_data.status);
                textview_notice.setText("Tips: error status: "+weather_data.status);
                textview_day2_u.setText(weather_data.status+"");
                textview_day2_d.setText(weather_data.status+"");
                textview_day3_u.setText(weather_data.status+"");
                textview_day3_d.setText(weather_data.status+"");
                textview_day4_u.setText(weather_data.status+"");
                textview_day4_d.setText(weather_data.status+"");
                textview_day5_u.setText(weather_data.status+"");
                textview_day5_d.setText(weather_data.status+"");
                textview_today_u.setText(weather_data.status+"");
                textview_today_d.setText(weather_data.status+"");
                textview_yesterday_u.setText(weather_data.status+"");
                textview_yesterday_d.setText(weather_data.status+"");
                textview_temperature.setText(weather_data.status+"");
                textview_city_name.setText(weather_data.status+"");
                textview_time.setText(weather_data.status+"");
                textview_date.setText(weather_data.status+"");
                textview_pm25.setText(weather_data.status+"");
                textview_pm10.setText(weather_data.status+"");
                textview_shidu.setText(weather_data.status+"");
                textview_quality.setText(weather_data.status+"");
                textview_temperature.setText(weather_data.status+"");
            }
        }
    }

    public void refresh(){
        if(get_d_time() > 1800){
            request_get_weather_json_from_url();
        }
        else if(get_d_time() < 0){
            request_get_weather_json_from_url();
        }
        else {
            request_get_weather_json();
            Toast.makeText(WeatherActivity.this,1800-get_d_time()+"秒后可联网刷新",Toast.LENGTH_SHORT).show();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                WeatherActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        parseJSONWithGSON();
                        set_textview_text();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        }).start();
    }

    private MyDatabaseHelper dbHelper;
    public void insert_city_to_database(String citycode,String cityname){
        dbHelper=new MyDatabaseHelper(this,"star_city_list.db",null,2);
        dbHelper.getWritableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("city_code",citycode);
        values.put("city_name",cityname);
        db.insert("citys", null,values);
        values.clear();
    }

    public long get_d_time(){
        long dt;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String time_now=simpleDateFormat.format(date);
            SharedPreferences pref = getSharedPreferences("weather_"+city_code,MODE_PRIVATE);
            String time_last_update=pref.getString("time_last_update","");
            if(time_last_update==""){
                return 1900;
            }
            Date d1;
            Date d2;
            d1 = df.parse(time_last_update);
            d2=df.parse(time_now);
            long diff = d2.getTime() - d1.getTime();//这样得到的差值是微秒级别
            dt=diff/1000;
        } catch (ParseException e) {
            e.printStackTrace();
            dt=1900;
        }
        return dt;
    }


    SwipeRefreshLayout swipeRefresh;
    long[] patter = { 0, 5, 2, 5 };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Vibrator vib = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
//        long[] patter = { 0, 5, 2, 5 };
//        vib.vibrate(patter, -1);
        setContentView(R.layout.activity_weather);
        Bundle bundle=this.getIntent().getExtras();
        city_code = bundle.getString("city_code");
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        textview_city_name=findViewById(R.id.city_name);
        textview_time=findViewById(R.id.update_time);
        textview_date=findViewById(R.id.update_date);
        textview_pm25=findViewById(R.id.pm25);
        textview_pm10=findViewById(R.id.pm10);
        textview_shidu=findViewById(R.id.shidu);
        textview_quality=findViewById(R.id.quality);
        textview_temperature=findViewById(R.id.temperature);
        textview_notice=findViewById(R.id.today_notice);
        textview_yesterday_d=findViewById(R.id.yesterday_d);
        textview_yesterday_u=findViewById(R.id.yesterday_u);
        textview_today_u=findViewById(R.id.today_u);
        textview_today_d=findViewById(R.id.today_d);
        textview_day2_u=findViewById(R.id.day2_u);
        textview_day2_d=findViewById(R.id.day2_d);
        textview_day3_u=findViewById(R.id.day3_u);
        textview_day3_d=findViewById(R.id.day3_d);
        textview_day4_u=findViewById(R.id.day4_u);
        textview_day4_d=findViewById(R.id.day4_d);
        textview_day5_u=findViewById(R.id.day5_u);
        textview_day5_d=findViewById(R.id.day5_d);
        button_refresh=findViewById(R.id.refresh);
        button_star=findViewById(R.id.star);

        refresh();

        swipeRefresh=findViewById(R.id.swipe_refresh);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        button_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.vibrate(patter, -1);
                refresh();
            }
        });

        button_refresh.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vib.vibrate(patter, -1);
                AlertDialog.Builder bb = new AlertDialog.Builder(WeatherActivity.this);
                bb.setPositiveButton("sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request_get_weather_json_from_url();
                    }
                });

                bb.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                bb.setMessage("立刻联网更新天气？");
                bb.show();
                return true;
            }
        });

        button_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vib.vibrate(patter, -1);
                if(responseData!=null){
                    if(weather_data.status==200){
                        if(city_code_already_exist(city_code)==false){
                            insert_city_to_database(city_code,weather_data.cityInfo.city);
                            Toast.makeText(WeatherActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(WeatherActivity.this,"已收藏",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean city_code_already_exist(String citycode){
        dbHelper=new MyDatabaseHelper(this,"star_city_list.db",null,2);
        dbHelper.getReadableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        int count=0;
        Cursor cursor = db.query("citys",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String citycode_=cursor.getString(cursor.getColumnIndex("city_code"));
                if(citycode.equals(citycode_)){
                    return true;
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        if(count==0){
            return false;
        }
        else {
            return true;
        }
    }
}

