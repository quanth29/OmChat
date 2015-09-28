package in.ohmama.omchat.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;

import in.ohmama.omchat.R;
import in.ohmama.omchat.xmpp.XmppConnectHelper;
import in.ohmama.omchat.util.LogUtil;

/**
 * Created by Leon on 9/14/15.
 */
public class BitmapLoadHelper {

    private Context mContext;
    private Bitmap bitmapHoler;
    private LruCache<String, Bitmap> mMemoryCache;

    private DiskLruCache mDiskLruCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    public BitmapLoadHelper(Context mContext) {
        this.mContext = mContext;
        init();
    }

    public void init() {
        // image image's holder picture
        bitmapHoler = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.mini_avatar);

        // bitmap cache initial
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 12;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        // bitmap disklrucache initial
        File cacheDir = getDiskCacheDir(mContext, "bitmap");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        try {
            mDiskLruCache = DiskLruCache.open(cacheDir, getAppVersion(mContext), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public void loadBitmap(String userName, ImageView imageView) {
        final Bitmap bitmap = getBitmapFromMemCache(userName);
        // if memory has the cache, there is no need to load from net
        if (bitmap != null) {
            LogUtil.i("bitmap loader from mem cache",userName);
            imageView.setImageBitmap(bitmap);
        } else {
            if (cancelPotentialWork(userName, imageView)) {
                BitmapWorkerTask task = new BitmapWorkerTask(imageView);
                AsyncDrawable asyncDrawable =
                        new AsyncDrawable(mContext.getResources(), bitmapHoler, task);
                imageView.setImageDrawable(asyncDrawable);
                task.execute(userName);
            }
        }
    }

    public static boolean cancelPotentialWork(String userName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.userName;
            // If bitmapData is not yet set or it differs from the new data
            if (bitmapData == null || !bitmapData.equals(userName)) {
                // Cancel previous task
                LogUtil.i("cancel bitmapData,bitmapData != userName",userName + "," + bitmapData + ","+(bitmapData != userName));
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    public void addBitmapToCache(String key, Bitmap bitmap) throws IOException {
        // Add to memory cache as before
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }

        // Also add to disk cache
        synchronized (mDiskCacheLock) {
            if (mDiskLruCache != null && mDiskLruCache.get(key) == null) {
                DiskLruCache.Editor editor = mDiskLruCache.edit(key);
                if (editor != null) {
                    OutputStream outputStream = editor.newOutputStream(0);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    editor.commit();
                }
                mDiskLruCache.flush();
            }
        }
    }

    public Bitmap getBitmapFromDiskCache(String key) throws IOException {
        synchronized (mDiskCacheLock) {
            // Wait while disk cache is started from background thread
            while (mDiskCacheStarting) {
                try {
                    mDiskCacheLock.wait();
                } catch (InterruptedException e) {
                }
            }
            if (mDiskLruCache != null) {
                DiskLruCache.Snapshot snapShot = mDiskLruCache.get(key);
                if (snapShot != null) {
                    InputStream is = snapShot.getInputStream(0);
                    return BitmapFactory.decodeStream(is);
                }
            }
        }
        return null;
    }

    // Creates a unique subdirectory of the designated app cache directory. Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    // 从网络获取图片
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String userName;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            userName = params[0];
            LogUtil.i("doInBackground userName",userName);
            if (userName != null) {
                try {
                    // try get bitmap from cache
                    Bitmap bitmapCache = getBitmapFromDiskCache(userName);
                    if (bitmapCache != null) {
                        LogUtil.i("bitmap loader from disk cache",userName);
                        return bitmapCache;
                    } else {
                        LogUtil.i("bitmap loader from net",userName);
                        // get bitmap from net
                        VCard card = new VCard();
                        card.load(XmppConnectHelper.getInstance().getConnection(), userName);
                        if (card.getAvatar() != null) {
                            Bitmap bmpAvator = BitmapFactory.decodeByteArray(card.getAvatar(), 0, card.getAvatar().length);
                            // put the bitmap to the cache
                            addBitmapToCache(String.valueOf(params[0]), bmpAvator);
                            return bmpAvator;
                        }
                    }
                } catch (XMPPException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bmAvator) {
            if (!isCancelled() && bmAvator != null) {
                if (imageViewReference != null) {
                    final ImageView imageView = imageViewReference.get();
                    final BitmapWorkerTask bitmapWorkerTask =
                            getBitmapWorkerTask(imageView);
                    if (this == bitmapWorkerTask && imageView != null) {
                        imageView.setImageBitmap(bmAvator);
                    }
                }
            }
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }


}
