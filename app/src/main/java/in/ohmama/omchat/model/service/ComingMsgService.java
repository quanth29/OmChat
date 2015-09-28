package in.ohmama.omchat.model.service;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.DeleteQuery;
import de.greenrobot.dao.query.QueryBuilder;
import in.ohmama.omchat.Constants;
import in.ohmama.omchat.model.OmComingMsg;
import in.ohmama.omchat.model.OmComingMsgDao;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by yanglone on 9/19/15.
 */
public class ComingMsgService extends BaseService<OmComingMsg, Long> {

    public ComingMsgService(AbstractDao dao) {
        super(dao);
    }


    public void saveComingMsg(String userName) {
        OmComingMsg msg = queryComingMsg(userName);
        int count = 0;
        // have data
        if (msg != null) {
            count = msg.getCount() + 1;
        } else { // no data
            msg = new OmComingMsg();
            count = 1;
        }
        msg.setUserName(userName);
        msg.setCount(count);
        msg.setTypeId(Constants.TYPE_MSG);
        getDao().insertOrReplace(msg);
    }

    // 保存好友请求
    public void saveFriendRequest(String userName) {
        OmComingMsg msg = new OmComingMsg();
        msg.setUserName(userName);
        msg.setTypeId(Constants.TYPE_FRIEND_REQ);
        getDao().insertOrReplace(msg);
    }

    // 获取新信息
    public OmComingMsg queryComingMsg(String userName) {
        return queryMsg(userName, 0);
    }

    // 获取好友请求
    public OmComingMsg queryFriendRequest(String userName) {
        return queryMsg(userName, Constants.TYPE_FRIEND_REQ);
    }

    // 删除信息
    public void deleteComingMsg(String name) {
        deleteMsg(name, Constants.TYPE_MSG);
    }

    // 删除好友请求
    public void deleteFriendRequest(String name) {
        deleteMsg(name, Constants.TYPE_FRIEND_REQ);
    }

    public void deleteMsg(String name, int typeId) {
        QueryBuilder<OmComingMsg> qb = getDao().queryBuilder();
        qb.where(OmComingMsgDao.Properties.UserName.eq(name), OmComingMsgDao.Properties.TypeId.eq(typeId));
        DeleteQuery deleteQuery = qb.buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }

    /**
     * 获取消息
     *
     * @param userName
     * @param typeId
     * @return
     */
    public OmComingMsg queryMsg(String userName, int typeId) {
        QueryBuilder<OmComingMsg> qb = getDao().queryBuilder();
        qb.where(OmComingMsgDao.Properties.UserName.eq(userName));
        qb.where(OmComingMsgDao.Properties.TypeId.eq(typeId));
        qb.limit(1);
        OmComingMsg msg = qb.unique();
        return msg;
    }

    /**
     * 所有未读消息
     *
     * @return
     */
    public int loadAllUnreadMsg() {
        QueryBuilder<OmComingMsg> qb = getDao().queryBuilder();
        qb.where(OmComingMsgDao.Properties.TypeId.eq(Constants.TYPE_MSG));
        List<OmComingMsg> msgs = qb.list();
        int count = 0;
        for (OmComingMsg m : msgs) {
            count += m.getCount();
        }
        return count;
    }

    /**
     * 获取所有好友请求
     *
     * @return
     */
    public int loadAllFriendReq() {
        QueryBuilder<OmComingMsg> qb = getDao().queryBuilder();
        qb.where(OmComingMsgDao.Properties.TypeId.eq(Constants.TYPE_FRIEND_REQ));
        return (int) qb.count();
    }
}
