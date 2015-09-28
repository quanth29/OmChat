package in.ohmama.omchat.model.service;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;
import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmComingMsg;
import in.ohmama.omchat.model.OmComingMsgDao;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmMessageDao;

/**
 * Created by yanglone on 9/19/15.
 */
public class MessageService extends BaseService<OmMessage, Long> {
    public MessageService(AbstractDao dao) {
        super(dao);
    }

    public List<OmMessage> loadUnreadMsg(String userName) {
        QueryBuilder<OmMessage> qb = getDao().queryBuilder();
        qb.where(OmMessageDao.Properties.UserName.eq(userName), OmMessageDao.Properties.IsRead.eq(false));
        return qb.list();
    }

    public List<OmMessage> loadMsg(String userName) {
        QueryBuilder<OmMessage> qb = getDao().queryBuilder();
        qb.where(OmMessageDao.Properties.UserName.eq(userName));
        return qb.list();
    }

    public List<OmMessage> loadLastestChatList() {
        String whereSql = "WHERE " + Constants.CL_USER_NAME + " IS NOT NULL GROUP BY " + Constants.CL_USER_NAME;
        return getDao().queryRaw(whereSql, null);
    }

    public void deleteUserMsg(String userName) {
        QueryBuilder<OmMessage> qb = getDao().queryBuilder();
        qb.where(OmMessageDao.Properties.UserName.eq(userName));
        DeleteQuery deleteQuery = qb.buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }


//    @Override
//    public long save(OmMessage msg){
////        if(msg.getToUserId().equals(OmApplication.userSelf.getId())){
////            msg.setTypeId(ChatType.TEXT_FROM.getValue());
////        }else{
////            msg.setTypeId(ChatType.TEXT_TO.getValue());
////        }
//        return getDao().insertOrReplace(msg);
//    }

}
