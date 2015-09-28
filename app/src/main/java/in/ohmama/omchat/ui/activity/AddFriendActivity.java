package in.ohmama.omchat.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NetLoadThread;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class AddFriendActivity extends BaseActivity {

    private EditText etFindFriendTxt;
    private Button btnAddFriend;
    private ListView lvFriends;
    private ArrayAdapter<String> friendAdapter;
    private List<String> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
        init();
    }

    public void init() {
        etFindFriendTxt = (EditText) findViewById(R.id.et_search_text);
        btnAddFriend = (Button) findViewById(R.id.btn_find_friend);
        btnAddFriend.setOnClickListener(this);
        lvFriends = (ListView) findViewById(R.id.lv_friend_list);

        friendAdapter = new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, datas);
        lvFriends.setAdapter(friendAdapter);
        lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent showUser = new Intent(AddFriendActivity.this, UserInfoActivity.class);
                LogUtil.i("add activity user name",datas.get(position));
                showUser.putExtra(Constants.KEY_USER_NAME, datas.get(position));
                startActivity(showUser);
            }
        });
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.btn_find_friend:
                String frientStr = etFindFriendTxt.getText() + "";
                if (frientStr.trim().length() == 0) {
                    etFindFriendTxt.setError("请输入信息");
                    return;
                }
                search(frientStr);
                break;
        }
    }

    public void search(final String userName) {
        new NetLoadThread(this) {
            @Override
            protected Object load() {
                datas = XmppConnectHelper.getInstance().searchUser(userName);
                return datas;
            }

            @Override
            protected void result(Object object) {
                friendAdapter.addAll(datas);
                friendAdapter.notifyDataSetChanged();
            }
        };
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_friend, menu);
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
