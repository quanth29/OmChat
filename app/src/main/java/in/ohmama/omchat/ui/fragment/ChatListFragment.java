package in.ohmama.omchat.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.ui.activity.ChattingActivity;
import in.ohmama.omchat.ui.adapter.ChatListAdapter;

public class ChatListFragment extends BaseFragemnt {


    private ListView lvChats;
    private TextView tvNoData;

    private ChatListAdapter chatAdatper;
    private List<OmMessage> conversationList = new ArrayList<>();


    private MessageService messageService;
    private ComingMsgService comingMsgService;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        initView(root);
        return root;

    }

    public void init() {
        messageService = DbUtil.getMessageService();
        chatAdatper = new ChatListAdapter(getActivity());
    }

    public void initView(View root) {
        tvNoData = (TextView) root.findViewById(R.id.tv_nodata);
        lvChats = (ListView) root.findViewById(R.id.lv_chat);
        lvChats.setAdapter(chatAdatper);
        lvChats.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent in = new Intent(getActivity(), ChattingActivity.class);
//                in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra(Constants.KEY_USER_NAME, conversationList.get(position).getUserName());
                startActivity(in);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadFragment();
    }

    @Override
    public void reloadFragment() {
        conversationList = messageService.loadLastestChatList();
        if (conversationList == null || conversationList.size() == 0) {
            lvChats.setVisibility(View.GONE);
            tvNoData.setVisibility(View.VISIBLE);
        } else {
            lvChats.setVisibility(View.VISIBLE);
            tvNoData.setVisibility(View.GONE);
        }
        chatAdatper.refreshAdapter(conversationList);
//        chatAdatper.setChatList(conversationList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    // test data
//    public void refreshData() {
//        testData();
//        chatAdatper.notifyDataSetChanged();
//    }
//
//    public void testData() {
//        for (int i = 0; i < 20; i++) {
//            OmMessage c = new OmMessage();
//            c.setTextMsg("nihaonihaonihaonihaonihaonihaonihaonihaonihaonihao" + i);
//            OmUser contact = new OmUser();
//            contact.setId(Long.valueOf(i));
//            contact.setNickName("San" + i);
//            conversationList.add(c);
//        }
//        chatAdatper.setChatList(conversationList);
//    }

}
