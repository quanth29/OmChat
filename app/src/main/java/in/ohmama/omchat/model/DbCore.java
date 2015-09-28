package in.ohmama.omchat.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import de.greenrobot.dao.query.QueryBuilder;
import in.ohmama.omchat.Constants;

/**
 * Created by Leon on 9/17/15.
 */
public class DbCore {

    private static final String DEFAULT_DB_NAME = Constants.DB_NAME;
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;

    private static Context mContext;
    private static String DB_NAME;
    private static SQLiteDatabase db;

    public static void init(Context context) {
        init(context, DEFAULT_DB_NAME);
    }

    public static void init(Context context, String dbName) {
        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }
        mContext = context.getApplicationContext();
        DB_NAME = dbName;
    }

    public static DaoMaster getDaoMaster() {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(mContext, DB_NAME, null);
            db = helper.getWritableDatabase();
            daoMaster = new DaoMaster(db);
        }
        return daoMaster;
    }

    public static DaoSession getDaoSession() {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster();
            }
            daoSession = daoMaster.newSession();
        }
        enableQueryBuilderLog();
        return daoSession;
    }

    public static void enableQueryBuilderLog(){
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    public static SQLiteDatabase getDb(){
        return db;
    };
}
