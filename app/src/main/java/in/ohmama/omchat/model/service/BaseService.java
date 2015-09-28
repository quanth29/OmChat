package in.ohmama.omchat.model.service;

import java.util.List;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.query.QueryBuilder;

/**
 * Created by Leon on 9/17/15.
 */
public class BaseService<T, K> {

    private AbstractDao<T,K> mDao;


    public BaseService(AbstractDao dao) {
        mDao = dao;
    }


    public long save(T item) {
        return mDao.insertOrReplace(item);
    }

    public void save(T... items) {
        mDao.insertInTx(items);
    }

    public void save(List items) {
        mDao.insertInTx(items);
    }

    public void saveOrUpdate(T item) {
        mDao.insertOrReplace(item);
    }

    public void saveOrUpdate(T... items) {
        mDao.insertOrReplaceInTx(items);
    }

    public void deleteByKey(K key) {
        mDao.deleteByKey(key);
    }

    public void delete(T item) {
        mDao.delete(item);
    }

    public void delete(T... items) {
        mDao.deleteInTx(items);
    }

    public void delete(List items) {
        mDao.deleteInTx(items);
    }

    public void deleteAll() {
        mDao.deleteAll();
    }


    public void update(T item) {
        mDao.update(item);
    }

    public void update(T... items) {
        mDao.updateInTx(items);
    }

    public void update(List items) {
        mDao.updateInTx(items);
    }

    public T query(K key) {
        return mDao.load(key);
    }

    public List queryAll() {
        return mDao.loadAll();
    }

    public List query(String whereColume, String... params) {
        String sqlWhere = " where " + whereColume + "=? ";
        return mDao.queryRaw(sqlWhere, params);
    }

    public QueryBuilder queryBuilder() {
        return mDao.queryBuilder();
    }

    public long count() {
        return mDao.count();
    }

    public void refresh(T item) {
        mDao.refresh(item);

    }

    public void detach(T item) {
        mDao.detach(item);
    }

    public AbstractDao<T,K> getDao(){
        return mDao;
    }

}
