package in.ohmama.omchat.ui.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by Leon on 9/9/15.
 */
public class OmLongClickButton extends Button {

    private OmLongclickListner longclickListner;

    private final int interval = 300;
    private final long MINIMUL_TIME = 1000;
    private long beginTime;

    public OmLongClickButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                longclickListner.start();
                beginTime = SystemClock.elapsedRealtime();
                post(actionRun);
                break;
            case MotionEvent.ACTION_UP:
                removeCallbacks(actionRun);
                long current = SystemClock.elapsedRealtime();
                long elapse = current - beginTime;
                if (elapse < MINIMUL_TIME) {
                    longclickListner.notReachMinimum();
                } else {
                    longclickListner.stop((int) elapse / 1000);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    Runnable actionRun = new Runnable() {
        @Override
        public void run() {
            postDelayed(this, interval);
            int elapseTime = (int) (SystemClock.elapsedRealtime() - beginTime);
            longclickListner.onRepeat(elapseTime);
        }
    };

    public void setLongclickListner(OmLongclickListner longclickListner) {
        this.longclickListner = longclickListner;
    }

    public interface OmLongclickListner {
        void onRepeat(int elapseTime);

        // press time not reach minimum time cancel action
        void notReachMinimum();

        void stop(int elapseTime);

        void start();
    }
}
