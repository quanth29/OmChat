package in.ohmama.omchat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.ui.view.RecordButton;
import in.ohmama.omchat.util.FileUtil;
import in.ohmama.omchat.util.ImageUtil;
import in.ohmama.omchat.util.NotificationUtil;
import in.ohmama.omchat.util.ToastUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;
import in.ohmama.omchat.ui.adapter.ChattingAdatper;

public class ChattingActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView lvChats;
    private ImageView ivSetModeKeyboard;
    private ImageView ivSetModeVoice;
    private ImageView ivSetTypeMode;
    private EditText etContent;
    private Button btnSend;
    private RecordButton btnRecordVoice;

    private ChattingAdatper chattingAdapter;
    private List<OmMessage> chattingList = new ArrayList<OmMessage>();
    private InputMethodManager imm;

    private String userName;

    private MessageService messageService;
    private ComingMsgService comingMsgService;
    private MsgReceiver msgReceiver;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        userName = getIntent().getStringExtra(Constants.KEY_USER_NAME);

        init();
        NotificationUtil.clearNoti(Constants.NOTIFY_TYPE_MSG);
//        refreshData();
    }

    private void init() {
        // system service
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        lvChats = (ListView) findViewById(R.id.lv_chat);
        chattingAdapter = new ChattingAdatper(this);
        lvChats.setAdapter(chattingAdapter);

        ivSetModeKeyboard = (ImageView) findViewById(R.id.chatting_setmode_keyboard);
        ivSetModeVoice = (ImageView) findViewById(R.id.chatting_setmode_voice);
        ivSetTypeMode = (ImageView) findViewById(R.id.chatting_type_mode);
        etContent = (EditText) findViewById(R.id.chatting_content);
        btnSend = (Button) findViewById(R.id.btn_msg_send);
        btnRecordVoice = (RecordButton) findViewById(R.id.chatting_press_to_voice);
        ivSetModeKeyboard.setOnClickListener(this);
        ivSetModeVoice.setOnClickListener(this);
        ivSetTypeMode.setOnClickListener(this);
        etContent.setOnClickListener(this);
        btnRecordVoice.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        // event

        btnRecordVoice.setOnFinishedRecordListener(new RecordButton.OnFinishedRecordListener() {
            @Override
            public void onFinishedRecord(String audioPath, int time) {
                if (audioPath != null) {
                    try {
                        XmppConnectHelper.getInstance().sendMsgWithParms(FileUtil.parseUrlTofileName(audioPath),
                                new String[]{Constants.KEY_PROPERTY_MEDIA, Constants.KEY_PROPERTY_TIME_DURATION},
                                new Object[]{ImageUtil.getBase64StringFromFile(audioPath), time});
                    } catch (Exception e) {
//                        autoSendIfFail(FileUtil.getFileName(audioPath), new String[]{"imgData"}, new Object[]{ImageUtil.getBase64StringFromFile(audioPath)});
                        e.printStackTrace();
                    }
                } else {
                    ToastUtil.toast("发送失败");
                }

            }
        });
        etContent.addTextChangedListener(textWatcher);

        messageService = DbUtil.getMessageService();
        comingMsgService = DbUtil.getComingMessageService();

        // 创建会话
        XmppConnectHelper.getInstance().setRecevier(userName);
        loadDataFromDb();
        clearComingMsg();
    }

    /**
     * 删除未读会话
     */
    private void clearComingMsg() {
        comingMsgService.deleteComingMsg(userName);
    }


    public void loadDataFromDb() {
        chattingList = messageService.loadMsg(userName);
        chattingAdapter.setChatList(chattingList);
        chattingAdapter.notifyDataSetChanged();
        setMsgReaded(chattingList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chatting_setmode_keyboard:
                toggleChatMode(false);
                break;
            case R.id.chatting_setmode_voice:
                toggleChatMode(true);
                break;
            case R.id.btn_msg_send: // send msg
                String msgText = etContent.getText().toString();
                if (msgText.length() > 0) {
                    etContent.setText("");
//                Message msg = new Message(userName, Message.Type.chat);
//                msg.setBody(msgText);
                    try {
                        XmppConnectHelper.getInstance().sendMsg(userName, msgText);
                    } catch (Exception e) {
                        // connect fail
                        e.printStackTrace();
                    }

                }
                break;
        }
    }

    /**
     * click buttom to sent message
     */
