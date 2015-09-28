package in.ohmama.omchat.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.packet.RosterPacket;

import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmComingMsg;
import in.ohmama.omchat.model.OmMessage;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.model.type.FriendType;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NetLoadThread;
import in.ohmama.omchat.util.ToastUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class NewFriendActivity extends AppCompatActivity {

    private ListView lvFriendRequest;
    private TextView tvNodata;
    private FriendRequestAdapter friendRequestAdapter;
    //    private List<OmComingMsg> newMsgs;
//    private List<OmMessage> msgList;
    private List<OmUser> userList;
    private ComingMsgService comingMsgService;
    //    private MessageService messageService;
    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_friend);

        init();
    }

    private void init() {
        userService = DbUtil.getUserService();
        comingMsgService = DbUtil.getComingMessageService();
        userList = userService.loadFriendReqMsgFromComingMsg();

        lvFriendRequest = (ListView) findViewById(R.id.lv_friend_request);
        tvNodata = (TextView) findViewById(R.id.tv_nodata);
        if (userList.size() == 0) {
            switchView(true);
        } else {
            switchView(false);
            friendRequestAdapter = new FriendRequestAdapter();
            lvFriendRequest.setAdapter(friendRequestAdapter);
        }
    }

    private void switchView(boolean showNodata) {
        if (showNodata) {
            tvNodata.setVisibility(View.VISIBLE);
            lvFriendRequest.setVisibility(View.GONE);
        } else {
            tvNodata.setVisibility(View.GONE);
            lvFriendRequest.setVisibility(View.VISIBLE);
        }
    }


    class FriendRequestAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userList.size();
        }

        @Override
        public OmUser getItem(int position) {
            return userList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return userList.get(position).getId();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = getLayoutInflater().inflate(R.layout.friend_req_item, null);
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.btnAdd = (Button) convertView.findViewById(R.id.btn_add_friend);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final OmUser user = getItem(position);
            holder.tvName.setText(user.getUserName());
            if (FriendType.getType(user.getTypeId()) == RosterPacket.ItemType.to || FriendType.getType(user.getTypeId()) == RosterPacket.ItemType.none) {
                holder.btnAdd.setText("添加");
            }

            holder.btnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriend(user.getUserName(), position);
                }
            });

            return convertView;
        }
    }

    public void addFriend(final String userName, final int position) {
        new NetLoadThread(this) {
            @Override
            protected Object load() {
                if (XmppConnectHelper.getInstance().addUser(userName)) {
                    return XmppConnectHelper.getInstance().getUserRawInfo(userName);
                }
                ;
                return null;
            }

            @Override
            protected void result(Object object) {
                if (object != null) {
                    // update user relation
                    userService.updateUserType(userName, FriendType.getValue(RosterPacket.ItemType.both));
                    userService.saveOrUpdate((OmUser) object);
                    // delete coming msg
                    comingMsgService.deleteFriendRequest(userName);
                    Button v = (Button) lvFriendRequest.getChildAt(position).findViewById(R.id.btn_add_friend);
                    v.setText("添加成功");
                    v.setClickable(false);
                } else {
                    ToastUtil.toast("添加失败");
                }
            }
        };
    }

    class ViewHolder {
        TextView tvName;
        Button btnAdd;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_friend, menu);
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
}
