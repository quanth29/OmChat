package in.ohmama.omchat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import in.ohmama.omchat.R;
import in.ohmama.omchat.helper.BitmapLoadHelper;
import in.ohmama.omchat.helper2.ImageFetcher;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.OmUserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据适配器类
 * 用于进入主界面的时候显示所有好友列表
 *
 * @author 廖乃波 & Leon
 */
public class RosterAdapter extends BaseAdapter {
    private Context context;
    private List<OmUser> userinfos = new ArrayList<>();

    private AvatorLoadListner avatorLoadListner;
    private BitmapLoadHelper bitmapLoadHelper;
    private ImageFetcher mImageFetcher;

    public RosterAdapter(Context context,List<OmUser> users, ImageFetcher imageFetcher) {
        super();
        this.context = context;
        userinfos = users;
//        bitmapLoadHelper = new BitmapLoadHelper(context);
        mImageFetcher = imageFetcher;

    }

    @Override
    public int getCount() {
        return userinfos.size();
    }

    @Override
    public Object getItem(int position) {
        return userinfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        OmUser user = userinfos.get(position);
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.roster_list_item, null);
            holder.user = (TextView) convertView.findViewById(R.id.tv_name);
            holder.status = (TextView) convertView.findViewById(R.id.tv_status);
            holder.avator = (ImageView) convertView.findViewById(R.id.iv_friend_list_avator);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.user.setText(user.getUserName());
        holder.status.setText(user.getStatus());
//        bitmapLoadHelper.loadBitmap(user.getUserName(), holder.avator);
        mImageFetcher.loadImage(user.getUserName(), holder.avator);

//        LogUtil.i("avator url", avatorLoadListner.userInfoLoad(user.getUserName(),holder.avator));
        return convertView;
    }

    public void setUserinfos(List<OmUser> mlist){
        userinfos = mlist;
    }

    public void setAvatorLoadListner(AvatorLoadListner avatorLoadListner) {
        this.avatorLoadListner = avatorLoadListner;
    }

    private class ViewHolder {
        TextView user;
        TextView status;
        ImageView avator;
    }

    public interface AvatorLoadListner {
        /**
         * load user avator url String
         *
         * @return
         */
        String userInfoLoad(String userName, ImageView ivAvator);

    }
}
