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
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.debug.DatabaseShowActivity;
import in.ohmama.omchat.helper.RecorderHelper;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.MessageService;
import in.ohmama.omchat.ui.OnFragmentChangeListener;
import in.ohmama.omchat.ui.adapter.MainAdapter;
import in.ohmama.omchat.ui.fragment.BaseFragemnt;
import in.ohmama.omchat.ui.view.NestRadioGroup;
import in.ohmama.omchat.ui.view.OmLongClickButton;
import in.ohmama.omchat.ui.view.OmScrollView;
import in.ohmama.omchat.util.LogUtil;
import in.ohmama.omchat.util.NotificationUtil;
import in.ohmama.omchat.xmpp.XmppConnectHelper;

public class MainActivity extends BaseActivity implements OnFragmentChangeListener, NestRadioGroup.OnCheckedChangeListener {

    private ViewPager mPager;
    private OmScrollView omScrollView;
    private View cancelCapture;
    private OmLongClickButton longClickButton;
    private FrameLayout videoViewContainer;
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

    private MessageService messageService;
    private ComingMsgService comingMsgService;
    private MainReceiver mainReceiver;

    // is recording the video
    private boolean isRecording = false;
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
        omScrollView = (OmScrollView) findViewById(R.id.omScrollView);
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

        setToolbarTitle(0);
        setSupportActionBar(mToolbar);

        messageService = DbUtil.getMessageService();
        comingMsgService = DbUtil.getComingMessageService();

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
        omScrollView.setScrollViewActiveListener(new OmScrollView.ScrollViewActiveListener() {

            @Override
            public void onFinishPull() {
                videoRecordHelper = new RecorderHelper(MainActivity.this);
                videoViewContainer.addView(videoRecordHelper.getCameraPreview());
                isRecording = false;
                // hide the bottom tabs
                bottomTabsContainer.setVisibility(View.INVISIBLE);
                omScrollView.setVisibility(View.INVISIBLE);
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
                // press button, begin record video

            }

            @Override
            public void notReachMinimum() {
                // not reach the minimum time, cancel record
                stopRecord();
            }

            @Override
            public void start() {
                if (!isRecording) {
//                    new MediaPrepareTask().execute();
                    videoRecordHelper.start();
                }
            }

            @Override
            public void stop(int elapseTime) {
                stopRecord();
                // 立即播放
                videoViewContainer.removeAllViews();
                videoViewContainer.addView(videoRecordHelper.getVideoPreview());
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
    }

    private void bounceBackAndReleaseRes() {
        if (isRecording) {
            videoRecordHelper.cameraStop();
            omScrollView.bounceBack();
            omScrollView.setVisibility(View.VISIBLE);
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

//            String videoPath = videoRecordHelper.startRecord();
//            if (videoPath != null) {
//                isRecording = true;
//            } else {
//                Toast.makeText(MainActivity.this, "init video record fail", Toast.LENGTH_SHORT).show();
//                LogUtil.i("init video record fail");
//            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
//            videoViewContainer.setTag(result);
        }
    }

    public TextureView getTextureViewInstance() {
        TextureView mTextureView = new TextureView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mTextureView.setLayoutParams(params);
        return mTextureView;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
