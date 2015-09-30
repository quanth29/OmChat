package in.ohmama.omchat.helper;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.v4.app.Fragment;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import in.ohmama.omchat.ui.view.CameraPreview;
import in.ohmama.omchat.ui.view.VideoPlayView;

/**
 * Created by yanglone on 9/30/15.
 */
public class VideoPlayHelper {

    public Hashtable<String, WeakReference<MediaPlayer>> players = new Hashtable<>();

    private Context mContext;

    public VideoPlayHelper(Context mContext) {
        this.mContext = mContext;
    }

    public SurfaceView getVideoPreview(String path) {
        MediaPlayer mediaPlayer;
        if (players.get(path) == null) {
            mediaPlayer = new MediaPlayer();
            // 重复播放
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.seekTo(0);
                    mp.start();
                }
            });
        } else {
            mediaPlayer = players.get(path).get();
        }
        VideoPlayView videoPlayPreview = new VideoPlayView(mContext, mediaPlayer);
        videoPlayPreview.setFilePath(path);
        players.put(path, new WeakReference<>(mediaPlayer));
        return videoPlayPreview;
    }

    /**
     * 释放资源
     */
    public void releasePlayer() {
        for (WeakReference<MediaPlayer> playRefs : players.values()) {
            if (playRefs.get() != null) {
                MediaPlayer mediaPlayer = playRefs.get();
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = null;
                }
            }
        }
    }
}
