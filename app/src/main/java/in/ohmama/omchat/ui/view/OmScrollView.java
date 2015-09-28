package in.ohmama.omchat.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.TranslateAnimation;
import android.widget.ListView;
import android.widget.ScrollView;

import in.ohmama.omchat.util.LogUtil;

/**
 * Created by Leon on 9/8/15.
 */
public class OmScrollView extends ScrollView {

    // data
    private static final float MOVE_FACTOR = 0.5f; // 移动因子,手指移动100px,那么View就只移动50px

    private static final int ANIM_TIME = 300; // 松开手指后, 界面回到正常位置需要的动画时间

    private float startY;// 手指按下时的Y值, 用于在移动时计算移动距离,如果按下时不能上拉和下拉，
    // 会在手指移动时更新为当前手指的Y值

    ScrollViewActiveListener scrollViewActiveListener;
    // ui
    private View contentView; // ScrollView的唯一内容控件
    private final Rect originalRect = new Rect();// 用于记录正常的布局位置
    private View backView; // when this view's top scrolled below this view's bottom, action something
    private ListView mListView;

    // flag
    private boolean canPullDown = false; // 是否可以继续下拉
    private boolean canPullUp = false; // 是否可以继续上拉
    private boolean isMoved = false; // 记录是否移动了布局
    private boolean isOnBottom = false;
    private int touchSlop;
    private int slopDistance; // over the slop distence can do something

    private boolean isFirstTouch = true;

    public OmScrollView(Context context) {
        super(context);
        init(context);
    }

