package in.ohmama.omchat.util;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.util.Objects;

import in.ohmama.omchat.R;

/**
 * Created by yanglone on 9/19/15.
 */
public abstract class NetLoadThread {

    boolean isHint;
    ProgressDialog mdialog;
    private Context c;

    @SuppressLint("NewApi")
    public NetLoadThread(Context _mcontext) {
        isHint = true;
        c = _mcontext;

        new AsyncTask<Object, Integer, Object>() {

            @Override
            protected Object doInBackground(Object... arg0) {
                return load();
            }

            @Override
            protected void onPostExecute(Object result) {
                if (isHint && (mdialog == null || !mdialog.isShowing())) {
                    return;
                } else {
                    try {
                        result(result);
                        if (isHint) {
                            mdialog.dismiss();

                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onPreExecute() {
                try {
                    mdialog = ProgressDialog.show(c, c.getResources().getString(R.string.dialog_title), c
                            .getResources().getString(R.string.dialog_load_content));
                    mdialog.setCancelable(true);
                    mdialog.setContentView(R.layout.dialog_loadding);
//                        mdialog.setIndeterminateDrawable(c.getResources().getDrawable(R.drawable.progress_dialog_style,null));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }.execute();
    }

    protected abstract Object load();

    protected abstract void result(Object object);

}