//    private void sendTxtMsg(String msg) {
//        OmMessage c = new OmMessage();
//        c.setTypeId(ChatType.TEXT_TO.getValue());
//        c.setTextMsg(msg);
//        c.setInOut(MsgInOut.MSG_OUT);
//        c.setIsRead(true);
//        c.setTime(new Date());
//        chattingList.add(c);
//        chattingAdapter.setChatList(chattingList);
//        chattingAdapter.notifyDataSetChanged();
//
//        // save to db
////        long index = messageService.save(c);
//        long index = messageService.save(c);
//    }

    // toggle between voice and text msg mode
    private void toggleChatMode(boolean toVoice) {
        if (toVoice) {
            ivSetModeKeyboard.setVisibility(View.VISIBLE);
            ivSetModeVoice.setVisibility(View.GONE);
            btnRecordVoice.setVisibility(View.VISIBLE);
            etContent.setVisibility(View.GONE);
            etContent.clearFocus();
            imm.hideSoftInputFromWindow(etContent.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
            ivSetModeKeyboard.setVisibility(View.GONE);
            ivSetModeVoice.setVisibility(View.VISIBLE);
            btnRecordVoice.setVisibility(View.GONE);
            etContent.setVisibility(View.VISIBLE);
            etContent.requestFocus();
            imm.showSoftInput(etContent, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    // toggle between "send msg button"
    private void toggleHasText(boolean hasText) {
        if (hasText) {
            btnSend.setVisibility(View.VISIBLE);
            ivSetTypeMode.setVisibility(View.GONE);
        } else {
            btnSend.setVisibility(View.GONE);
            ivSetTypeMode.setVisibility(View.VISIBLE);
        }
    }

    // msg content change listner
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count > 0) {
                toggleHasText(true);
            } else {
                toggleChatMode(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (msgReceiver == null) msgReceiver = new MsgReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_MSG_REV);
        intentFilter.addAction(Constants.ACTION_MSG_SENT);
        registerReceiver(msgReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (msgReceiver != null) unregisterReceiver(msgReceiver);
    }

    private class MsgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACTION_MSG_REV:
                case Constants.ACTION_MSG_SENT:
                    List<OmMessage> msgList = messageService.loadUnreadMsg(userName);
                    chattingList.addAll(msgList);
                    chattingAdapter.notifyDataSetChanged();

                    setMsgReaded(msgList);
                    break;


            }
            // recevice msg
            if (intent.getAction().equals(Constants.ACTION_MSG_REV)) {


                // Do stuff - maybe update my view based on the changed DB contents
            }
        }
    }

    // 设为已读
    private void setMsgReaded(List<OmMessage> msgList) {
        for (OmMessage m : msgList) {
            if (!m.getIsRead()) {
                m.setIsRead(true);
                messageService.save(m);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // testdata
//    public void refreshData() {
//        testData();
//        chattingAdapter.notifyDataSetChanged();
//    }
//
//    public void testData() {
//        for (int i = 0; i < 20; i++) {
//            OmMessage c = new OmMessage();
//            c.setId(Long.valueOf(i));
//            if (i % 2 == 0)
//                c.setTypeId(ChatType.TEXT_FROM.getValue());
//            else
//                c.setTypeId(ChatType.TEXT_TO.getValue());
//            c.setTextMsg("nihaonihaonihaonihaonihaonihaonihaonihaonihaonihao" + i);
//            chattingList.add(c);
//        }
//        chattingAdapter.setChatList(chattingList);
//    }

}
