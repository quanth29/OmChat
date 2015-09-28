package in.ohmama.omchat.ui.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;

/**
 * Created by yanglone on 9/24/15.
 */
public class VideoPlayView extends SurfaceView {

    private MediaPlayer mediaPlayer;
    private SurfaceHolder mHolder;
    String filePath;
    static String TAG = "omlog";

    public VideoPlayView(Context context, MediaPlayer mp) {
        super(context);

        mediaPlayer = mp;
        mHolder = getHolder();
        mHolder.addCallback(videoPlayCallback);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private SurfaceHolder.Callback videoPlayCallback = new SurfaceHolder.Callback() {
        // SurfaceHolder被修改的时候回调
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被销毁");
            // 销毁SurfaceHolder的时候记录当前的播放位置并停止播放
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(TAG, "SurfaceHolder 被创建");
            // 创建SurfaceHolder的时候，如果存在上次播放的位置，则按照上次播放位置进行播放
            videoPlay(filePath);
            // 设置显示视频的SurfaceHolder
            mediaPlayer.setDisplay(mHolder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Log.i(TAG, "SurfaceHolder 大小被改变");
        }

    };

    public void videoPlay(String filePath) {
        // 获取视频文件地址
        File file = new File(filePath);

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // 设置播放的视频源
            mediaPlayer.setDataSource(file.getAbsolutePath());
            Log.i(TAG, "开始装载");
            mediaPlayer.prepareAsync();
            mediaPlayer.setDisplay(mHolder);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "装载完成");
                    mediaPlayer.start();
                    // 按照初始位置播放
//                    mediaPlayer.seekTo(msec);
                }
            });

            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    // 发生错误重新播放
//                    play(0);
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
