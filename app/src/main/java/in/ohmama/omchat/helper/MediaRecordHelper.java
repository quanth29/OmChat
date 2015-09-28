package in.ohmama.omchat.helper;

import android.media.MediaPlayer;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

import in.ohmama.omchat.*;

/**
 * Created by Leon on 9/12/15.
 */
public class MediaRecordHelper {

    private boolean isRecord = false;
    private MediaRecorder mMediaRecorder;

    private MediaRecordHelper() {
    }

    private static MediaRecordHelper mInstance;

    public synchronized static MediaRecordHelper getInstance() {
        if (mInstance == null)
            mInstance = new MediaRecordHelper();
        return mInstance;
    }

    public int startRecordAndFile(String fileName) {
        //判断是否有外部存储设备sdcard
        if (isRecord) {
            return ErrorCode.E_STATE_RECODING;
        } else {
            if (mMediaRecorder == null)
                createMediaRecord(fileName);

            try {
                mMediaRecorder.prepare();
                mMediaRecorder.start();
                // 让录制状态为true
                isRecord = true;
                return ErrorCode.SUCCESS;
            } catch (IOException ex) {
                ex.printStackTrace();
                return ErrorCode.E_UNKOWN;
            }
        }

    }

    public void play(String fileName) {
        MediaPlayer player = new MediaPlayer();
        String path = filePath(fileName);
        try {
            player.setDataSource(path);
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.start();
    }


    public void stopRecordAndFile() {
        close();
    }

    public long getRecordFileSize(String name) {
        return getFileSize(filePath(name));
    }


    private void createMediaRecord(String fileName) {
         /* ①Initial：实例化MediaRecorder对象 */
        mMediaRecorder = new MediaRecorder();

        /* setAudioSource/setVedioSource*/
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);//设置麦克风

        /* 设置输出文件的格式：THREE_GPP/MPEG-4/RAW_AMR/Default
         * THREE_GPP(3gp格式，H263视频/ARM音频编码)、MPEG-4、RAW_AMR(只支持音频且音频编码要求为AMR_NB)
         */
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);

         /* 设置音频文件的编码：AAC/AMR_NB/AMR_MB/Default */
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

         /* 设置输出文件的路径 */
        File file = new File(filePath(fileName));
        if (file.exists()) {
            file.delete();
        } else {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.setOutputFile(filePath(fileName));
    }


    private void close() {
        if (mMediaRecorder != null) {
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }

    private String filePath(String name) {
        String dir = Constants.SOUND_PATH;
        File dirFile = new File(dir);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }
        return dir + name + in.ohmama.omchat.Constants.AMR_SURFFIX;
    }

    /**
     * 获取文件大小
     *
     * @param path,文件的绝对路径
     * @return
     */
    public static long getFileSize(String path) {
        File mFile = new File(path);
        if (!mFile.exists())
            return -1;
        return mFile.length();

    }

    class ErrorCode {
        public final static int SUCCESS = 1000;
        public final static int E_STATE_RECODING = 1002;
        public final static int E_UNKOWN = 1003;
    }

}
