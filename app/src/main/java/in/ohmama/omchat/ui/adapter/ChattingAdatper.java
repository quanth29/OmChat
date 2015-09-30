package in.ohmama.omchat.ui.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.helper.VideoPlayHelper;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.type.MsgInOut;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by Leon on 9/11/15.
 */
public class ChattingAdatper extends BaseAdapter {

    private List<OmMessage> chatList = new ArrayList<>();
    private Context context;
    private MediaPlayer player;
    private VideoPlayHelper playerHelper;

    public ChattingAdatper(Context context) {
        this.context = context;
        playerHelper = new VideoPlayHelper(context);
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
        return chatList.get(position).hashCode();
    }

    /**
     * @param position
     * @return 可以返回任何数字，不同即可，不用该数字，但必须用到getItemType
     */
    @Override
    public int getItemViewType(int position) {
        OmMessage m = getItem(position);
        int tmp = m.getInOut() | m.getTypeId();
        switch (tmp) {
            case Constants.MSG_OUT | Constants.MSG_TYPE_TXT:
                return 0;
            case Constants.MSG_OUT | Constants.MSG_TYPE_SOUND:
                return 1;
            case Constants.MSG_OUT | Constants.MSG_TYPE_IMG:
                return 2;
            case Constants.MSG_OUT | Constants.MSG_TYPE_VIDEO:
                return 3;
            case Constants.MSG_IN | Constants.MSG_TYPE_TXT:
                return 4;
            case Constants.MSG_IN | Constants.MSG_TYPE_SOUND:
                return 5;
            case Constants.MSG_IN | Constants.MSG_TYPE_IMG:
                return 6;
            case Constants.MSG_IN | Constants.MSG_TYPE_VIDEO:
                return 7;
        }
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return Constants.MSG_TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChatHolder chatHolder = null;
        OmMessage conv = getItem(position);
        switch (conv.getInOut() | conv.getTypeId()) {
            case Constants.MSG_OUT | Constants.MSG_TYPE_TXT: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_text_to_item, null);
                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.chatText.setText(conv.getTextMsg());
                break;
            }
            case Constants.MSG_OUT | Constants.MSG_TYPE_SOUND: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_voice_to_item, null);
                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    chatHolder.tvIsReadRedDot = (TextView) convertView.findViewById(R.id.msg_voice_isread);
                    chatHolder.tvVoiceTime = (TextView) convertView.findViewById(R.id.msg_voice_time);
                    chatHolder.ivVoiceIcon = (ImageView) convertView.findViewById(R.id.msg_voice_icon);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.tvVoiceTime.setText(conv.getMediaDuration() + "\"");
                playSound(chatHolder.chatText, conv.getTextMsg());
                break;
            }
            case Constants.MSG_IN | Constants.MSG_TYPE_TXT: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_text_from_item, null);
                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.chatText.setText(conv.getTextMsg());
                break;
            }
            case Constants.MSG_IN | Constants.MSG_TYPE_SOUND: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_voice_from_item, null);
                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    chatHolder.tvIsReadRedDot = (TextView) convertView.findViewById(R.id.msg_voice_isread);
                    chatHolder.tvVoiceTime = (TextView) convertView.findViewById(R.id.msg_voice_time);
                    chatHolder.ivVoiceIcon = (ImageView) convertView.findViewById(R.id.msg_voice_icon);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.tvVoiceTime.setText(conv.getMediaDuration() + "\"");
                playSound(chatHolder.chatText, conv.getTextMsg());
                break;
            }
            case Constants.MSG_IN | Constants.MSG_TYPE_VIDEO: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_video_from, null);
//                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    chatHolder.videoContainer = (FrameLayout) convertView.findViewById(R.id.chat_video_container);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.videoContainer.addView(playerHelper.getVideoPreview(conv.getTextMsg()));
                break;
            }
            case Constants.MSG_OUT | Constants.MSG_TYPE_VIDEO: {
                if (convertView == null) {
                    chatHolder = new ChatHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.chat_video_to, null);
//                    chatHolder.chatText = (TextView) convertView.findViewById(R.id.chatText);
                    chatHolder.videoContainer = (FrameLayout) convertView.findViewById(R.id.chat_video_container);
                    convertView.setTag(chatHolder);
                } else {
                    chatHolder = (ChatHolder) convertView.getTag();
                }
                chatHolder.videoContainer.addView(playerHelper.getVideoPreview(conv.getTextMsg()));
                break;
            }
        }

        return convertView;
    }

    int sessionId;

    public void playSound(View soundBubble, final String filePath) {

        soundBubble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (player != null) {
                    // 如果在播则 停止
                    if (player.isPlaying()) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                }

                if (player == null) {
                    player = new MediaPlayer();
                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            player.stop();
                            player.release();
                            player = null;
                        }
                    });
                }
                if (sessionId != player.getAudioSessionId()) {
                    try {
//                        playingView = v;
                        sessionId = player.getAudioSessionId();
                        player.setDataSource(filePath);
                        player.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    player.start();
                } else {
                    player.reset();
                    player.start();
                }
            }
        });
    }

    public void setChatList(List<OmMessage> chatList) {
        this.chatList = chatList;
    }

    class ChatHolder {
        TextView chatText;
        TextView tvIsReadRedDot; // 红点
        TextView tvVoiceTime; // 语音时长
        ImageView ivVoiceIcon; // 语音Icon
        FrameLayout videoContainer;
    }
}
