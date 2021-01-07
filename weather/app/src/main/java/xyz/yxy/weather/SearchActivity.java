package xyz.yxy.weather;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import scut.carson_ho.searchview.ICallBack;
import scut.carson_ho.searchview.SearchView;
import scut.carson_ho.searchview.bCallBack;

import static xyz.yxy.weather.MainActivity.insert;
import static xyz.yxy.weather.MainActivity.insert_int;

public class SearchActivity extends AppCompatActivity {

    public List<weather> weatherList;
    public void parseJSONWithGSON(){
        if(responseData!=""){
            Gson gson=new Gson();
            weatherList=gson.fromJson(responseData,new TypeToken<List<weather>>(){}.getType());
        }
    }

    public void open_new_activity_with_pid(int pid_){
        Intent intent=new Intent(SearchActivity.this,List3Activity.class);
        Bundle bundle=new Bundle();
        bundle.putInt("id",pid_);
        intent.putExtras(bundle);
        startActivity(intent);
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

    String responseData;
    private SearchView searchView;
    public void ref(weather w){
        if(w.getCity_code()!=""){
            Intent intent=new Intent(SearchActivity.this,WeatherActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("city_code",w.getCity_code());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        SharedPreferences pref = getSharedPreferences("citylist",MODE_PRIVATE);
        responseData=pref.getString("citylist","");
        searchView = (SearchView) findViewById(R.id.search_view);

        // 4. 设置点击搜索按键后的操作（通过回调接口）
        // 参数 = 搜索框输入的内容

        searchView.setOnClickSearch(new ICallBack() {
            @Override
            public void SearchAciton(String string) {
                parseJSONWithGSON();
                if(weatherList!=null){
                    for(final weather w:weatherList){
                        if(string.equals(w.getCity_name()+"市")){
                            if(is_county(w.getid())==true&&w.getCity_code()!=""){
                                final CharSequence[] items = { "查看天气信息", "查看下属区县" };
                                android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(SearchActivity.this).setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int item) {
                                                if(item==1){
                                                    open_new_activity_with_pid(w.getid());
                                                }else{
                                                    ref(w);
                                                }
                                            }
                                        }).create();
                                dlg.show();
                                break;
                            }
                            else if(is_county(w.getid())==false||w.getCity_code()==""){
                                ref(w);
                                break;
                            }
                            ref(w);break;
                        }
                        if((string+"县").equals(w.getCity_name())){
                            ref(w);break;
                        }
                        if((string+"区").equals(w.getCity_name())){
                            ref(w);break;
                        }
                        if(w.getCity_code().equals(string) || w.getCity_name().equals(string)){
                            if(w.getCity_code().equals("")&&w.getPid()==0){
                                open_new_activity_with_pid(w.getid());
                                break;
                            }
                            else {
                                if(is_county(w.getid())==true&&w.getCity_code()!=""){
                                    final CharSequence[] items = { "查看天气信息", "查看下属区县" };
                                    android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(SearchActivity.this).setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int item) {
                                                if(item==1){
                                                    open_new_activity_with_pid(w.getid());
                                                }else{
                                                    ref(w);
                                                }
                                            }
                                        }).create();
                                    dlg.show();
                                    break;
                                }
                                else if(is_county(w.getid())==false||w.getCity_code()==""){
                                    ref(w);
                                    break;
                                }
                                ref(w);break;
                            }
                        }
                        if(w.getCity_name().equals(string.substring(0,string.length()-1))){
                            if(w.getCity_code().equals("")&&w.getPid()==0){
                                open_new_activity_with_pid(w.getid());
                                break;
                            }
                            else {
                                if(is_county(w.getid())==true&&w.getCity_code()!=""){
                                    final CharSequence[] items = { "查看天气信息", "查看下属区县" };
                                    android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(SearchActivity.this).setItems(items,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,int item) {
                                                if(item==1){
                                                    open_new_activity_with_pid(w.getid());
                                                }else{
                                                    ref(w);
                                                }
                                            }
                                        }).create();
                                    dlg.show();
                                    break;
                                }
                                else if(is_county(w.getid())==false||w.getCity_code()==""){
                                    ref(w);
                                    break;
                                }
                                ref(w);break;
                            }
                        }
                    }
                }
            }
        });

        // 5. 设置点击返回按键后的操作（通过回调接口）
        searchView.setOnClickBack(new bCallBack() {
            @Override
            public void BackAciton() {
                finish();
            }
        });
    }
}
