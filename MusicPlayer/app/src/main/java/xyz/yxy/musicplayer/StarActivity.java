package xyz.yxy.musicplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

import static xyz.yxy.musicplayer.MainActivity.insert;

public class StarActivity extends AppCompatActivity {

    private MyDatabaseHelper dbHelper;
    public String[] show_star_song_list(){
        String[] datas={};
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        Cursor cursor = db.query("songs",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String path = cursor.getString(cursor.getColumnIndex("song_path"));
                path=path.substring(path.lastIndexOf("/")+1);
                datas=insert(datas,path);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return datas;
    }

    public String[] show_star_song_list_full_path(){
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

    public void open_play_activity(int position){
        Intent intent=new Intent(StarActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("is_star_music","true");
        bundle.putInt("position_in_database",position);
        bundle.putString("path","database");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public void open_play_activity(){
        Intent intent=new Intent(StarActivity.this,PlayActivity.class);
        Bundle bundle=new Bundle();
        bundle.putString("path","***");
        intent.putExtras(bundle);
        startActivity(intent);
    }

    public String from_position_get_song(int l){
        String[] data={};
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("songs",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String path = cursor.getString(cursor.getColumnIndex("song_path"));
                data=insert(data,path);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return data[l];
    }

    private void scrollMyListViewToPosition(final ListView myListView,final long p) {
        myListView.post(new Runnable() {
            @Override
            public void run() {
                myListView.setSelection((int)p-3);
            }
        });
    }

    public boolean is_equals(String[] a, String[] b){
        if(a.length==b.length){
            int l= a.length;
            for(int i=0;i<l;i++){
                if(a[i].equals(b[i])){

                }else {
                    return false;
                }
            }
            return true;
        }else {
            return false;
        }
    }

    public void onResume(){
        String[] datas=show_star_song_list();
        if(is_equals(datas,data)){

        }else {
            data=show_star_song_list();
            ArrayAdapter<String> adapter1=new ArrayAdapter<String>(StarActivity.this,android.R.layout.simple_list_item_1,data);
            listView_star_list.setAdapter(adapter1);
        }
        super.onResume();
    }

    ListView listView_star_list;
    String[] data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_star);
        listView_star_list=findViewById(R.id.star_list);
        dbHelper=new MyDatabaseHelper(this,"songs.db",null,1);
        dbHelper.getWritableDatabase();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "歌曲后台播放", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                open_play_activity();
            }
        });

        data=show_star_song_list();
        ArrayAdapter<String> adapter1=new ArrayAdapter<String>(StarActivity.this,android.R.layout.simple_list_item_1,data);
        listView_star_list.setAdapter(adapter1);

        listView_star_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File file=new File(show_star_song_list_full_path()[position]);
                if(file.exists()){
                    open_play_activity(position);
                }else {
                    Toast.makeText(StarActivity.this,"歌曲不存在",Toast.LENGTH_SHORT).show();
                    SQLiteDatabase db=dbHelper.getWritableDatabase();
                    String a=from_position_get_song(position);
                    db.delete("songs","song_path=?",new String[] {a});
                    data=show_star_song_list();
                    ArrayAdapter<String> adapter=new ArrayAdapter<String>(StarActivity.this,android.R.layout.simple_list_item_1,data);
                    ListView listView=(ListView)findViewById(R.id.star_list);
                    scrollMyListViewToPosition(listView,id);
                    listView.setAdapter(adapter);
                }
            }
        });

        listView_star_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, final long id) {
                AlertDialog.Builder bb = new AlertDialog.Builder(StarActivity.this);
                bb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int in) {
                        SQLiteDatabase db=dbHelper.getWritableDatabase();
                        String a=from_position_get_song(position);
                        db.delete("songs","song_path=?",new String[] {a});
                        data=show_star_song_list();
                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(StarActivity.this,android.R.layout.simple_list_item_1,data);
                        ListView listView=(ListView)findViewById(R.id.star_list);
                        scrollMyListViewToPosition(listView,id);
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
                return true;
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
            StarActivity.this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
