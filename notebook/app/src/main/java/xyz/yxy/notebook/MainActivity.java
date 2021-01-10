package xyz.yxy.notebook;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.SystemClock.sleep;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private MyDatabaseHelper dbHelper;
    int article_id=0;
    int list_length=0;

    public int from_item_get_id(int l){
        int[] data={};
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("article",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndex("title"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                data=insert_int(data,id);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return data[l];
    }

    public int get_bigist_id(){
        int data=0;
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("article",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                if(id>data){
                    data=id;
                }
            }while(cursor.moveToNext());
        }
        cursor.close();
        return data;
    }

    private void scrollMyListViewToBottom(final ListView myListView, final ArrayAdapter myListAdapter) {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                myListView.setSelection(myListAdapter.getCount() - 1);
            }
        });
    }

    private void scrollMyListViewToBottom(final ListView myListView, final ArrayAdapter myListAdapter,final long p) {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                myListView.setSelection((int)p-3);
            }
        });
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

    public String[] showlist(){
        String[] datas={};
        int num=1;
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.query("article",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndex("title"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                int size = datas.length;
                String[] tmp=new String[size+1];
                tmp[size]=title;
                datas=insert(datas,time+"  "+title);
                num++;
//                article_id=datas.length;
            }while(cursor.moveToNext());
        }
        cursor.close();
        return datas;
    }
    public void insert_database(int id,String title,String author,String time,String art){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id",id);
        values.put("title",title);
        values.put("author",author);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String date_=simpleDateFormat.format(date);
        values.put("time",date_);
        values.put("art",art);
        db.insert("article", null,values);
        values.clear();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        long[] patter = { 0, 5, 2, 5 };
        final Vibrator vib = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        vib.vibrate(patter, -1);
        dbHelper=new MyDatabaseHelper(this,"articleset.db",null,2);
        dbHelper.getWritableDatabase();
        File file=new File("/storage/emulated/0/Pictures/yxynotebook/");
        if(!file.exists()){
            file.mkdir();
        }
        File no=new File("/storage/emulated/0/Pictures/yxynotebook/.nomedia");
        if(!file.exists()){
            try {
                no.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action.refresh_first");
        registerReceiver(refresh_first_activity, intentFilter);

        article_id=get_bigist_id();

        String[] data=showlist();
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
        ListView listView=(ListView)findViewById(R.id.listview);
        scrollMyListViewToBottom(listView,adapter);
        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {

                AlertDialog.Builder bb = new AlertDialog.Builder(MainActivity.this);
                bb.setPositiveButton("sure", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int in) {
                        SQLiteDatabase db=dbHelper.getWritableDatabase();
                        int a=from_item_get_id(i);
                        db.delete("article","id=?",new String[] {a+""});

                        String[] data=showlist();
                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                        ListView listView=(ListView)findViewById(R.id.listview);
                        scrollMyListViewToBottom(listView,adapter,i);
                        listView.setAdapter(adapter);
                        list_length--;
                    }
                });

                bb.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                bb.setMessage("是否删除？");
                bb.setTitle("提示");
                bb.show();
                return true;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int a=from_item_get_id((int)i);
                Intent intent=new Intent(MainActivity.this,WriteActivity.class);
                Bundle bundle=new Bundle();
                bundle.putLong("id",i);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                list_length++;
                article_id++;
                Article a = new Article();
                a.id_=article_id;
                a.title_="";
                a.author_="";
                a.time_ = "";
                a.art_="";
                insert_database(a.id_,a.title_,a.author_,a.time_,a.art_);

                String[] data=showlist();
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                ListView listView=(ListView)findViewById(R.id.listview);
                scrollMyListViewToBottom(listView,adapter);
                listView.setAdapter(adapter);

                Intent intent=new Intent(MainActivity.this,WriteActivity.class);
                Bundle bundle=new Bundle();
                bundle.putLong("id",adapter.getCount()-1);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private BroadcastReceiver refresh_first_activity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("action.refresh_first")){
                String[] data=showlist();
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,data);
                ListView listView=(ListView)findViewById(R.id.listview);
                scrollMyListViewToBottom(listView,adapter);
                listView.setAdapter(adapter);
            }
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_articallist) {
            // Handle the camera action
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(refresh_first_activity);
    }
}
