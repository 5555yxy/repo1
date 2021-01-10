package xyz.yxy.notebook;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import xyz.yxy.notebook.ImageUtils;

import static android.support.v4.content.FileProvider.getUriForFile;
import static java.security.AccessController.getContext;

public class WriteActivity extends AppCompatActivity {
    public static final int REQUEST_PICK_IMAGE = 11101;
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private ImageView picture;
    private Uri imageUri;


    private MyDatabaseHelper dbHelper;
    int a=2;
    int ll;

    public static Article[] insert_article(Article[] arr, Article str)
    {
        int size = arr.length;
        Article[] tmp = new Article[size + 1];
        System.arraycopy(arr, 0, tmp, 0, size);
        tmp[size] = str;
        return tmp;
    }

    public Article get_article_from_database(int l){
        Article[] datas={};
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("article",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndex("title"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String article=cursor.getString(cursor.getColumnIndex("art"));
                String time=cursor.getString(cursor.getColumnIndex("time"));
                String author=cursor.getString(cursor.getColumnIndex("author"));
                Article data=new Article();
                data.setAuthor_(author);
                data.setTitle_(title);
                data.setArt_(article);
                data.setTime_(time);
                datas=insert_article(datas,data);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return datas[l];
    }

    public int from_item_get_id(int l){
        int[] data={};
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        Cursor cursor = db.query("article",null,null,null,null,null,null);
        if(cursor.moveToFirst()){
            do{
                String title=cursor.getString(cursor.getColumnIndex("title"));
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                data=MainActivity.insert_int(data,id);
            }while(cursor.moveToNext());
        }
        cursor.close();
        return data[l];
    }

    public void update_database(int id,String title,String author,String time,String art){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id",id);
        values.put("title",title);
        values.put("author",author);

        values.put("time",time);
        values.put("art",art);
//        db.insert("article", null,values);
        String sql="update article set title=?"+",author=?"+",art=? where id=?";
//        db.update("article",values,"title=?",new String[]{title});
        db.execSQL(sql,new Object[] {title,author,art,id});
        values.clear();
    }

    public void update_database_insert_pic(int id, String pic){
        SQLiteDatabase db=dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id",id);
        values.put("art",pic);
//        db.insert("article", null,values);
        String sql="update article set picture=? where id=?";
//        db.update("article",values,"title=?",new String[]{title});
        Log.d("database_insert_pic",pic);
        db.execSQL(sql,new Object[] {pic,id});
        values.clear();
    }

    //region 初始化content内容，参考：
    //  http://blog.sina.com.cn/s/blog_766aa3810100u8tx.html#cmt_523FF91E-7F000001-B8CB053C-7FA-8A0
    //  https://segmentfault.com/q/1010000004268968
    //  http://www.jb51.net/article/102683.htm
    private void initContent(){
        String input = get_article_from_database(ll).art_;
        //String regex = "<img src=\\".*?\\"\\/>";
        Pattern p = Pattern.compile("\\<img src=\".*?\"\\/>");
        Matcher m = p.matcher(input);
        //List<String> result = new ArrayList<String>();


        SpannableString spannable = new SpannableString(input);
        while(m.find()){
            //Log.d("YYPT_RGX", m.group());
            //这里s保存的是整个式子，即<img src="xxx"/>，start和end保存的是下标
            String s = m.group();
            int start = m.start();
            int end = m.end();
            //path是去掉<img src=""/>的中间的图片路径
            String path = s.replaceAll("\\<img src=\"|\"\\/>","").trim();
            //Log.d("YYPT_AFTER", path);

            //利用spannableString和ImageSpan来替换掉这些图片
            int width = ScreenUtils.getScreenWidth(WriteActivity.this);
            int height = ScreenUtils.getScreenHeight(WriteActivity.this);

            try {
                Bitmap bitmap = ImageUtils.getSmallBitmap(path, width, 480);
                ImageSpan imageSpan = new ImageSpan(this, bitmap);
                spannable.setSpan(imageSpan, start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        text_article.setText(spannable);
        //content.append("\n");
        //Log.d("YYPT_RGX_SUCCESS",content.getText().toString());
    }

    //region 插入图片
    private void insertImg(String path){
        String tagPath = "<img src=\""+path+"\"/>";//为图片路径加上<img>标签
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        if(bitmap != null){
            SpannableString ss = getBitmapMime(path, tagPath);
            insertPhotoToEditText(ss);
            text_article.append("\n");
            Log.d("YYPT_Insert", text_article.getText().toString());

        }else{
            Toast.makeText(WriteActivity.this,"插入失败，无读写存储权限，请到权限中心开启",Toast.LENGTH_LONG).show();
        }
    }
    //endregion

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private void callGallery(){

        int permission = ActivityCompat.checkSelfPermission(WriteActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(WriteActivity.this,PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
        }

        //调用系统图库
        //Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");  //相片类型
        //startActivityForResult(intent,1);

        Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
        getAlbum.setType("image/*");
        startActivityForResult(getAlbum,CHOOSE_PHOTO);


    }

    //region 将图片插入到EditText中
    private void insertPhotoToEditText(SpannableString ss){
        Editable et = text_article.getText();
        int start = text_article.getSelectionStart();
        et.insert(start,ss);
        text_article.setText(et);
        text_article.setSelection(start+ss.length());
        text_article.setFocusableInTouchMode(true);
        text_article.setFocusable(true);
    }
    //endregion

    //region 根据图片路径利用SpannableString和ImageSpan来加载图片
    private SpannableString getBitmapMime(String path,String tagPath) {
        SpannableString ss = new SpannableString(tagPath);//这里使用加了<img>标签的图片路径
        int width = ScreenUtils.getScreenWidth(WriteActivity.this);
        int height = ScreenUtils.getScreenHeight(WriteActivity.this);
        Log.d("ScreenUtils", "高度:"+height+",宽度:"+width);
        Bitmap bitmap = ImageUtils.getSmallBitmap(path,width,480);
        Log.d("YYPT_IMG_COMPRESS", "高度："+bitmap.getHeight()+",宽度:"+bitmap.getWidth());
        ImageSpan imageSpan = new ImageSpan(this, bitmap);
        ss.setSpan(imageSpan, 0, tagPath.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ss;
    }
    //endregion

    static final int IMAGE_CODE = 99;

    @Override
    public void onBackPressed() {
        Article article=new Article();
        article.setId_(a_id);
        article.setAuthor_("I");
        article.setArt_(text_article.getText().toString());
        article.setTitle_(text_title.getText().toString());
        update_database(article.id_,article.title_,article.author_,article.time_,article.art_);
        Toast.makeText(WriteActivity.this,"Saved", Toast.LENGTH_SHORT).show();
        WriteActivity.super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ContentResolver resolver = getContentResolver();
        Bitmap bm = null;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    if (data != null) {
                        String realPathFromUri = RealPathFromUriUtils.getRealPathFromUri(this, data.getData());
                        Toast.makeText(this,realPathFromUri,Toast.LENGTH_SHORT).show();
                        Log.d("notebook",realPathFromUri);
                        File oldfile=new File(realPathFromUri);
                        String path=oldfile.getName();
                        File file=new File("/storage/emulated/0/Pictures/yxynotebook/");
                        if(!file.exists()){
                            file.mkdir();
                        }
                        copyFile(realPathFromUri,"/storage/emulated/0/Pictures/yxynotebook/"+path);
                        update_database_insert_pic(ll,"/storage/emulated/0/Pictures/yxynotebook/"+path);
                        insertImg(path);
                    } else {
                        Toast.makeText(this, "图片损坏，请重新选择", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case TAKE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        try {
                            // 将拍摄的照片显示出来
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            Matrix matrix = new Matrix();
//                        matrix.postScale(1f, 1f);
                            matrix.postRotate(90);
                            Bitmap dstbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                                    matrix, true);
//                            picture.setImageBitmap(dstbmp);
                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                            Date date = new Date(System.currentTimeMillis());
                            String date_=simpleDateFormat.format(date);
                            File pic = new File("/storage/emulated/0/Pictures/yxynotebook/"+date_+".png");
                            FileOutputStream out = new FileOutputStream(pic);
                            if (dstbmp.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                                out.flush();
                                out.close();
                            }
                            int a_id = from_item_get_id((int) id);
                            update_database_insert_pic(a_id, "/storage/emulated/0/Pictures/yxynotebook/"+date_+".png");
                            insertImg("/storage/emulated/0/Pictures/yxynotebook/"+date_+".png");
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                case CHOOSE_PHOTO:
                    if (resultCode == RESULT_OK) {
                        String realPathFromUri = RealPathFromUriUtils.getRealPathFromUri(this, data.getData());
                        Toast.makeText(this,realPathFromUri,Toast.LENGTH_SHORT).show();
                        Log.d("notebook",realPathFromUri);
                        File oldfile=new File(realPathFromUri);
                        String path=oldfile.getName();
                        WriteActivity.copyFile(realPathFromUri,"/storage/emulated/0/Pictures/yxynotebook/"+path);
                        int a_id=from_item_get_id((int)id);
                        update_database_insert_pic(a_id,"/storage/emulated/0/Pictures/yxynotebook/"+path);
                        insertImg("/storage/emulated/0/Pictures/yxynotebook/"+path);
                        break;
                    }
                    break;
                case IMAGE_CODE:
                    try{
                        // 获得图片的uri
                        Uri originalUri = data.getData();
                        bm = MediaStore.Images.Media.getBitmap(resolver,originalUri);
                        String[] proj = {MediaStore.Images.Media.DATA};
                        // 好像是android多媒体数据库的封装接口，具体的看Android文档
                        Cursor cursor = managedQuery(originalUri,proj,null,null,null);
                        // 按我个人理解 这个是获得用户选择的图片的索引值
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        // 将光标移至开头 ，这个很重要，不小心很容易引起越界
                        cursor.moveToFirst();
                        // 最后根据索引值获取图片路径
                        String path = cursor.getString(column_index);
                        insertImg(path);
                        //Toast.makeText(AddFlagActivity.this,path,Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(WriteActivity.this,"图片插入失败",Toast.LENGTH_SHORT).show();
                    }
                default:
                    break;
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int i=0;
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            File newfile = new File(newPath);
            if (oldfile.exists()) { //文件不存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                Log.d("notebook","复制中");
                inStream.close();
                fs.close();
            }
        }
        catch (Exception e) {
            Log.d("notebook","复制出错");
            e.printStackTrace();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private void displayImage(String imagePath) {
        if (imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    EditText text_article;
    EditText text_title;
    long id;
    int a_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final long[] patter = { 0, 5, 2, 5 };
        final Vibrator vib = (Vibrator) this.getSystemService(this.VIBRATOR_SERVICE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);


        dbHelper=new MyDatabaseHelper(this,"articleset.db",null,2);
        dbHelper.getWritableDatabase();

//        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
//        if(actionBar != null){
//            actionBar.setHomeButtonEnabled(true);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//        }

        Bundle bundle=this.getIntent().getExtras();
        id = bundle.getLong("id");
        text_article = findViewById(R.id.edit_article);
        text_title = findViewById(R.id.edit_title);
        final Button button_save = findViewById(R.id.button_save);
        final Button button_insert_pic=findViewById(R.id.button_insert_pic);
        final Button button_edit=findViewById(R.id.button_edit);
        final Button button_showpic=findViewById(R.id.back);
        Article data=new Article();
        a_id=from_item_get_id((int)id);
        ll=(int)id;
        data=get_article_from_database((int)id);
        text_article.setText(data.art_);
        text_title.setText(data.title_);
        text_article.setFocusable(true);
        text_article.setFocusableInTouchMode(true);
        text_article.requestFocus();
        text_article.setSelection(text_article.getText().length());
        initContent();
        String[] mPermissionList = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
        };
        ActivityCompat.requestPermissions(WriteActivity.this, mPermissionList, 100);
        onRequestPermissionsResult(REQUEST_PICK_IMAGE,null,null);
        File file=new File("/storage/emulated/0/Pictures/yxynotebook/");
        if(!file.exists()){
            file.mkdir();
        }
        File no=new File("/storage/emulated/0/Pictures/yxynotebook/"+".nomedia");
        if(!file.exists()){
            try {
                no.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        button_showpic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:// 松开事件发生后执行代码的区域
                        vib.vibrate(patter, -1);
                        AlertDialog.Builder bb = new AlertDialog.Builder(WriteActivity.this);

                        bb.setPositiveButton("save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Article article=new Article();
                                article.setId_(a_id);
                                article.setAuthor_("I");
                                article.setArt_(text_article.getText().toString());
                                article.setTitle_(text_title.getText().toString());
                                update_database(article.id_,article.title_,article.author_,article.time_,article.art_);
                                Toast.makeText(WriteActivity.this,"Save", Toast.LENGTH_SHORT).show();
                                WriteActivity.super.onBackPressed();
                            }
                        });

                        bb.setNegativeButton("don't save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                WriteActivity.super.onBackPressed();
                            }
                        });
                        bb.setMessage("是否保存？");
                        bb.setTitle("提示");
                        bb.show();
                    case MotionEvent.ACTION_DOWN:// 按住事件发生后执行代码的区域
                        vib.vibrate(patter, -1);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });


        button_edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:// 松开事件发生后执行代码的区域
                        vib.vibrate(patter, -1);
                        if(a%2==0){
                            text_title.setEnabled(false);
                            text_article.setEnabled(false);
                            text_title.setFocusable(false);
                            text_article.setFocusableInTouchMode(false);
                            text_title.setFocusable(false);
                            text_article.setFocusableInTouchMode(false);
                            button_insert_pic.setEnabled(false);
                            button_edit.setText("已锁定");
//                            InputMethodManager imm = (InputMethodManager) WriteActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
//                            if (view !=null && imm != null){
//                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);  //强制隐藏
//                            }
                            a++;
                        }else if(a%2==1){
                            text_title.setEnabled(true);
                            text_article.setEnabled(true);
                            text_title.setFocusable(true);
                            text_article.setFocusable(true);
                            text_title.setFocusableInTouchMode(true);
                            text_article.setFocusableInTouchMode(true);
                            button_insert_pic.setEnabled(true);
                            button_edit.setText("未锁定");
                            a++;
                        }
                    case MotionEvent.ACTION_DOWN:// 按住事件发生后执行代码的区域
                        vib.vibrate(patter, -1);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });





        button_insert_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        button_insert_pic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_UP:// 松开事件发生后执行代码的区域
                        vib.vibrate(patter, -1);



                        final CharSequence[] items = { "手机相册", "相机拍摄" };
                        android.app.AlertDialog dlg = new android.app.AlertDialog.Builder(WriteActivity.this).setTitle("选择图片").setItems(items,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int item) {
                                        if(item==1){
//                                            Intent intent=new Intent(WriteActivity.this,CameraActivity.class);
//                                            Bundle bundle=new Bundle();
//                                            bundle.putLong("id",id);
//                                            intent.putExtras(bundle);
//                                            startActivity(intent);
                                            File outputImage = new File(getExternalCacheDir(), "output_image.jpg");
                                            try {
                                                if (outputImage.exists()) {
                                                    outputImage.delete();
                                                }
                                                outputImage.createNewFile();
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            if (Build.VERSION.SDK_INT < 24) {
                                                imageUri = Uri.fromFile(outputImage);
                                            } else {
                                                imageUri = FileProvider.getUriForFile(WriteActivity.this, "xyz.yxy.cameraalbumtest.fileprovider", outputImage);
                                            }
                                            // 启动相机程序
                                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                                            startActivityForResult(intent, TAKE_PHOTO);
                                        }else{
//                                            Intent intent=new Intent(WriteActivity.this,PictureActivity.class);
//                                            Bundle bundle=new Bundle();
//                                            bundle.putLong("id",id);
//                                            intent.putExtras(bundle);
//                                            startActivity(intent);
                                            callGallery();
                                        }
                                    }
                                }).create();
                        dlg.show();
                    case MotionEvent.ACTION_DOWN:// 按住事件发生后执行代码的区域
                        vib.vibrate(patter, -1);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vib.vibrate(patter, -1);
                Article article=new Article();
                article.setId_(a_id);
                article.setAuthor_("I");
                article.setArt_(text_article.getText().toString());
                article.setTitle_(text_title.getText().toString());
                update_database(article.id_,article.title_,article.author_,article.time_,article.art_);
                Toast.makeText(WriteActivity.this,"Save", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onDestroy() {
        Intent intent = new Intent();
        intent.setAction("action.refresh_first");
        sendBroadcast(intent);
        super.onDestroy();
    }
}
