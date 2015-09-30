package in.ohmama.omchat.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.List;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.debug.DatabaseShowActivity;
import in.ohmama.omchat.helper.RecorderHelper;
import in.ohmama.omchat.helper2.ImageFetcher;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.ui.OnFragmentChangeListener;
import in.ohmama.omchat.ui.adapter.MainAdapter;
import in.ohmama.omchat.ui.adapter.RosterAdapter;
import in.ohmama.omchat.ui.fragment.BaseFragemnt;
import in.ohmama.omchat.ui.view.NestRadioGroup;
import in.ohmama.omchat.ui.view.OmLongClickButton;
import in.ohmama.omchat.ui.view.OmBounceScrollView;
import in.ohmama.omchat.ui.view.scrollview.OmSeperateScrollViewHelper;
import in.ohmama.omchat.util.FileUtil;
import in.ohmama.omchat.util.ImageUtil;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NotificationUtil;
import in.ohmama.omchat.util.ToastUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class MainActivity extends BaseActivity implements OnFragmentChangeListener, NestRadioGroup.OnCheckedChangeListener {

    private ViewPager mPager;
    private OmBounceScrollView omBounceScrollView;
    private View cancelCapture;
    private OmLongClickButton longClickButton;
    private FrameLayout videoViewContainer;// 取景
    private LinearLayout videoShootContainer;// 拍摄层
    private FrameLayout videoReplayContainer;// 视频回放层

    private LinearLayout bottomTabsContainer;
    private Toolbar mToolbar;

    private NestRadioGroup rg;
    private RadioButton rbChatList;
    private RadioButton rbRoster;
    private RadioButton rbExplore;
    private RadioButton rbMe;
    private TextView tvChatList;
    private TextView tvRoster;
    private TextView tvExplore;
    private TextView tvMe;
    private ImageView ivChatList;
    private ImageView ivRoster;
    private ImageView ivExplore;
    private ImageView ivMe;

    private MainAdapter mPagerAdapter;
    private RecorderHelper videoRecordHelper;
    private OmSeperateScrollViewHelper scrollViewHelper;

    private MessageService messageService;
    private UserService userService;
    private ComingMsgService comingMsgService;
    private MainReceiver mainReceiver;

    // is recording the video
    private boolean isRecording = false;
    private boolean isVideoMode = false;// 正在打开录视频中
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        initEvent();
    }

    public void init() {
        longClickButton = (OmLongClickButton) findViewById(R.id.btn_press_to_capture);
        mPager = (ViewPager) findViewById(R.id.viewpage_infos);
        cancelCapture = findViewById(R.id.cancelCapture);
        omBounceScrollView = (OmBounceScrollView) findViewById(R.id.omScrollView);
        videoViewContainer = (FrameLayout) findViewById(R.id.videoViewContainer);
        bottomTabsContainer = (LinearLayout) findViewById(R.id.bottomTabsContainer);
        mToolbar = (Toolbar) findViewById(R.id.mToolbar);
        rg = (NestRadioGroup) findViewById(R.id.bottomTabsContainer);
        rbChatList = (RadioButton) findViewById(R.id.rbChatingList);
        rbRoster = (RadioButton) findViewById(R.id.rbRoster);
        rbExplore = (RadioButton) findViewById(R.id.rbExpore);
        rbMe = (RadioButton) findViewById(R.id.rbMe);
        tvChatList = (TextView) findViewById(R.id.tvChatList);
        tvRoster = (TextView) findViewById(R.id.tvRoster);
        tvExplore = (TextView) findViewById(R.id.tvExplore);
        tvMe = (TextView) findViewById(R.id.tvMe);
        ivChatList = (ImageView) findViewById(R.id.ivChatList);
        ivRoster = (ImageView) findViewById(R.id.ivRoster);
        ivExplore = (ImageView) findViewById(R.id.ivExplore);
        ivMe = (ImageView) findViewById(R.id.ivMe);
        videoShootContainer = (LinearLayout) findViewById(R.id.videoShootContainer);
        videoReplayContainer = (FrameLayout) findViewById(R.id.videoReplayContainer);

        setToolbarTitle(0);
        setSupportActionBar(mToolbar);

        messageService = DbUtil.getMessageService();
        comingMsgService = DbUtil.getComingMessageService();
        userService = DbUtil.getUserService();

        mPagerAdapter = new MainAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        int toFragment = getIntent().getIntExtra(Constants.KEY_TO_FRAGMENT, 0);
        if (toFragment > 0) {
            mPager.setCurrentItem(toFragment);
            NotificationUtil.clearNoti(Constants.NOTIFY_TYPE_FRIEND);
        }
    }

    public void initEvent() {
        rg.setOnCheckedChangeListener(this);
        rbRoster.setOnClickListener(this);
        rbChatList.setOnClickListener(this);
        rbExplore.setOnClickListener(this);
        rbMe.setOnClickListener(this);

        // pull down to open capture video
        omBounceScrollView.setScrollViewActiveListener(new OmBounceScrollView.ScrollViewActiveListener() {

            @Override
            public void onFinishPull() {
                videoRecordHelper = new RecorderHelper(MainActivity.this);
                videoViewContainer.addView(videoRecordHelper.getCameraPreview());
                // hide the bottom tabs
                bottomTabsContainer.setVisibility(View.INVISIBLE);
                omBounceScrollView.setVisibility(View.INVISIBLE);
                isVideoMode = true;
            }

            @Override
            public void onPullCrossOver(boolean isCrossed) {
                LogUtil.i("isCrossed", isCrossed);
            }
        });

        // press to record video
        longClickButton.setLongclickListner(new OmLongClickButton.OmLongclickListner() {
            @Override
            public void onRepeat(int elapseTime) {
            }

            @Override
            public void notReachMinimum() {
                // not reach the minimum time, cancel record
                stopRecord();
            }

            @Override
            public void start() {
                if (!isRecording) {
                    new MediaPrepareTask().execute();
                }
            }

            @Override
            public void stop(int elapseTime) {
                // 停止播放
                stopRecord();
                // 打开通讯录
                replayVideoAndShowContact();
            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                tabSelect(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        // bounce back view
        cancelCapture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                bounceBackAndReleaseRes();
            }
        });
    }

    private void tabSelect(int index) {
        int colorGreen = getResources().getColor(R.color.green_dark);
        int colorDefault = getResources().getColor(R.color.primary_dark_material_light);

        ivChatList.setColorFilter(null);
        ivRoster.setColorFilter(null);
        ivExplore.setColorFilter(null);
        ivMe.setColorFilter(null);
        tvChatList.setTextColor(colorDefault);
        tvExplore.setTextColor(colorDefault);
        tvRoster.setTextColor(colorDefault);
        tvMe.setTextColor(colorDefault);

        switch (index) {
            case 0:
                tvChatList.setTextColor(colorGreen);
                ivChatList.setColorFilter(colorGreen);
                break;
            case 1:
                tvRoster.setTextColor(colorGreen);
                ivRoster.setColorFilter(colorGreen);
                break;
            case 2:
                tvExplore.setTextColor(colorGreen);
                ivExplore.setColorFilter(colorGreen);
                break;
            case 3:
                tvMe.setTextColor(colorGreen);
                ivMe.setColorFilter(colorGreen);
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 正在拍摄层，则执行退出
        if (isVideoMode) {
            stopRecord();
            bounceBackAndReleaseRes();
        } else {
            if (KeyEvent.KEYCODE_BACK == keyCode) {
                if (System.currentTimeMillis() - exitTime > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    XmppConnectHelper.getInstance().closeConnection();
                    finish();
                    System.exit(0);
                }
            } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
            } else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                        AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        bounceBackAndReleaseRes();
        if (mainReceiver != null) unregisterReceiver(mainReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mainReceiver == null) mainReceiver = new MainReceiver();
        IntentFilter intentFilter = new IntentFilter(Constants.ACTION_MSG_REV);
        intentFilter.addAction(Constants.ACTION_FRIEND_ADDED);
        intentFilter.addAction(Constants.ACTION_FRIEND_REQUEST);
        registerReceiver(mainReceiver, intentFilter);

        changeBottomBarNum(0);
        changeBottomBarNum(1);
    }

    /**
     * 设置toolbar标题
     *
     * @param count 未读数
     */
    public void setToolbarTitle(int count) {
        String title = "omCHAT";
        if (count > 0) {
            title = title + "(" + count + ")";
        }
        mToolbar.setTitle(title);
    }

    //
    // RECORD VIDEO
    // BEGIN
    //
    private void stopRecord() {
        videoRecordHelper.cameraStop(); // release the MediaRecorder object
        videoViewContainer.removeAllViews(); // 去掉视频框
        videoReplayContainer.removeAllViews();// 去掉回放层
    }

    // 回放小视频，并在底下加上通讯录
    private void replayVideoAndShowContact() {
        scrollViewHelper = new OmSeperateScrollViewHelper()
                .contentLayout(R.layout.scroll_contact_bottom);
        scrollViewHelper.setmHeaderView(videoRecordHelper.getVideoPreview());// 播放录好的视频
        View replayRootView = scrollViewHelper.createView(getLayoutInflater());
        videoReplayContainer.addView(replayRootView);

        // 通讯录
        ListView lvContact = (ListView) replayRootView.findViewById(R.id.lv_contact);
        final List<OmUser> userDatas = userService.loadFriend();
        int mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        ImageFetcher mImageFetcher = new ImageFetcher(this, mImageThumbSize);
        RosterAdapter rosterAdapter = new RosterAdapter(this, userDatas, mImageFetcher);
        lvContact.setAdapter(rosterAdapter);

        // 发送视频
        final String path = videoRecordHelper.path;
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    // 发送对象
                    XmppConnectHelper.getInstance().setRecevier(userDatas.get(position).getUserName());
                    // 发送资源
                    XmppConnectHelper.getInstance().sendMsgWithParms(FileUtil.parseUrlTofileName(path),
                            new String[]{Constants.KEY_PROPERTY_MEDIA, Constants.KEY_PROPERTY_TIME_DURATION},
                            new Object[]{ImageUtil.getBase64StringFromFile(path), videoRecordHelper.videoDuration});
                    bounceBackAndReleaseRes();
                } catch (Exception e) {
                    ToastUtil.toast("发送视频出错:"+e.getMessage());
                }

            }
        });
    }

