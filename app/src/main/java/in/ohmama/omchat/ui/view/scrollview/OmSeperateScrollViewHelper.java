package in.ohmama.omchat.ui.view.scrollview;

import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.ohmama.omchat.R;


/**
 * Created by Leon on 5/31/15.
 */
public class OmSeperateScrollViewHelper {

    public Toolbar mToolbar;
    private View mHeaderView;
    private View mContentView;
    private View[] toolbarChildren;
    private ViewGroup headerContainer;
    private ViewGroup contentContainer;
    protected static final String TAG = "OmSeperateScrollViewHelper";

    private LayoutInflater mInflater;
    private int mHeaderLayoutResId;
    private int mContentLayoutResId;
    private int headerHeight;

    public void initActionBar(Activity activity) {
        mToolbar = (Toolbar) activity.findViewById(R.id.mToolbar);
    }

    public View createView(LayoutInflater inflater) {
        mInflater = inflater;

        if (mContentView == null) {
            mContentView = inflater.inflate(mContentLayoutResId, null);
        }
        if (mHeaderView == null) {
            mHeaderView = inflater.inflate(mHeaderLayoutResId, null, false);
        }
        final OmScrollRootView root = createRootView();
        return root;
    }

    private OmScrollRootView createRootView() {
        OmScrollRootView omScrollRootViewContainer = (OmScrollRootView) mInflater.inflate(R.layout.scroll_root_view, null);
        omScrollRootViewContainer.setOnScrollChangedCallback(mOnScrollChangedListener);

        headerContainer = (ViewGroup) omScrollRootViewContainer.findViewById(R.id.header_container);
        contentContainer = (ViewGroup) omScrollRootViewContainer.findViewById(R.id.content_container);
        headerContainer.getScrollY();
        headerContainer.addView(mHeaderView);
        contentContainer.addView(mContentView);
        return omScrollRootViewContainer;
    }

    private OmScrollChangedCallback mOnScrollChangedListener = new OmScrollChangedCallback() {
        public void onScroll(int l, int t) {
            updatePosition(t);
        }
    };

    public void updatePosition(int top) {
        headerContainer.setTop(0);
        headerHeight = headerContainer.getHeight();
        float ratio = (float) Math.min(Math.max(top, 0), headerHeight * 2) / (headerHeight * 2);
        int newAlpha = (int) ((1 - ratio) * 255);
        headerContainer.setAlpha(newAlpha);
    }

    public final <T extends OmSeperateScrollViewHelper> T headerLayout(int layoutResId) {
        mHeaderLayoutResId = layoutResId;
        return (T) this;
    }

    public final <T extends OmSeperateScrollViewHelper> T contentLayout(int layoutResId) {
        mContentLayoutResId = layoutResId;
        return (T) this;
    }

    public void setmHeaderView(View mHeaderView) {
        this.mHeaderView = mHeaderView;
    }

    public interface ObservableScrollable {
        void setOnScrollChangedCallback(OmScrollChangedCallback callback);
    }

}