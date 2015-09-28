package in.ohmama.omchat.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.jivesoftware.smack.Roster;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;
import in.ohmama.omchat.Constants;
import in.ohmama.omchat.R;
import in.ohmama.omchat.helper.BitmapLoadHelper;
import in.ohmama.omchat.helper2.ImageCache;
import in.ohmama.omchat.helper2.ImageFetcher;
import in.ohmama.omchat.model.DbUtil;
import in.ohmama.omchat.model.OmUser;
import in.ohmama.omchat.model.service.ComingMsgService;
import in.ohmama.omchat.model.service.UserService;
import in.ohmama.omchat.ui.activity.ChattingActivity;
import in.ohmama.omchat.ui.activity.NewFriendActivity;
import in.ohmama.omchat.ui.adapter.RosterAdapter;
import in.ohmama.omchat.util.LogUtil;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RosterFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RosterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RosterFragment extends BaseFragemnt {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;
    private OnFragmentInteractionListener mListener;


    private ImageFetcher mImageFetcher;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private static final String IMAGE_CACHE_DIR = "avators";

    private ListView lvFriends;
    private View rootView;
    View header;

    private UserService userService;
    private ComingMsgService comingMsgService;
    /**
     * 用户列表
     */
    private List<OmUser> userDatas = new ArrayList<>();
    private BitmapLoadHelper bitmapLoadHelper;
    /**
     * 花名册
     */
    private Roster roster;
    /**
     * 花名册数据适配器
     */
    private RosterAdapter rosterAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
        mImageFetcher.setLoadingImage(R.drawable.mini_avatar);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bitmapLoadHelper = new BitmapLoadHelper(getActivity());
        rootView = inflater.inflate(R.layout.fragment_contact, container, false);
        init(rootView);
        return rootView;
    }

    public void init(View rootView) {
        userService = DbUtil.getUserService();
        comingMsgService = DbUtil.getComingMessageService();

        lvFriends = (ListView) rootView.findViewById(R.id.listview);
        lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view,
                                    int position, long id) {
                if (position == 0) {
                    // header click
                    Intent showFriendReq = new Intent(getActivity(), NewFriendActivity.class);
                    startActivity(showFriendReq);
                } else {
                    String user = userDatas.get(position-1).getUserName();
                    Intent chatIntent = new Intent(getActivity(), ChattingActivity.class);
                    chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    chatIntent.putExtra(Constants.KEY_USER_NAME, user);
                    startActivity(chatIntent);
                }
            }
        });
        // 获取花名册
        loadRosterFromDb();
//        updateRoster();
        rosterAdapter = new RosterAdapter(getActivity(), userDatas, mImageFetcher);

        // header view “新的朋友”
        header = LayoutInflater.from(getActivity()).inflate(R.layout.new_friend_item, lvFriends, false);
        lvFriends.addHeaderView(header);
        lvFriends.setAdapter(rosterAdapter);
    }

    public void loadRosterFromDb() {
//        QueryBuilder<OmUser> qb = DbUtil.getUserDao().queryBuilder();
//        qb.where(OmUserDao.Properties.UserName.eq(OmApplication.userSelf.getUserName()));
        userDatas = userService.loadFriend();
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        reloadFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        LogUtil.i("on onStart");
    }

    @Override
    public void reloadFragment() {
        super.reloadFragment();
//        userDatas.clear();
//        rosterAdapter.notifyDataSetChanged();
        LogUtil.i("in RosterFragment reloadFragment");
        userDatas = userService.loadFriend();
        rosterAdapter.setUserinfos(userDatas);

        // 查检是否有新的好友请求
        int count = userService.loadFriendReqMsgFromComingMsg().size();
        TextView tvCount = (TextView) header.findViewById(R.id.msg_count);
        if (count > 0) {
            LogUtil.i("count > 0");
            tvCount.setText(count + "");
            tvCount.setBackgroundResource(R.drawable.red_dot);
        } else {
            tvCount.setText("");
            tvCount.setBackgroundDrawable(null);
        }
        rosterAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public static RosterFragment newInstance(String param1, String param2) {
        RosterFragment fragment = new RosterFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RosterFragment() {
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
