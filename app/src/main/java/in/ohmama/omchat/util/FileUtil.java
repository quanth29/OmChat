package in.ohmama.omchat.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Base64;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Leon on 8/23/15.
 */
public class FileUtil {

    public final static int IMG = 0;
    public final static int SOUND = 1;
    public final static int APK = 2;
    public final static int PPT = 3;
    public final static int XLS = 4;
    public final static int DOC = 5;
    public final static int PDF = 6;
    public final static int CHM = 7;
    public final static int TXT = 8;
    public final static int MOVIE = 9;

    /**
     * 生成写有文字图片的文件流
     *
     * @param text bitmap上显示的文字
     * @return 图片流
     */
    public static InputStream createImageInputStream(String text) {
        Bitmap bm = createBitmap(text);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
        return bs;
    }

    /**
     * 生成带文字的空图片
     *
     * @param text 要生成的文字
     * @return 生成图片
     */
    public static Bitmap createBitmap(String text) {
        Bitmap.Config config = Bitmap.Config.ARGB_8888;
        Bitmap bm = Bitmap.createBitmap(400, 400, config);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(20);

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        Canvas canvas = new Canvas(bm);
        int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
        int y = (canvas.getHeight() / 2) - (bounds.height() / 2);
        canvas.drawARGB(255, 255, 255, 255);
        canvas.drawText(text, x, y - paint.ascent() / 2, paint);
        return bm;
    }

    /**
     * 检查文件是否存在于文件夹中
     *
     * @param dir
     * @param fileName
     * @return
     */
    public static boolean isFileExist(File dir, String fileName) {
        String[] files = dir.list();
        if (files != null) {
            for (String f : files) {
                if (f.equals(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 从文件地址中，截取文件名
     *
     * @param fileUrl
     * @return
     */
    public static String parseUrlTofileName(String fileUrl) {
        return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    }

    /**
     * 是否为图片
     *
     * @param url
     * @return
     */
    public static boolean isPicture(String url) {
        try {
            String reg = "(.+)\\.(jpg|bmp|gif|png)(\\?.+)?";
            if (url.matches(reg))
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 字节写入文件
     *
     * @param data
     * @param filePath
     * @throws IOException
     */
    public static boolean saveFileByBase64(byte[] data, String filePath) {
        // 对字节数组字符串进行Base64解码并生成图燿
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                File file2 = new File(filePath.substring(0, filePath.lastIndexOf("/") + 1));
                file2.mkdirs();
            }
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(data);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

    /**
     * 保存bitmap到本地
     *
     * @param bmp
     * @param filename 文件名
     */
    public static void saveBitmap(Bitmap bmp, String filename) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param fileString base64
     * @param filePath   保存路径,包括名字
     * @return
     */
    public static boolean saveFileByBase64(String fileString, String filePath) {
        // 对字节数组字符串进行Base64解码并生成图燿
        if (fileString == null) // 图像数据为空
            return false;
        byte[] data = Base64.decode(fileString, Base64.DEFAULT);
        saveFileByBase64(data, filePath);
//        MyApplication.getInstance().sendBroadcast(
//        		new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,Uri.parse("file://"+filePath)));
        return true;
    }


    /**
     * 判断是否有sdcard
     *
     * @return
     */
    public boolean hasSDCard() {
        boolean b = false;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {
            b = true;
        }
        return b;
    }

    /**
     * 得到sdcard路径
     *
     * @return
     */
    public String getExtPath() {
        String path = "";
        if (hasSDCard()) {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        return path;
    }

    /**
     * 根据路径和名称都可以
     *
     * @param filePath
     * @return
     */
    public static int getType(String filePath) {
        if (filePath == null) {
            return -1;
        }
        String end;
        if (filePath.contains("/")) {
            File file = new File(filePath);
            if (!file.exists())
                return -1;
            /* 取得扩展名 */
            end = file
                    .getName()
                    .substring(file.getName().lastIndexOf(".") + 1,
                            file.getName().length()).toLowerCase();
        } else {
            end = filePath.substring(filePath.lastIndexOf(".") + 1,
                    filePath.length()).toLowerCase();
            ;
        }

        end = end.trim().toLowerCase();
        if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
                || end.equals("xmf") || end.equals("ogg") || end.equals("wav")
                || end.equals("amr")) {
            return SOUND;
        } else if (end.equals("3gp") || end.equals("mp4")) {
            return MOVIE;
        } else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
                || end.equals("jpeg") || end.equals("bmp")) {
            return IMG;
        } else if (end.equals("apk")) {
            return APK;
        } else if (end.equals("ppt")) {
            return PPT;
        } else if (end.equals("xls")) {
            return XLS;
        } else if (end.equals("doc")) {
            return DOC;
        } else if (end.equals("pdf")) {
            return PDF;
        } else if (end.equals("chm")) {
            return CHM;
        } else if (end.equals("txt")) {
            return TXT;
        } else {
            return -1;
        }
    }

}
