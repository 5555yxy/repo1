package xyz.yxy.notebook;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import xyz.yxy.notebook.MainActivity;

public class Article {
    int id_;
    String title_;
    String author_;
    String time_;
    String art_;
    String picture_;
    void setId_(int id){
        this.id_=id;
    }
    void setTitle_(String tit){
        this.title_=tit;
    }
    void setAuthor_(String aut){
        this.author_=aut;
    }
    void setTime_(String tim){
        this.time_=tim;
    }
    void setArt_(String ar){
        this.art_=ar;
    }
    void setPicture_(String pic){this.picture_=pic;}
}