//    private void clearReplayViews() {
//        videoReplayContainer.removeAllViews();
//    }

    private void bounceBackAndReleaseRes() {
        isVideoMode = false;
        if (videoRecordHelper != null) {
            videoRecordHelper.cameraStop();
            omBounceScrollView.bounceBack();
            omBounceScrollView.setVisibility(View.VISIBLE);
            videoRecordHelper.releaseCameraRes();
            videoRecordHelper.releasePlayer();
            videoViewContainer.removeAllViews();
            bottomTabsContainer.setVisibility(View.VISIBLE);

        }
    }

    /**
     * when sub fragment has new notice
     * change the main view's bottom tab
     *
     * @param pageNum
     */
    @Override
    public void changeBottomBarNum(int pageNum) {
        if (pageNum == 0) {
            // 新消息数目
            int count = comingMsgService.loadAllUnreadMsg();
            TextView tvNewMsgCount = (TextView) findViewById(R.id.dot_num_new_msg);
            if (count > 0) {
                tvNewMsgCount.setText(count + "");
                tvNewMsgCount.setBackgroundResource(R.drawable.red_dot);
                setToolbarTitle(count);
            } else {
                tvNewMsgCount.setText("");
                tvNewMsgCount.setBackgroundDrawable(null);
            }
        } else if (pageNum == 1) {
            TextView tvNewFriendReqCount = (TextView) findViewById(R.id.dot_num_new_friend);
            int count = comingMsgService.loadAllFriendReq();
            if (count > 0) {
                tvNewFriendReqCount.setText(count + "");
                tvNewFriendReqCount.setBackgroundResource(R.drawable.red_dot);
            } else {
                tvNewFriendReqCount.setText("");
                tvNewFriendReqCount.setBackgroundDrawable(null);
            }
        }
    }

    @Override
    public void onCheckedChanged(NestRadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rbChatingList:
                mPager.setCurrentItem(0);
                break;
            case R.id.rbRoster:
                mPager.setCurrentItem(1);
                break;
            case R.id.rbExpore:
                mPager.setCurrentItem(2);
                break;
            case R.id.rbMe:
                mPager.setCurrentItem(3);
                break;
        }
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            videoRecordHelper.start();
            return "";
        }

    }

    //
    // RECORD VIDEO
    // END
    //
    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Constants.ACTION_MSG_REV:
                case Constants.ACTION_FRIEND_ADDED:
                    refreshFragment(0);
                    changeBottomBarNum(0);
                    break;
                case Constants.ACTION_FRIEND_REQUEST:
                    changeBottomBarNum(1);
                    refreshFragment(1);
                    break;
                default:
                    break;
            }
        }
    }

    private void refreshFragment(int which) {
        WeakReference fragmentReference = mPagerAdapter.fragmentReferences.get(which);
        if (fragmentReference != null) {
            BaseFragemnt baseFragemnt = (BaseFragemnt) fragmentReference.get();
            if (baseFragemnt != null) {
                baseFragemnt.reloadFragment();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_debug:
                Intent in = new Intent(this, DatabaseShowActivity.class);
                startActivity(in);
                break;
            case R.id.action_add_friend:
                Intent addFriend = new Intent(this, AddFriendActivity.class);
                startActivity(addFriend);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
