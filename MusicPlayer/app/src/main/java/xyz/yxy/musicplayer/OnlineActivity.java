package xyz.yxy.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.icu.util.RangeValueIterator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class OnlineActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String NOTIFICATION_CHANNEL_ID_SERVICE = "xyz.yxy.musicplayer.OnlineActivity";

    public void initChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID_SERVICE, "Download Service", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            nm.createNotificationChannel(channel);
        }
    }

    public static String[] insert(String[] arr, String str)
    {
        int size = arr.length;
        String[] tmp = new String[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size] = str;
        return tmp;
    }

    public static song_info[] insert(song_info[] arr, song_info str)
    {
        int size = arr.length;
        song_info[] tmp = new song_info[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size]=str;
        return tmp;
    }

    public void clear_set(){
        song_name_list.clear();
        song_url_list.clear();
        song_author_list.clear();
    }

    List<String> song_name_list=new ArrayList<>();
    List<String> song_author_list=new ArrayList<>();
    List<String> song_url_list=new ArrayList<>();
    private void handleData(String content) {
        clear_set();
        song_info song=new song_info();
        Document doc = Jsoup.parse(content);
        Elements items = doc.select("div.play-item.gcol.gid-electronic");
        for (Element item : items) {
            song.author = item.select("div[class=playtxt]>span[class=ptxt-artist]").select("a").text();
            song.songname = item.select("div[class=playtxt]>span[class=ptxt-track]").text();
            song.url = item.select("span[class=playicn]").select("a[title=Download]").attr("href");
            if(song==null){

            }else {
                song_name_list.add(song.songname);
                song_author_list.add(song.author);
                song_url_list.add(song.url);
            }
        }
    }

    public void refresh_list(){
        List<String> res=new ArrayList<>();
        for(String e : song_name_list){
            File file=new File("/storage/emulated/0/Music/"+e+".mp3");
            if(file.exists()){
                res.add("(√) "+e);
            }else {
                res.add(e);
            }
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(OnlineActivity.this,android.R.layout.simple_list_item_1,res);
        listView_songlist.setAdapter(adapter);
    }


    public void findid(){
        listView_songlist=findViewById(R.id.listview_online);
        button_page_front=findViewById(R.id.button_front);
        button_page_next=findViewById(R.id.button_next);
        button_refresh=findViewById(R.id.button_refresh);
        editText_1=findViewById(R.id.editText1);
        editText_2=findViewById(R.id.editText2);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab_stop=findViewById(R.id.stop);
    }

    private void request_get_song_list(){//sendRequestWithOkHttp
        url="http://freemusicarchive.org/genre/"+music_type+"/?page="+music_page+"&sort=track_interest&d=1&per_page=25";
        SharedPreferences pref = getSharedPreferences(url.replace("/","_"),MODE_PRIVATE);
        String res_data=pref.getString("responseData","");
        Calendar cal=Calendar.getInstance();
        int d=cal.get(Calendar.DATE);
        int last_d=pref.getInt("date",0);
        Log.d("musicplayer",d+"_"+last_d);
        if(res_data!=""&&d==last_d){
            Log.d("res_data",res_data);
            responseData=res_data;
            button_refresh.setEnabled(true);
            button_page_front.setEnabled(true);
            button_page_next.setEnabled(true);
            Toast.makeText(OnlineActivity.this,"读取缓存",Toast.LENGTH_SHORT).show();
            if(responseData==""){

            }else {
                handleData(responseData);
                refresh_list();
            }
            editText_1.setText(music_type);
            editText_2.setText(music_page+"");
            OnlineActivity.this.setTitle("第"+music_page+"页");
        }
        else {
            if(res_data!=""){
                Log.d("res_data",res_data);
                responseData=res_data;
                button_refresh.setEnabled(true);
                button_page_front.setEnabled(true);
                button_page_next.setEnabled(true);
                Toast.makeText(OnlineActivity.this,"联网请求",Toast.LENGTH_SHORT).show();
                if(responseData==""){

                }else {
                    handleData(responseData);
                    refresh_list();
                }
                editText_1.setText(music_type);
                editText_2.setText(music_page+"");
                OnlineActivity.this.setTitle("第"+music_page+"页");
            }
            request_get_song_list_from_url();
        }
    }

    String responseData;
    private void request_get_song_list_from_url(){
        button_refresh.setEnabled(false);
        button_page_next.setEnabled(false);
        button_page_front.setEnabled(false);
        OnlineActivity.this.setTitle("加载中");
        url="http://freemusicarchive.org/genre/"+music_type+"/?page="+music_page+"&sort=track_interest&d=1&per_page=25";
        HttpUtil.sendOkHttpRequest(url,new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OnlineActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(OnlineActivity.this,"请检查网络连接",Toast.LENGTH_SHORT).show();
                                button_refresh.setEnabled(true);
                                button_page_next.setEnabled(true);
                                button_page_front.setEnabled(true);
                                OnlineActivity.this.setTitle("加载失败");
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseData=response.body().string();
                Calendar cal=Calendar.getInstance();
                int d=cal.get(Calendar.DATE);
                SharedPreferences.Editor editor=getSharedPreferences(url.replace("/","_"),MODE_PRIVATE).edit();
                editor.putString("responseData",responseData);
                editor.putInt("date",d);
                editor.apply();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        OnlineActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button_refresh.setEnabled(true);
                                button_page_front.setEnabled(true);
                                button_page_next.setEnabled(true);
                                Toast.makeText(OnlineActivity.this,"更新成功",Toast.LENGTH_SHORT).show();
                                if(responseData==""){

                                }else {
                                    handleData(responseData);
                                    refresh_list();
                                }
                                editText_1.setText(music_type);
                                editText_2.setText(music_page+"");
                                OnlineActivity.this.setTitle("第"+music_page+"页");
                            }
                        });
                    }
                }).start();
            }
        });
    }

    public void open_play_activity(){
        Intent intent=new Intent(OnlineActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("path","***");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private DownloadService.DownloadBinder downloadBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }
    };

    public void open_play_activity(String path){
        Intent intent=new Intent(OnlineActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("path",path);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    ListView listView_songlist;
    Button button_page_front;
    Button button_page_next;
    Button button_refresh;
    EditText editText_1;
    EditText editText_2;
    FloatingActionButton fab;
    FloatingActionButton fab_stop;
    String music_type;
    String url;
    int music_page;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);
        findid();
        initChannel();
        button_page_front.setOnClickListener(this);
        button_page_next.setOnClickListener(this);
        button_refresh.setOnClickListener(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.download_success");
        registerReceiver(download_success, intentFilter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                open_play_activity();
            }
        });

        fab_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_stop.hide();
                downloadBinder.cancelDownload();
            }
        });

        listView_songlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
                final String music_path="/storage/emulated/0/Music/"+song_name_list.get(position)+".mp3";
                File file=new File("/storage/emulated/0/Music/"+song_name_list.get(position)+".mp3");
                if(file.exists()){
                    fab_stop.hide();
                    open_play_activity(music_path);
                }else {
                    final File filels=new File("/storage/emulated/0/Music/ls");
                    if(filels.exists()){
                        Toast.makeText(OnlineActivity.this,"下载中",Toast.LENGTH_SHORT).show();
                    }else {
                        AlertDialog.Builder bb = new AlertDialog.Builder(OnlineActivity.this);
                        bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int in) {
                                fab_stop.show();
                                String url = song_url_list.get(position);
                                try {
                                    filels.createNewFile();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                downloadBinder.startDownload(url,song_name_list.get(position)+".mp3");
                                just_download_path=music_path;
                            }
                        });
                        bb.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        bb.setMessage("是否下载？");
                        bb.show();
                    }
                }
            }
        });

        listView_songlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final String music_path="/storage/emulated/0/Music/"+song_name_list.get(position)+".mp3";
                final File file=new File("/storage/emulated/0/Music/"+song_name_list.get(position)+".mp3");
                if(file.exists()){
                        AlertDialog.Builder bb = new AlertDialog.Builder(OnlineActivity.this);
                        bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int in) {
                                file.delete();
                                if(file.exists()){

                                }else {
                                    refresh_list();
                                    Toast.makeText(OnlineActivity.this,"已删除",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        bb.setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        bb.setMessage("是否删除？");
                        bb.show();

                }else {

                }
                return true;
            }
        });



        SharedPreferences pref = getSharedPreferences("last_text",MODE_PRIVATE);
        music_type=pref.getString("type","Classical");
        music_page=pref.getInt("page",1);
        if(music_type.equals("")){
            music_type="Classical";
        }
