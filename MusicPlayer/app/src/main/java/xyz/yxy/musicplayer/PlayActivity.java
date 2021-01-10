package xyz.yxy.musicplayer;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.nio.channels.Channel;
import java.text.SimpleDateFormat;

import static xyz.yxy.musicplayer.MainActivity.insert;
import static xyz.yxy.musicplayer.MainActivity.is_music_file;

public class PlayActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String NOTIFICATION_CHANNEL_ID_SERVICE = "xyz.yxy.musicplayer.PlayActivity";

    public void initChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel=new NotificationChannel(NOTIFICATION_CHANNEL_ID_SERVICE, "Play Service", NotificationManager.IMPORTANCE_LOW);
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});
            nm.createNotificationChannel(channel);
        }
    }

    private PlayService playService;
    AudioManager am;

    public String music_Path;

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    //  回调onServiceConnected 函数，通过IBinder 获取 Service对象，实现Activity与 Service的绑定
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playService = ((PlayService.MyBinder) (service)).getService();
            if(is_star_music!=null){
                playService.binder.start_play(show_star_song_list()[position_in_database]);

            }else {
                playService.binder.start_play(music_Path);

            }
            if(playService.binder.isPlaying()){

            }
            else {
                if(music_Path.equals("***")){

                }else {
                    handler.post(runnable);
                }
                if(no_path){

                }else {
                    playService.binder.playOrPause();
                }
            }
            play_button();

            try {
                set_music_info();
            }catch (Exception e){
                e.printStackTrace();
            }

            playService.binder.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if(is_star_music_){
                        button_next.show();
                        button_last.show();
                        if(isloop){
                            Toast.makeText(PlayActivity.this,"播放",Toast.LENGTH_SHORT).show();
                            playService.binder.playOrPause();
                            play_button();
                            already_star();
                        } else {
                            if(show_star_song_list().length>0){
                                if(position_in_database<show_star_song_list().length-1){
                                    if(playService.binder.mediaPlayer.getDuration()<5000){
                                        playService.binder.reset();
                                        Toast.makeText(PlayActivity.this,"文件损坏！请重新下载!",Toast.LENGTH_SHORT).show();
                                    }else {
                                        position_in_database++;
                                        playService.binder.reset();
                                        music_Path=show_star_song_list()[position_in_database];
                                        playService.binder.start_play_again(music_Path);
                                        playService.binder.playOrPause();
                                        set_music_info();
                                        play_button();
                                        already_star();
                                        Toast.makeText(PlayActivity.this,"切换下一首",Toast.LENGTH_SHORT).show();
                                    }
                                }
                                else {
                                    if(playService.binder.mediaPlayer.getDuration()<5000){
                                        playService.binder.reset();
                                        Toast.makeText(PlayActivity.this,"文件损坏！请重新下载!",Toast.LENGTH_SHORT).show();
                                    }else {
                                        position_in_database=0;
                                        playService.binder.reset();
                                        music_Path=show_star_song_list()[position_in_database];
                                        playService.binder.start_play_again(music_Path);
                                        playService.binder.playOrPause();
                                        set_music_info();
                                        play_button();
                                        already_star();
                                        Toast.makeText(PlayActivity.this,"回到第一首",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }else {
                        button_next.hide();
                        button_last.hide();
                        if(!isloop){
                            playService.cancel_notification();
                            if(playService.binder.mediaPlayer.getDuration()<5000){
                                playService.binder.reset();
                                Toast.makeText(PlayActivity.this,"文件损坏！请重新下载!",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(PlayActivity.this,"停止",Toast.LENGTH_SHORT).show();
                                play_button();
                                already_star();
                            }
                        }
                        else {
                            if(playService.binder.mediaPlayer.getDuration()<5000){
                                playService.binder.reset();
                                Toast.makeText(PlayActivity.this,"文件损坏！请重新下载!",Toast.LENGTH_SHORT).show();
                            }else {
                                playService.binder.playOrPause();
                                Toast.makeText(PlayActivity.this,"播放",Toast.LENGTH_SHORT).show();
                                play_button();
                                already_star();
                            }
                        }
                    }
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService = null;
        }
    };

    Drawable k;
    public void set_music_info(){
        if(music_Path.equals("***")){

        }else {
            MediaMetadataRetriever mediaMetadataRetriever = (MediaMetadataRetriever) new MediaMetadataRetriever();
            File music=new File(music_Path);
            Uri uri = (Uri) Uri.fromFile(music);
            if(uri!=null){
                mediaMetadataRetriever.setDataSource(PlayActivity.this, uri);
                String title = (String) mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                String artist=mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                PlayActivity.this.setTitle(title);
                byte[] art =mediaMetadataRetriever.getEmbeddedPicture();

                if (art != null) {
                    Bitmap bMap = BitmapFactory.decodeByteArray(art, 0, art.length);
                    imageView_song.setImageBitmap(bMap);
                }else {
                    imageView_song.setImageDrawable(k);
                }
                textView_artist.setText("\n"+artist);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    public void onResume(){
//        flag=false;
        initPlayWork();
        Log.d("musicplayer","onResume");
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
//            moveTaskToBack(true);
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed(){
        if(flag){
            moveTaskToBack(true);
        }
//        super.onBackPressed();
        else {
            Intent intent=new Intent(PlayActivity.this,MainActivity.class);
            Bundle bundle=new Bundle();
            bundle.putBoolean("open",false);
            intent.putExtras(bundle);
            startActivity(intent);
        }
        Log.d("musicplayer","onbackpressed");
    }

    @Override
    protected void onDestroy() {
        playService.binder.stop();
        playService.cancel_notification();
        handler.removeCallbacks(runnable);
        unbindService(serviceConnection);
        Log.d("musicplayer","destroy");
        SharedPreferences.Editor editor=getSharedPreferences("last_music_path",MODE_PRIVATE).edit();
        editor.putString("music_Path",music_Path);
        editor.putBoolean("is_star_music_",is_star_music_);
        editor.putInt("position_in_database",position_in_database);
        editor.apply();
        super.onDestroy();
    }

    boolean flag=false;
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle=intent.getExtras();
        flag=true;
        if(bundle!=null){
            if(bundle.getString("path").equals("***")){

            }
            else {
                is_star_music=bundle.getString("is_star_music");
                position_in_database=bundle.getInt("position_in_database");
                if(music_Path!=bundle.getString("path")){
                    handler.post(runnable);
                    if(playService!=null){
                        playService.binder.stop();
                    }
//                    unbindService(serviceConnection);
//                    music_Path = bundle.getString("path");
                    if(is_star_music!=null){
                        is_star_music_=true;
                        button_next.show();
                        button_last.show();
                        music_Path=show_star_song_list()[position_in_database];
                        Log.d("play",music_Path);
                    }else {
                        is_star_music_=false;
                        button_next.hide();
                        button_last.hide();
                        music_Path = bundle.getString("path");
                        Log.d("play",music_Path);
                    }
                    playService.binder.start_play_again(music_Path);
//                    Intent intent2 = new Intent(PlayActivity.this, PlayService.class);
//                    bindService(intent2, serviceConnection, this.BIND_AUTO_CREATE);
                    if(playService.binder.isPlaying()){

                    }
                    else {
                        playService.binder.playOrPause();
                    }
                    already_star();
                    play_button();
                    try {
                        set_music_info();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }else {
            flag=false;
        }
        Log.d("musicplayer","onnewintent");
    }

    //  通过 Handler 更新 UI 上的组件状态
    private SimpleDateFormat time = new SimpleDateFormat("mm:ss");
    public Handler handler = new Handler();
    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            textView_ctime.setText(time.format(playService.binder.mediaPlayer.getCurrentPosition()));
            seekBar_progress.setProgress(playService.binder.mediaPlayer.getCurrentPosition());
            seekBar_progress.setMax(playService.binder.mediaPlayer.getDuration());
            textView_time.setText(time.format(playService.binder.mediaPlayer.getDuration()));
            handler.postDelayed(runnable, 200);
        }
    };

    public void onStop(){
        Log.d("musicplayer","onStop");
        flag=false;
        super.onStop();
    }

    public void play_button(){
        if(playService.binder.isPlaying()){
            button_pause.show();
            button_play.hide();
        }
        else {
            button_play.show();
            button_pause.hide();
        }
    }

    private int maxVolume = 60; // 最大音量值
    private int curVolume = 10; // 当前音量值
    private int stepVolume = 0; // 每次调整的音量幅度

    private MediaPlayer mediaPlayer = null;// 播放器
    private AudioManager audioMgr = null; // Audio管理器，用了控制音量
    private AssetManager assetMgr = null; // 资源管理器
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pause:
            case R.id.play:
                if(playService!=null){
//                    Toast.makeText(PlayActivity.this,music_Path,Toast.LENGTH_SHORT).show();
                    playService.binder.playOrPause();
                    play_button();
                }

                break;
            case R.id.stop:
                if(playService==null){
                    play_button();
                }
                else {
                    playService.binder.stop();
                    play_button();
                }
                break;
            case R.id.next:
                if(show_star_song_list().length>0){
                    if(position_in_database<show_star_song_list().length-1){
                        position_in_database++;
                        if(playService!=null){
                            playService.binder.stop();
                        }
                        music_Path=show_star_song_list()[position_in_database];
                        Log.d("play",music_Path);
                        playService.binder.start_play_again(music_Path);
                        if(playService.binder.isPlaying()){

                        }
                        else {
                            playService.binder.playOrPause();
                        }
                        set_music_info();
                        already_star();
                    }else {
                        position_in_database=0;
                        if(playService!=null){
                            playService.binder.stop();
                        }
                        music_Path=show_star_song_list()[position_in_database];
                        Log.d("play",music_Path);
                        playService.binder.start_play_again(music_Path);
                        if(playService.binder.isPlaying()){

                        }
                        else {
                            playService.binder.playOrPause();
                        }
                        set_music_info();
                        already_star();
                        Toast.makeText(PlayActivity.this,"回到第一首",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PlayActivity.this,"无收藏歌曲",Toast.LENGTH_SHORT).show();
                }
                play_button();
                break;
            case R.id.last:
                if(show_star_song_list().length>0){
                    if(position_in_database>0){
                        position_in_database--;
                        if(playService!=null){
                            playService.binder.stop();
                        }
                        music_Path=show_star_song_list()[position_in_database];
                        Log.d("play",music_Path);
                        playService.binder.start_play_again(music_Path);
                        if(playService.binder.isPlaying()){

                        }
                        else {
                            playService.binder.playOrPause();
                        }
                        already_star();
                        set_music_info();
                    }else {
                        Toast.makeText(PlayActivity.this,"已经是第一首",Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PlayActivity.this,"无收藏歌曲",Toast.LENGTH_SHORT).show();
                }
                play_button();
                break;
            case R.id.v_up://按下增大音量按钮
                curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                curVolume += stepVolume;
                if (curVolume >= maxVolume) {
                    curVolume = maxVolume;
                }
//                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                adjustVolume();
                break;
            case R.id.v_down://按下减小音量按钮
                curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
                curVolume -= stepVolume;
                if (curVolume <= 0) {
                    curVolume = 0;
                }
//                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                adjustVolume();
                break;
            default:
                break;
        }
    }

    private void adjustVolume() {
        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        progressBar_volume.setProgress(curVolume);
//        audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
    }


    public void insert_song_path_to_database(String path){
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getWritableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("song_path",path);
        db.insert("songs", null,values);
        Toast.makeText(PlayActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
        values.clear();
    }

    public boolean song_already_exist(String path){
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getReadableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        int count=0;
        Cursor cursor = db.query("songs",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String citycode_=cursor.getString(cursor.getColumnIndex("song_path"));
                if(path.equals(citycode_)){
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

    public String[] show_star_song_list(){
        String[] datas={};
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.query("songs",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String path = cursor.getString(cursor.getColumnIndex("song_path"));
                datas=insert(datas,path);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return datas;
    }

    void already_star(){
        if(is_music_file(music_Path)){
            if(song_already_exist(music_Path)==true){
                aSwitch_star.setChecked(true);
            }else {
                aSwitch_star.setChecked(false);
            }
        }
        if(isloop){
            aSwitch_isloop.setChecked(true);
        }else {
            aSwitch_isloop.setChecked(false);
        }
    }

    private void initPlayWork() {
        audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 获取最大音乐音量
        maxVolume = audioMgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 获取初始化音量
        curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        // 每次调整的音量大概为最大音量的1/6
//        stepVolume = maxVolume / 15;
        stepVolume = 1;
        progressBar_volume.setMax(maxVolume);
        progressBar_volume.setProgress(curVolume);
    }

    SeekBar seekBar_progress;
    ProgressBar progressBar_volume;
    FloatingActionButton button_next;
    FloatingActionButton button_last;
    FloatingActionButton button_stop;
    FloatingActionButton button_play;
    FloatingActionButton button_pause;
    FloatingActionButton button_up;
    FloatingActionButton button_down;
    TextView textView_ctime;
    TextView textView_time;
    TextView textView_artist;
    Switch aSwitch_star;
    Switch aSwitch_isloop;
    ImageView imageView_song;
    String is_star_music=null;
    int 唉=2;
    boolean is_star_music_=false;
    boolean isloop=false;
    boolean no_path=false;
    int position_in_database;
    private MyDatabaseHelper dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getWritableDatabase();
        am = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        button_play=findViewById(R.id.play);
        button_stop=findViewById(R.id.stop);
        seekBar_progress=findViewById(R.id.seekBar_progress);
        button_next=findViewById(R.id.next);
        button_last=findViewById(R.id.last);
        button_pause=findViewById(R.id.pause);
        button_up=findViewById(R.id.v_up);
        button_down=findViewById(R.id.v_down);
        aSwitch_star=findViewById(R.id.switch_star);
        aSwitch_isloop=findViewById(R.id.switch_isloop);
        textView_ctime=findViewById(R.id.text_crrent_time);
        textView_time=findViewById(R.id.text_time);
        textView_artist=findViewById(R.id.text_artist);
        imageView_song=findViewById(R.id.imageView);
        progressBar_volume=findViewById(R.id.volume_bar);
        k =imageView_song.getDrawable();

        initPlayWork();

        Bundle bundle=this.getIntent().getExtras();

        if(bundle!=null){
            is_star_music=bundle.getString("is_star_music",null);
            position_in_database=bundle.getInt("position_in_database");

            if(is_star_music!=null){
                button_next.show();
                button_last.show();
                is_star_music_=true;
                music_Path=show_star_song_list()[position_in_database];
            }else {
                is_star_music_=false;
                button_next.hide();
                button_last.hide();
                music_Path = bundle.getString("path");
                if(music_Path.equals("***")){
                    no_path=true;
                    SharedPreferences pref = getSharedPreferences("last_music_path",MODE_PRIVATE);
                    music_Path=pref.getString("music_Path","");
                    is_star_music_=pref.getBoolean("is_star_music_",false);
                    if(is_star_music_){
                        position_in_database=pref.getInt("position_in_database",0);
                        button_next.show();
                        button_last.show();
                    }
                }
            }
            Intent intent = new Intent(PlayActivity.this, PlayService.class);
            intent.putExtras(bundle);
            bindService(intent, serviceConnection, this.BIND_AUTO_CREATE);
            startService(intent);
        }

        already_star();
        initChannel();

        int permission = ActivityCompat.checkSelfPermission(PlayActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(PlayActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }else{

        }

        button_stop.setOnClickListener(this);
        button_play.setOnClickListener(this);
        button_pause.setOnClickListener(this);
        button_next.setOnClickListener(this);
        button_last.setOnClickListener(this);
        button_up.setOnClickListener(this);
        button_down.setOnClickListener(this);

        imageView_song.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(唉%2==0){
                    button_down.hide();
                    button_up.hide();
                } else {
                    button_up.show();
                    button_down.show();
                }
                唉++;
            }
        });

        button_down.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                curVolume=0;
                audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, AudioManager.FLAG_PLAY_SOUND);
                progressBar_volume.setProgress(curVolume);
                return true;
            }
        });

        aSwitch_star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_music_file(music_Path)){
                    if(song_already_exist(music_Path)==true){
                        SQLiteDatabase db=dbHelper.getWritableDatabase();
//                                String a=from_position_get_song(position_in_database);
                        db.delete("songs","song_path=?",new String[] {music_Path});
                        Toast.makeText(PlayActivity.this,"取消收藏",Toast.LENGTH_SHORT).show();
                        aSwitch_star.setChecked(false);
                    }else {
                        insert_song_path_to_database(music_Path);
                        aSwitch_star.setChecked(true);
                    }
                }
            }
        });

        aSwitch_isloop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isloop==true){
                    Toast.makeText(PlayActivity.this,"取消循环(顺序播放/播放一次)",Toast.LENGTH_SHORT).show();
                    isloop=false;
                    aSwitch_isloop.setChecked(false);
                }else {
                    Toast.makeText(PlayActivity.this,"设置循环(单曲播放)",Toast.LENGTH_SHORT).show();
                    isloop=true;
                    aSwitch_isloop.setChecked(true);
                }
            }
        });
        seekBar_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser == true) {
                    playService.binder.mediaPlayer.seekTo(progress);
                    play_button();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(playService.binder.isPlaying()){
                    playService.binder.playOrPause();
                    play_button();
                }
                else {

                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(playService.binder.isPlaying()){

                }
                else {
                    playService.binder.playOrPause();
                    play_button();
                }
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
