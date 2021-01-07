package xyz.yxy.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import static xyz.yxy.weather.MainActivity.insert;
import static xyz.yxy.weather.MainActivity.insert_int;

public class List2Activity extends AppCompatActivity {
    ListView city_list2;
    public List<weather> weatherList;

    public String[] showList_city(List<weather> weatherList){
        String[] datas={};
        String k;
        for(weather w:weatherList){
            if(w.getPid()==id){
                k=w.getCity_name();
                datas=insert(datas,k);
            }
        }
        return datas;
    }

    public String get_citycode_from_position(int pos){
        String[] datas={};
        int[] datas_int={};
        for(weather w:weatherList){
            if(w.getPid()==id){
                datas=insert_citycode(datas,w);
                datas_int=insert_int(datas_int,w.getPid());
            }
        }
        return datas[pos];
    }

    public int get_pid_from_position(int pos){      //下一个activity的id
        int[] datas_int={};
        int i = 0;
        for(weather w:weatherList){
            if(w.getPid()==id){
                datas_int=insert_int(datas_int,w.getid());
            }
        }
        return datas_int[pos];
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
        if(i<1){
            return false;
        }else if(i>=1){
            return true;
        }
        return false;
    }

    public static String[] insert_citycode(String[] arr, weather str)
    {
        int size = arr.length;
        String[] tmp = new String[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size] = str.getCity_code();
        return tmp;
    }

    public void open_new_activity(String city_code){
        if(city_code!=""){
            Intent intent=new Intent(List2Activity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",city_code);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else if(city_code==""){
            Toast.makeText(List2Activity.this,"City_Code 无效",Toast.LENGTH_SHORT).show();
        }
    }

    public void open_new_activity_with_pid(int pid_){
        Intent intent=new Intent(List2Activity.this,List3Activity.class);
        Bundle bundle=new Bundle();
        bundle.putInt("id",pid_);
        intent.putExtras(bundle);
        startActivity(intent);
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

    int id;
    long[] patter = { 0, 5, 2, 5 };
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Vibrator vib = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        //long[] patter = { 0, 5, 2, 5 };
//        vib.vibrate(patter, -1);
        setContentView(R.layout.activity_list2);
        city_list2=findViewById(R.id.listview2);
        Bundle bundle=this.getIntent().getExtras();
        id = bundle.getInt("id");
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        SharedPreferences pref = getSharedPreferences("citylist",MODE_PRIVATE);
        String responseData=pref.getString("citylist","");
        Gson gson=new Gson();
        weatherList=gson.fromJson(responseData,new TypeToken<List<weather>>(){}.getType());
        String[] data=showList_city(weatherList);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(List2Activity.this,android.R.layout.simple_list_item_1,data);
        city_list2.setAdapter(adapter);
        city_list2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if(is_county(get_pid_from_position(position))==true&&get_citycode_from_position(position)!=""){
                    final CharSequence[] items = { "查看天气信息", "查看下属区县" };
                    android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(List2Activity.this).setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int item) {
                                    if(item==1){
                                        open_new_activity_with_pid(get_pid_from_position(position));
                                    }else{
                                        open_new_activity(get_citycode_from_position(position));
                                    }
                                }
                            }).create();
                    dlg.show();
                }
                else if(is_county(get_pid_from_position(position))==false||get_citycode_from_position(position)==""){
                    open_new_activity(get_citycode_from_position(position));
                }
            }
        });
    }
}
