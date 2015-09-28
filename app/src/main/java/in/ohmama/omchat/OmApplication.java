package in.ohmama.omchat;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.squareup.picasso.Cache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import in.ohmama.omchat.model.DaoMaster;
import in.ohmama.omchat.model.DaoSession;
import in.ohmama.omchat.model.DbCore;
import in.ohmama.omchat.model.OmUserInfo;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by Leon on 9/13/15.
 */
public class OmApplication extends Application {

    private static Context context;
    public static String BASE_PATH;
    public static String AVATOR_BASE_PATH;
    private Picasso picasso;

    //    public static String userName;
    public static OmUserInfo userSelf;
    //    public static VCard userInfo;
//    private static SQLiteDatabase db;
//    private static DaoMaster daoMaster;
//    private static DaoSession daoSession;
//    private Picasso picasso;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        DbCore.init(this);
        context = getApplicationContext();
        BASE_PATH = getFilesDir().getPath();
        AVATOR_BASE_PATH = BASE_PATH + "/avators/";
    }



    public Picasso getPicasso() {
        if (picasso == null) {
            setPicaso();
        }
        return picasso;

    }

    private void setPicaso() {
        // Prepare OkHttp
//        File httpCacheDirectory = new File(getCacheDir(), "photos");
//        Cache cache = new Cache(httpCacheDirectory, 100 * 1024 * 1024);
//        OkHttpClient okHttpClient = new OkHttpClient();
//        if (cache != null) {
//            okHttpClient.setCache(cache);
//        }
        picasso = new Picasso.Builder(this)
                .build();
        picasso.setIndicatorsEnabled(true);
    }

//    /**
//     * 取得DaoMaster
//     *
//     * @param context
//     * @return
//     */
//    public static DaoMaster getDaoMaster(Context context) {
//        if (daoMaster == null) {
//            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(context, Constants.DB_NAME, null);
//            daoMaster = new DaoMaster(helper.getWritableDatabase());
//        }
//        return daoMaster;
//    }

//    /**
//     * 取得DaoSession
//     *
//     * @param context
//     * @return
//     */
//    public static DaoSession getDaoSession(Context context) {
//        if (daoSession == null) {
//            if (daoMaster == null) {
//                daoMaster = getDaoMaster(context);
//            }
//            daoSession = daoMaster.newSession();
//        }
//        return daoSession;
//    }


//    private void setupDatabase() {
//        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
//        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
//        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
//        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "omchat_db", null);
//        db = helper.getWritableDatabase();
//        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
//        daoMaster = new DaoMaster(db);
//        daoSession = daoMaster.newSession();
//    }
//
//    public static SQLiteDatabase getDatabase(){
//        return db;
//    }

//    private void setPicaso() {
//        // Prepare OkHttp
////        File httpCacheDirectory = new File(getCacheDir(), "photos");
//        picasso = new Picasso.Builder(this)
//                .build();
//        picasso.setIndicatorsEnabled(true);
//    }
//
//    public Picasso getPicasso() {
//        if (picasso == null) {
//            setPicaso();
//            LogUtil.i("picasso is null create it cache dir is", getCacheDir());
//        }
//        return picasso;
//
//    }
}
