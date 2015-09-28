package in.ohmama.omchat.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import in.ohmama.omchat.model.OmMessage;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "OM_MESSAGE".
*/
public class OmMessageDao extends AbstractDao<OmMessage, Long> {

    public static final String TABLENAME = "OM_MESSAGE";

    /**
     * Properties of entity OmMessage.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property TypeId = new Property(1, Integer.class, "typeId", false, "TYPE_ID");
        public final static Property TextMsg = new Property(2, String.class, "textMsg", false, "TEXT_MSG");
        public final static Property UserName = new Property(3, String.class, "userName", false, "USER_NAME");
        public final static Property Time = new Property(4, java.util.Date.class, "time", false, "TIME");
        public final static Property InOut = new Property(5, Integer.class, "inOut", false, "IN_OUT");
        public final static Property MediaDuration = new Property(6, Integer.class, "mediaDuration", false, "MEDIA_DURATION");
        public final static Property IsRead = new Property(7, Boolean.class, "isRead", false, "IS_READ");
    };


    public OmMessageDao(DaoConfig config) {
        super(config);
    }
    
    public OmMessageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"OM_MESSAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TYPE_ID\" INTEGER," + // 1: typeId
                "\"TEXT_MSG\" TEXT," + // 2: textMsg
                "\"USER_NAME\" TEXT," + // 3: userName
                "\"TIME\" INTEGER," + // 4: time
                "\"IN_OUT\" INTEGER," + // 5: inOut
                "\"MEDIA_DURATION\" INTEGER," + // 6: mediaDuration
                "\"IS_READ\" INTEGER);"); // 7: isRead
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"OM_MESSAGE\"";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, OmMessage entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        Integer typeId = entity.getTypeId();
        if (typeId != null) {
            stmt.bindLong(2, typeId);
        }
 
        String textMsg = entity.getTextMsg();
        if (textMsg != null) {
            stmt.bindString(3, textMsg);
        }
 
        String userName = entity.getUserName();
        if (userName != null) {
            stmt.bindString(4, userName);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(5, time.getTime());
        }
 
        Integer inOut = entity.getInOut();
        if (inOut != null) {
            stmt.bindLong(6, inOut);
        }
 
        Integer mediaDuration = entity.getMediaDuration();
        if (mediaDuration != null) {
            stmt.bindLong(7, mediaDuration);
        }
 
        Boolean isRead = entity.getIsRead();
        if (isRead != null) {
            stmt.bindLong(8, isRead ? 1L: 0L);
        }
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public OmMessage readEntity(Cursor cursor, int offset) {
        OmMessage entity = new OmMessage( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1), // typeId
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // textMsg
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // userName
            cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)), // time
            cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5), // inOut
            cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6), // mediaDuration
            cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0 // isRead
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, OmMessage entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTypeId(cursor.isNull(offset + 1) ? null : cursor.getInt(offset + 1));
        entity.setTextMsg(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setUserName(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setTime(cursor.isNull(offset + 4) ? null : new java.util.Date(cursor.getLong(offset + 4)));
        entity.setInOut(cursor.isNull(offset + 5) ? null : cursor.getInt(offset + 5));
        entity.setMediaDuration(cursor.isNull(offset + 6) ? null : cursor.getInt(offset + 6));
        entity.setIsRead(cursor.isNull(offset + 7) ? null : cursor.getShort(offset + 7) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(OmMessage entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(OmMessage entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}
