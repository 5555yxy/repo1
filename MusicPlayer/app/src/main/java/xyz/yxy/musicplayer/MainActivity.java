package xyz.yxy.musicplayer;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    String file_path;//"/storage/emulated/0/netease/cloudmusic/Music"

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    data=search_File_list("");
                    refresh_list(data);
                } else {
                    Toast.makeText(this, "拒绝权限将无法使用程序", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
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

    private String[] search_File_list(String keyword) {
        String[] result = {"返回上一级"};

        File rootfile = new File(file_path);//"/storage/emulated/0"
        File[] files = rootfile.listFiles();
        if(files!=null){
            for (File file : files) {
                if (file.getName().contains(keyword) && !file.getName().contains(".ncm") && file.getName().substring(file.getName().lastIndexOf("/")+1).indexOf(".")!=0) {
                    result=insert(result,file.getPath());
                }
            }
            if (result.equals("")){

            }
        }
        return result;
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    public String[] show_list(String keyword){
        return search_File_list(keyword);
    }

    public void open_play_activity(String path){
        Intent intent=new Intent(MainActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("path",path);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void open_play_activity(){
        Intent intent=new Intent(MainActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("path","***");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public static boolean is_music_file(String result){
        if(result.indexOf(".mp3")>= 0|| result.indexOf(".flac")>= 0|| result.indexOf(".aac")>= 0){
            return true;
        }
        return false;
    }

    public void refresh_list(String[] keyword){
        String[] res={};
        res=insert(res,keyword[0]);
        for(int i = 1;i<keyword.length;i++){
            res=insert(res,keyword[i].substring(keyword[i].lastIndexOf("/")+1));//keyword[i].substring(20)
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,res);
        result.setAdapter(adapter);
    }

    public void onStop(){
        SharedPreferences.Editor editor=getSharedPreferences("last_path",MODE_PRIVATE).edit();
        editor.putString("path",file_path);
        editor.apply();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private static MyDatabaseHelper dbHelper;
    public void insert_song_path_to_database(String path){
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getWritableDatabase();
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("song_path",path);
        db.insert("songs", null,values);
        Toast.makeText(MainActivity.this,"收藏成功",Toast.LENGTH_SHORT).show();
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

    public void one_second(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                        drawer.openDrawer(GravityCompat.START);
                    }
                });
//                try {
//                    Thread.sleep(900);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                        drawer.closeDrawer(GravityCompat.START);
//                    }
//                });
            }
        }).start();
    }

    ListView result;
    String[] data;
    FloatingActionButton fab;
    Button button_search;
    EditText editText_search;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getWritableDatabase();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.openDrawer(GravityCompat.START);

        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

        File filels=new File("/storage/emulated/0/Music/ls");
        if(filels.exists()){
            filels.delete();
        }

        result = (ListView) this.findViewById(R.id.result);
        button_search=findViewById(R.id.button);
        editText_search=findViewById(R.id.edittext);
        SharedPreferences pref = getSharedPreferences("last_path",MODE_PRIVATE);
        file_path=pref.getString("path","/storage/emulated/1");
        String keyword = "";
        data=search_File_list(keyword);
        refresh_list(data);
        one_second();

        result.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position==0){
                    if(file_path.equals("/storage/emulated/0")){//if(file_path.equals("/storage/emulated/0")){

                    }else {
                        file_path=file_path.substring(0,file_path.lastIndexOf("/"));

                        if(file_path.equals("/storage/emulated/0")){
                            MainActivity.this.setTitle("root");
                        }else {
                            MainActivity.this.setTitle(file_path.substring(20));//20
                        }
//                        Toast.makeText(MainActivity.this,file_path,Toast.LENGTH_SHORT).show();
                        data=search_File_list("");
                        refresh_list(data);
                    }
                }else {
                    if(is_music_file(data[position])){
                        open_play_activity(data[position]);
                    }
                    else {
                        file_path=data[position];
                        MainActivity.this.setTitle(file_path.substring(20));
//                        Toast.makeText(MainActivity.this,file_path,Toast.LENGTH_SHORT).show();
                        data=search_File_list("");
                        refresh_list(data);
                    }
                }
            }
        });

        result.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                if(is_music_file(data[position])){

                    final CharSequence[] items = { "收藏", "删除" };
                    android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(MainActivity.this).setItems(items,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int item) {
                                    if(item==1){
                                        AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
                                        bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int in) {
                                                File file=new File(data[position]);
                                                if(file.exists()){
                                                    file.delete();
                                                    data=search_File_list(editText_search.getText().toString());
                                                    refresh_list(data);
                                                }
                                            }
                                        });
                                        bb.setNegativeButton("否", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        bb.setMessage("删除歌曲？");
                                        bb.show();
                                    }else{
                                        if(song_already_exist(data[position])==true){
                                            Toast.makeText(MainActivity.this,"已收藏",Toast.LENGTH_SHORT).show();
                                        }else {
                                            insert_song_path_to_database(data[position]);
                                        }
                                    }
                                }
                            }).create();
                    dlg.show();


                }
                return true;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "歌曲后台播放", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                open_play_activity();
            }
        });

        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                data=search_File_list(editText_search.getText().toString());
                refresh_list(data);
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (!drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.openDrawer(GravityCompat.START);
            if(file_path.equals("/storage/emulated/0")){
                drawer.openDrawer(GravityCompat.START);
                Toast.makeText(MainActivity.this,"再按一下退出",Toast.LENGTH_SHORT).show();
            }else {
                file_path=file_path.substring(0,file_path.lastIndexOf("/"));
//                Toast.makeText(MainActivity.this,file_path,Toast.LENGTH_SHORT).show();
                if(file_path.equals("/storage/emulated/0")){
                    MainActivity.this.setTitle("root");
                }else {
                    MainActivity.this.setTitle(file_path.substring(20));
                }
                data=search_File_list("");
                refresh_list(data);
            }
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode == KeyEvent.KEYCODE_BACK){
//            moveTaskToBack(false);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(MainActivity.this,"关于",Toast.LENGTH_LONG).show();
            return true;
        }
        if (id == R.id.action_help) {
            Toast.makeText(MainActivity.this,"使用方法:\n   省略···",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent=new Intent(MainActivity.this,StarActivity.class);
            startActivity(intent);
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {
            Intent intent=new Intent(MainActivity.this,OnlineActivity.class);
            startActivity(intent);
        } else if(id==R.id.nav_local){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
        }else if(id==R.id.nav_playing){
            open_play_activity();
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent){
        boolean open=intent.getBooleanExtra("open",true);
        if(open){
            new Thread( new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            open_play_activity();
                        }
                    });
                }
            }).start();
        }
        else {

        }
        super.onNewIntent(intent);
    }

    public void onResume(){
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(350);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                MainActivity.this.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//                        drawer.openDrawer(GravityCompat.START);
//                    }
//                });
//            }
//        }).start();
        super.onResume();
    }
}
