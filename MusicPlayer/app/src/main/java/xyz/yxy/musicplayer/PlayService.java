package xyz.yxy.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class PlayService extends Service {
    public boolean tag = false;
    public String music_path;

    public PlayService() {

    }

    //  通过 Binder 来保持 Activity 和 Service 的通信
    public MyBinder binder = new MyBinder();

    public class MyBinder extends Binder {
        public MediaPlayer mediaPlayer;
        String music_path_in_binder;
        PlayService getService() {
            return PlayService.this;
        }

        public void start_play(String path) {
            mediaPlayer = new MediaPlayer();
            try {
                music_path_in_binder=path;
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //startForeground(2, getNotification("playing",path.substring(path.lastIndexOf("/")+1)));
            Log.d("musicplayer","###"+path);
        }
        public void start_play_again(String path) {
//            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            try {
                music_path_in_binder=path;
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.setLooping(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("musicplayer","###again"+path);
            //startForeground(2, getNotification("playing",path.substring(path.lastIndexOf("/")+1)));
        }
        public void set_path(String path){
            music_path_in_binder=path;
        }

        public void stop() {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.reset();
//                mediaPlayer.release();//不注释会出问题
                if("***".equals(music_path_in_binder)){

                }else{
                    try {
                        mediaPlayer.setDataSource(music_path_in_binder);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mediaPlayer.setLooping(false);
                }
            }
//            getNotificationManager().cancel(2);
//            stopForeground(true);
            startForeground(2, getNotification("stop",music_path_in_binder.substring(music_path_in_binder.lastIndexOf("/")+1)));

        }

        public void reset() {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
        }

        public void playOrPause() {
            if(mediaPlayer!=null){
                if (mediaPlayer.isPlaying()) {
                    //stopForeground(true);
                    mediaPlayer.pause();
                    startForeground(2, getNotification("pause",music_path_in_binder.substring(music_path_in_binder.lastIndexOf("/")+1)));
                } else {
                    //stopForeground(true);
                    mediaPlayer.start();
                    startForeground(2, getNotification("playing",music_path_in_binder.substring(music_path_in_binder.lastIndexOf("/")+1)));
                }
            }
        }

        public boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }

        public boolean exist_music_media(){
            if(mediaPlayer!=null){
                return true;
            }
            else {
                return false;
            }
        }

        void setNextMediaPlayer(MediaPlayer next){

        }

        public String get_playing_path(){
            return music_path;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title,String song_name) {
        String CHANNEL_ONE_ID = "xyz.yxy.musicplayer.PlayActivity";
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setChannelId(CHANNEL_ONE_ID);
        builder.setContentText(song_name);
        return builder.build();
    }

    public void cancel_notification(){
        getNotificationManager().cancel(2);
        stopForeground(true);
    }
}