    public OmScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OmScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public OmScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        // 得到Header的高度，这个高度需要用这种方式得到，在onLayout方法里面得到的高度始终是0
        getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (backView != null)
                            slopDistance = backView.getBottom();
                        else
                            slopDistance = getHeight() / 5;
                        getViewTreeObserver()
                                .removeGlobalOnLayoutListener(this);
                    }
                });
    }

    public void setBackView(View v) {
        backView = v;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 0) {
            contentView = getChildAt(0);
        }
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        onFinishInflate();
    }

    @Override
    public void addView(View child, int index) {
        super.addView(child, index);
        onFinishInflate();
    }

    @Override
    public void addView(View child, int width, int height) {
        super.addView(child, width, height);
        onFinishInflate();
    }

    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        onFinishInflate();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (contentView == null)
            return;
        // ScrollView中的唯一子控件的位置信息, 这个位置信息在整个控件的生命周期中保持不变
        originalRect.set(contentView.getLeft(), contentView.getTop(),
                contentView.getRight(), contentView.getBottom());
    }

    /**
     * 在触摸事件中, 处理上拉和下拉的逻辑
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isFirstTouch) {
            isFirstTouch = false;
            checkSubListView(this);
        }
        if (isOnBottom)
            return false;
        if (contentView == null) {
            return super.dispatchTouchEvent(ev);
        }
        // 手指是否移动到了当前ScrollView控件之外
        boolean isTouchOutOfScrollView = ev.getY() >= this.getHeight()
                || ev.getY() <= 0;
        if (isTouchOutOfScrollView) { // 如果移动到了当前ScrollView控件之外
            if (isMoved) {// 如果当前contentView已经被移动, 首先把布局移到原位置
                bounceBack();
            }
            return true;
        }
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                // 判断是否可以上拉和下拉
                canPullDown = isCanPullDown();
                canPullUp = isCanPullUp();
                // 记录按下时的Y值
                startY = ev.getY();
                startX = ev.getX();
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.i("dispatchTouchEvent ACTION_UP contentView.getTop(),", contentView.getTop() + "," + slopDistance);

                if (contentView.getTop() > slopDistance) {
                    if (scrollViewActiveListener != null)
                        scrollViewActiveListener.onFinishPull();
                    LogUtil.i("dispatchTouchEvent 1");
                    bounceBottom();
                } else {
                    LogUtil.i("dispatchTouchEvent 2");
                    bounceBack();
                }

                isVertical = false;
                isHorizental = false;

                int deltaY = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                // 如果横向受控，则不获得事件
                if (isHorizental)
                    break;

                // 在移动的过程中， 既没有滚动到可以上拉的程度， 也没有滚动到可以下拉的程度
                if (!canPullDown && !canPullUp) {
                    startY = ev.getY();
                    canPullDown = isCanPullDown();
                    canPullUp = isCanPullUp();
                    break;
                }

                // 计算手指移动的距离
                float nowY = ev.getY();
                deltaY = (int) (nowY - startY);
                if ((deltaY > touchSlop || isVertical) && !isHorizental) {
                    isVertical = true;
                }

                if (!isVertical) {
                    float nowX = ev.getX();
                    int deltaX = (int) (nowX - startX);
                    deltaX = Math.abs(deltaX);
                    if (deltaX > touchSlop || isHorizental) {
                        isHorizental = true;
                    }
                }
                if (!isHorizental) {
                    // 是否应该移动布局
                    boolean shouldMove = (canPullDown && deltaY > 0) // 可以下拉， 并且手指向下移动
                            || (canPullUp && deltaY < 0) // 可以上拉， 并且手指向上移动
                            || (canPullUp && canPullDown); // 既可以上拉也可以下拉（这种情况出现在ScrollView包裹的控件比ScrollView还小）
                    if (shouldMove) {
                        // 计算偏移量
                        int offset = (int) (deltaY * MOVE_FACTOR);
                        // 随着手指的移动而移动布局
                        contentView.layout(originalRect.left,
                                originalRect.top + offset, originalRect.right,
                                originalRect.bottom + offset);
                        isMoved = true; // 记录移动了布局

                        if (scrollViewActiveListener != null) {
                            if (contentView.getTop() > slopDistance) {
                                scrollViewActiveListener.onPullCrossOver(true);
                            }
                        }
                    }
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    // float iStartY;
    float startX;
    boolean isVertical;
    boolean isHorizental;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (isHorizental) {
                    return false;
                } else if (isVertical)
                    return true;
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 将内容布局移动到原位置 可以在UP事件中调用, 也可以在其他需要的地方调用, 如手指移动到当前ScrollView外时
     */
    public void bounceBack() {
        if (!isMoved && !isOnBottom) {
            return; // 如果没有移动布局， 则跳过执行
        }
        isOnBottom = false;
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(0, 0,
                contentView.getTop(), originalRect.top);
        anim.setDuration(ANIM_TIME);
        contentView.startAnimation(anim);
        // 设置回到正常的布局位置
        contentView.layout(originalRect.left, originalRect.top,
                originalRect.right, originalRect.bottom);
        // 将标志位设回false
        canPullDown = false;
        canPullUp = false;
        isMoved = false;
    }

    private void bounceBottom() {
        if (!isMoved) {
            return; // 如果没有移动布局， 则跳过执行
        }
        isOnBottom = true;
        // 开启动画
        TranslateAnimation anim = new TranslateAnimation(0, 0,
                contentView.getTop(), getHeight());
        anim.setDuration(500);
        contentView.startAnimation(anim);
        // 设置回到正常的布局位置
        contentView.layout(originalRect.left, getHeight(),
                originalRect.right, getHeight() + contentView.getHeight());
        // 将标志位设回false
        canPullDown = false;
        canPullUp = false;
        isMoved = false;
    }

    /**
     * 判断是否滚动到顶部
     */
    private boolean isCanPullDown() {
        return (getScrollY() == 0
                || contentView.getHeight() < getHeight() + getScrollY()) && shouldScroll();
    }

    /**
     * 判断是否滚动到底部
     */
    private boolean isCanPullUp() {
        return false;
//        return contentView.getHeight() <= getHeight() + getScrollY();
    }

    /**
     * if listview is inside the scrollview, set the listview to it
     * otherwise, both listview and scrollView will move at same time
     * if not has listView, just igore this
     *
     * @param listView
     */
    public void setListView(ListView listView) {
        mListView = listView;
    }

    private boolean shouldScroll() {
        boolean shouldScroll = true;
        if (mListView != null) {
            View v = mListView.getChildAt(0);
            if (v != null)
                shouldScroll = v.getY() >= 0;
        }
        return shouldScroll;
    }

    public void setScrollViewActiveListener(ScrollViewActiveListener scrollListener) {
        scrollViewActiveListener = scrollListener;
    }

    public void checkSubListView(ViewGroup vg) {
        if (vg instanceof ListView) {
            mListView = (ListView) vg;
            return;
        }
        for (int i = 0; i < vg.getChildCount(); i++) {
            View subGroupView = vg.getChildAt(i);
            if (subGroupView instanceof ViewGroup) {
                ViewGroup v = (ViewGroup) subGroupView;
                checkSubListView(v);
            }
        }
    }

    public interface ScrollViewActiveListener {
        // finish pull & cross the slop
        void onFinishPull();

        // cross over the slop
        void onPullCrossOver(boolean isCrossed);
    }

}
