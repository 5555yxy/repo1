package xyz.yxy.weather;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ListView city_list;
    public String responseData;
    int select=1;



    private void request_get_city_list_json(){//sendRequestWithOkHttp
        SharedPreferences pref = getSharedPreferences("citylist",MODE_PRIVATE);
        String res_data=pref.getString("citylist","");
        if(res_data!=""){
            Log.d("res_data",res_data);
            responseData=res_data;
            parseJSONWithGSON();
        }
        else {
            Toast.makeText(MainActivity.this,"联网更新列表",Toast.LENGTH_SHORT).show();
            request_get_city_list_json_from_url();
        }
    }

    private void request_get_city_list_json_from_url(){
        String url="http://cdn.sojson.com/_city.json?attname=";
        HttpUtil.sendOkHttpRequest(url,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"请检查网络连接",Toast.LENGTH_SHORT).show();
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
                SharedPreferences.Editor editor=getSharedPreferences("citylist",MODE_PRIVATE).edit();
                editor.putString("citylist",responseData);
                editor.putString("time_last_update",time_now);
                editor.apply();
                if(select==2){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    parseJSONWithGSON();
                                    Gson gson=new Gson();
                                    weatherList=gson.fromJson(responseData,new TypeToken<List<weather>>(){}.getType());
                                    String[] data=showList_city(weatherList);
                                    ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                                    city_list.setAdapter(adapter);
                                }
                            });
                        }
                    }).start();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }
        });
    }

    public List<weather> weatherList;
    public void parseJSONWithGSON(){
        Gson gson=new Gson();
        weatherList=gson.fromJson(responseData,new TypeToken<List<weather>>(){}.getType());
    }

    public static String[] insert(String[] arr, String str)
    {
        int size = arr.length;
        String[] tmp = new String[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size] = str;
        return tmp;
    }

    public static int[] insert_int(int[] arr, int str)
    {
        int size = arr.length;
        int[] tmp = new int[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size] = str;
        return tmp;
    }

    public String[] showList_city(List<weather> weatherList){
        String[] datas={};
        String k;
        for(weather w:weatherList){
            if(w.getPid()==0){
                k=w.getCity_name();
                datas=insert(datas,k);
            }
        }
        return datas;
    }

    public void open_new_activity(int id){
        if(weatherList.get(id).getCity_code()!=""){
            Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",weatherList.get(id).getCity_code());
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else if(weatherList.get(id).getPid()==0){
            Intent intent=new Intent(MainActivity.this,List2Activity.class);
            Bundle bundle=new Bundle();
            bundle.putInt("id",weatherList.get(id).getid());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public boolean is_county(int pid_){
        int[] datas_int={};
        int i= 0;
        for(weather w:weatherList){
            if(w.getPid()==pid_){
                datas_int=insert_int(datas_int,w.getPid());
                i++;
            }
        }
        if(i<=1){
            return false;
        }else if(i>1){
            return true;
        }
        return false;
    }

    public void open_new_activity(String city_code){
        if(city_code!=""){
            Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",city_code);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    public void open_new_activity_with_pid(int pid_){
        Intent intent=new Intent(MainActivity.this,List3Activity.class);
        Bundle bundle=new Bundle();
        bundle.putInt("id",pid_);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public String[] show_star_city_list(){
        String[] datas={};
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.query("citys",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String citycode=cursor.getString(cursor.getColumnIndex("city_code"));
                String cityname = cursor.getString(cursor.getColumnIndex("city_name"));
                datas=insert(datas,cityname);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return datas;
    }

    public String from_position_get_citycode(int l){
        String[] data={};
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("citys",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String citycode = cursor.getString(cursor.getColumnIndex("city_code"));
                data=insert(data,citycode);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return data[l];
    }

    public void open_weather_activity(String city_code){
        if(city_code!=""){
            Intent intent=new Intent(MainActivity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",city_code);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else {
            Toast.makeText(MainActivity.this,"City_Code NULL",Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollMyListViewToPosition(final ListView myListView,final long p) {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                myListView.setSelection((int)p-3);
            }
        });
    }

    public long get_d_time(){
        long dt;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//yyyy-MM-dd HH:mm:ss
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(System.currentTimeMillis());
            String time_now=simpleDateFormat.format(date);
            SharedPreferences pref = getSharedPreferences("citylist",MODE_PRIVATE);
            String time_last_update=pref.getString("time_last_update","");
            Log.d("time_last_update",time_last_update);
            Date d1;
            Date d2;
            d1 = df.parse(time_last_update);
            d2=df.parse(time_now);
            long diff = d2.getTime() - d1.getTime();//这样得到的差值是微秒级别
//            long days = diff / (1000 * 60 * 60 * 24);
//            long hours = (diff-days*(1000 * 60 * 60 * 24))/(1000* 60 * 60);
//            long minutes = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60))/(1000* 60);
//            long second = (diff-days*(1000 * 60 * 60 * 24)-hours*(1000* 60 * 60)-minutes*(1000*60))/1000;
//            Toast.makeText(MainActivity.this,""+days+"天"+hours+"小时"+minutes+"分",Toast.LENGTH_SHORT).show();
//            dt=days*24*60*60+hours*60*60+minutes*60+second;
            dt=diff/1000;
        } catch (ParseException e) {
            e.printStackTrace();
            dt=1900;
        }
        return dt;
    }

    @Override
    public void onBackPressed() {
//         super.onBackPressed();//注释掉这行,back键不退出activity
        AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
        bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int in) {
                MainActivity.super.onBackPressed();
            }
        });
        bb.setNegativeButton("否", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        bb.setMessage("退出？");
//        bb.setTitle("提示");
        bb.show();
    }

    public void onResume() {
        if(select==1){
            String[] data1=show_star_city_list();
            ArrayAdapter<String> adapter1=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data1);
            city_list.setAdapter(adapter1);
        }
        super.onResume();
    }

    private MyDatabaseHelper dbHelper;
    FloatingActionButton fab;
    FloatingActionButton fab_search;
    long[] patter = { 0, 5, 2, 5 };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Vibrator vib = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        //long[] patter = { 0, 5, 2, 5 };
        setContentView(R.layout.activity_main);
        dbHelper=new MyDatabaseHelper(this,"star_city_list.db",null,2);
        dbHelper.getWritableDatabase();
        city_list=findViewById(R.id.listview);
        request_get_city_list_json();
        String[] data1=show_star_city_list();
        ArrayAdapter<String> adapter1=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data1);
        city_list.setAdapter(adapter1);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_search=findViewById(R.id.fab_search);
        fab_search.animate().translationY(0);
        fab.animate().translationY(50);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vib.vibrate(patter, -1);
                switch (select){
                    case 1:
                        break;
                    case 2:
                        if(get_d_time()>1800){
                            request_get_city_list_json_from_url();
                        }
                        else if(get_d_time()<0){
                            request_get_city_list_json_from_url();
                        }
                        else {
                            request_get_city_list_json();
                            Toast.makeText(MainActivity.this,1800-get_d_time()+"秒后可联网刷新",Toast.LENGTH_SHORT).show();
                        }
                        if(responseData==""){
                            Toast.makeText(MainActivity.this,"请检查网络连接",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case 3:
                        break;
                }
            }
        });

        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
                bb.setPositiveButton("sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request_get_city_list_json_from_url();
                    }
                });

                bb.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                bb.setMessage("联网更新列表？");
                bb.show();
                return true;
            }
        });

        fab_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vib.vibrate(patter, -1);
                switch (select){
                    case 1:

                    case 2:
                        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        break;
                }
            }
        });
        city_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                switch (select){
                    case 1:
                        open_weather_activity(from_position_get_citycode(position));
                        break;
                    case 2:
                        if(is_county(position)==true&&weatherList.get(position).getCity_code()!=""){
                            final CharSequence[] items = { "查看天气信息", "查看下属市区" };
                            android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(MainActivity.this).setItems(items,
                                new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int item) {
                                    if(item==1){
                                        open_new_activity_with_pid(weatherList.get(position).getid());
                                    }else{
                                        open_new_activity(weatherList.get(position).getCity_code());
                                    }
                                }
                            }).create();
                            dlg.show();
                        }
                        else if(is_county(position)==false||weatherList.get(position).getCity_code()==""){
                            open_new_activity(position);
                        }
                        else if(is_county(position)==false&&weatherList.get(position).getCity_code()!=""){
                            open_new_activity(weatherList.get(position).getCity_code());
                        }
                        break;
                    case 3:
                        break;
                }
            }
        });

        city_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                switch (select){
                    case 1:
                        AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
                        bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int in) {
                                SQLiteDatabase db=dbHelper.getWritableDatabase();
                                String a=from_position_get_citycode(i);
                                db.delete("citys","city_code=?",new String[] {a});
                                String[] data=show_star_city_list();
                                ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                                ListView listView=(ListView)findViewById(R.id.listview);
                                scrollMyListViewToPosition(listView,i);
                                listView.setAdapter(adapter);
                            }
                        });
                        bb.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        bb.setMessage("取消收藏？");
                        bb.show();
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                }
                return true;
            }
        });
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("ResourceType")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        vib.vibrate(patter, -1);
                        select=1;
                        fab.hide();
                        fab.animate().translationY(150);
//                        fab_search.hide();
                        fab_search.animate().translationY(0);
                        fab.animate().translationY(50);
                        String[] data1=show_star_city_list();
                        ArrayAdapter<String> adapter1=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data1);
                        city_list.setAdapter(adapter1);
                        return true;

                    case R.id.navigation_dashboard:
                        vib.vibrate(patter, -1);
                        select=2;
//                        fab_search.show();
                        fab_search.animate().translationY(-185);
                        fab.animate().translationY(0);
                        fab.show();
                        if(responseData!=null){
                            Gson gson=new Gson();
                            weatherList=gson.fromJson(responseData,new TypeToken<List<weather>>(){}.getType());
                            String[] data=showList_city(weatherList);
                            ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                            city_list.setAdapter(adapter);
                        }
                        return true;
                }
                return false;
            }
        });
    }
}
