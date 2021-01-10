package xyz.yxy.weather;

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

public class List3Activity extends AppCompatActivity {
    ListView city_list3;
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
        for(weather w:weatherList){
            if(w.getPid()==id){
                datas=insert_citycode(datas,w);
            }
        }

        return datas[pos];
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
            Intent intent=new Intent(List3Activity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",city_code);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        else {
            Toast.makeText(List3Activity.this,"City_Code 为空",Toast.LENGTH_SHORT).show();
        }
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
//        long[] patter = { 0, 5, 2, 5 };
//        vib.vibrate(patter, -1);
        setContentView(R.layout.activity_list3);
        city_list3=findViewById(R.id.listview3);
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
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(List3Activity.this,android.R.layout.simple_list_item_1,data);
        city_list3.setAdapter(adapter);
        city_list3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                open_new_activity(get_citycode_from_position(position));
            }
        });
    }
}