//        url="http://freemusicarchive.org/genre/"+music_type+"/?page="+music_page+"&sort=track_interest&d=1&per_page=25";
        editText_1.setText(music_type);
        editText_2.setText(music_page+"");
        OnlineActivity.this.setTitle("第"+music_page+"页");
        request_get_song_list();

        Intent intent = new Intent(this, DownloadService.class);
        startService(intent); // 启动服务
        bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
        if (ContextCompat.checkSelfPermission(OnlineActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(OnlineActivity.this, new String[]{ Manifest.permission. WRITE_EXTERNAL_STORAGE }, 1);
        }

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_front:
                if(music_page>1){
                    music_page--;
                    request_get_song_list();
                }else {
                    Toast.makeText(OnlineActivity.this,"已经是第一页",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button_next:
                music_page++;
                request_get_song_list();
                break;
            case R.id.button_refresh:
                music_type=editText_1.getText().toString();
                String page=editText_2.getText().toString();
                try{
                    music_page=Integer.parseInt(page);
                }catch (Exception e){
                    e.printStackTrace();
                }
                request_get_song_list_from_url();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy(){
        downloadBinder.cancelDownload();
        unregisterReceiver(download_success);
        super.onDestroy();
    }

    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);
    }

    protected void onStop(){
        SharedPreferences.Editor editor=getSharedPreferences("last_text",MODE_PRIVATE).edit();
        editor.putString("type",editText_1.getText().toString());
        String page=editText_2.getText().toString();
        try{
            int page_int=Integer.parseInt(page);
            editor.putInt("page",page_int);
        }catch (Exception e){
            e.printStackTrace();
        }
        editor.apply();
        super.onStop();
    }


    String just_download_path;
    private BroadcastReceiver download_success = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("action.download_success")){
                refresh_list();
                open_play_activity(just_download_path);
                fab_stop.hide();
            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            OnlineActivity.this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
