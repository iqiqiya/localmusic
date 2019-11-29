package com.iakie.localmusic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView nextIv,playIv,lastIv;
    TextView singerTv,songTv;
    RecyclerView musicRv;

    // 数据源
    List<LocalMusicBean>mDatas;
    private LocalMusicAdapter adapter;
    private int position;
    // 正在播放的位置
    int currnetPlayPosition = -1;
    // 暂停音乐时的位置
    int currentPausePositionInSong = 0;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        mediaPlayer = new MediaPlayer();
        mDatas = new ArrayList<>();

        // 创建适配器对象
        adapter = new LocalMusicAdapter(this, mDatas);
        musicRv.setAdapter(adapter);

        // 设置布局管理器  竖向线性布局  false不反转
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        musicRv.setLayoutManager(layoutManager);

        // 加载本地数据源
        loadLocalMusicData();

        // 设置每一项的点击事件
        setEventListener();
    }

    private void setEventListener() {
        // 设置每一项的点击事件
        adapter.setOnItemClickListener(new LocalMusicAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int possition) {
                currnetPlayPosition = position;
                LocalMusicBean musicBean = mDatas.get(possition);
                // 设置底部显示的歌手名称和歌曲名
                singerTv.setText(musicBean.getSinger());
                songTv.setText(musicBean.getSong());
                stopMusic();
                // 重置多媒体播放器
                mediaPlayer.reset();
                // 设置新的播放路径
                try {
                    mediaPlayer.setDataSource(musicBean.getPath());
                    playMusic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 点击播放按钮
     * 两种情况
     * 1.播放音乐
     * 2.从暂停状态开始播放
     */
    private void playMusic() {
        // 播放音乐 两种情况
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            if (currentPausePositionInSong == 0) {
                // 从头开始播放
                try {
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // 从暂停状态到播放
                mediaPlayer.seekTo(currentPausePositionInSong);
                mediaPlayer.start();
            }
            playIv.setImageResource(R.mipmap.icon_pause);
        }
    }

    private void pauseMusic() {
        // 暂停音乐
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            currentPausePositionInSong = mediaPlayer.getCurrentPosition();
            mediaPlayer.pause();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

    private void stopMusic(){
        // 停止播放
        if (mediaPlayer!=null){
            currentPausePositionInSong = 0;
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            mediaPlayer.stop();
            playIv.setImageResource(R.mipmap.icon_play);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMusic();
    }

    private void loadLocalMusicData() {
        // 加载本地存储中的音乐mp3文件到集合当中
        // 1.获取ContentResolver对象
        ContentResolver resolver = getContentResolver();
        // 2.获取本地音乐存储的Uri地址
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // 3.开始查询地址
        Cursor cursor = resolver.query(uri, null, null, null, null);
        // 4.遍历Cursor
        int id = 0;
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            String singer = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
            id++;
            String sid = String.valueOf(id);
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
            long duraation = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
            String time =  sdf.format(new Date(duraation));
            // 将一行中的数据封装到对象当中
            LocalMusicBean bean = new LocalMusicBean(sid,song,singer,album,time,path);
            mDatas.add(bean);
        }

        // 数据发生变化，提示适配器更新
        adapter.notifyDataSetChanged();
    }

    private void initView() {
        /*初始化控件*/
        nextIv = findViewById(R.id.local_music_bottom_iv_next);
        playIv = findViewById(R.id.local_music_bottom_iv_play);
        lastIv = findViewById(R.id.local_music_bottom_iv_last);
        singerTv = findViewById(R.id.local_music_bottom_tv_singer);
        songTv = findViewById(R.id.local_music_bottom_tv_song);
        musicRv = findViewById(R.id.local_music_rv);

        nextIv.setOnClickListener(this);
        lastIv.setOnClickListener(this);
        playIv.setOnClickListener(this);
    }

    // 三个控制按钮的点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.local_music_bottom_iv_last:
                // TODO
                break;
            case R.id.local_music_bottom_iv_next:
                // TODO
            case R.id.local_music_bottom_iv_play:
                if (currnetPlayPosition == -1) {
                    // 并没有选中播放的音乐
                    Toast.makeText(this, "请先选择想要播放的音乐",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mediaPlayer.isPlaying()) {
                    // 此时正在播放
                    pauseMusic();
                }else {
                    // 此时没有播放音乐，点击开始播放
                    playMusic();
                }
                break;
        }
    }
}
