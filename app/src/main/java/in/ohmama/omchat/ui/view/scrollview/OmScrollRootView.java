package in.ohmama.omchat.ui.view.scrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Leon on 5/31/15.
 */
public class OmScrollRootView extends ScrollView implements OmSeperateScrollViewHelper.ObservableScrollable {
    private OmScrollChangedCallback mOmScrollChangedCallback;

    public OmScrollRootView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t != oldt)
            mOmScrollChangedCallback.onScroll(l, t);
    }

    @Override
    public void setOnScrollChangedCallback(OmScrollChangedCallback callback) {
        mOmScrollChangedCallback = callback;
    }

}
