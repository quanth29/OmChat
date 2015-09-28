package in.ohmama.omchat.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmComingMsg;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.util.DateUtil;
import in.ohmama.omchat.util.FileUtil;

/**
 * Created by Leon on 9/11/15.
 */
public class ChatListAdapter extends BaseAdapter {

    Context mContext;
    List<OmMessage> chatList = new ArrayList<>();
    ComingMsgService comingMsgService;

    public void setChatList(List<OmMessage> chatList) {
        this.chatList = chatList;
    }

    public ChatListAdapter(Context mContext) {
        this.mContext = mContext;
        comingMsgService = DbUtil.getComingMessageService();
    }

    @Override
    public int getCount() {
        return chatList.size();
    }

    @Override
    public OmMessage getItem(int position) {
        return chatList.get(position);
    }

    @Override
    public long getItemId(int position) {
        Long id = chatList.get(position).getId();
        if (id == null)
            return chatList.get(position).hashCode();
        return id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatListHolder holder = null;
        OmMessage chat = chatList.get(position);
//
        if (convertView == null) {
            holder = new ChatListHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item, null);
            holder.nickName = (TextView) convertView.findViewById(R.id.chatlist_item_nick_name);
            holder.chatString = (TextView) convertView.findViewById(R.id.chatlist_item_content);
            holder.time = (TextView) convertView.findViewById(R.id.chatlist_item_time);
            holder.msgCount = (TextView) convertView.findViewById(R.id.msg_count);
            convertView.setTag(holder);
        } else {
            holder = (ChatListHolder) convertView.getTag();
        }
        holder.nickName.setText(chat.getUserName());
        String textMsg = chat.getTextMsg();
        int type = FileUtil.getType(textMsg);
        switch (type){
            case FileUtil.SOUND:
                textMsg = Constants.FILE_TYPE_AUDIO;
                break;
        }
        holder.chatString.setText(textMsg);
        holder.msgCount.setText("");
        holder.msgCount.setBackgroundDrawable(null);
        if(chat.getUserName()!=null){
            OmComingMsg comingMsg = comingMsgService.queryComingMsg(chat.getUserName());
            if (comingMsg != null) {
                int count = comingMsg.getCount();
                if (count > 0) {
                    holder.msgCount.setText(count + "");
                    holder.msgCount.setBackgroundResource(R.drawable.red_dot);
                }
            }
        }
        holder.time.setText(DateUtil.dateToStr_yyyy_MM_dd_HH_mm(chat.getTime()));
        return convertView;
    }

    public void refreshAdapter(List<OmMessage> newChatList){
        chatList.clear();
        notifyDataSetChanged();
        chatList.addAll(newChatList);
        notifyDataSetChanged();
    }

    class ChatListHolder {
        TextView nickName;
        TextView chatString;
        TextView time;
        TextView msgCount;
    }
}
