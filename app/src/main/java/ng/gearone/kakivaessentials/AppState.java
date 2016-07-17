package ng.gearone.kakivaessentials;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * Created by anthony on 29/04/16.
 */
public class AppState {
    private static AppState ourInstance = new AppState();

    private Application mApp;
    private boolean configured;

    public static final String TAG = AppState.class.getSimpleName();
    public static final String APP_SESSION = "StoreLocatorSession";

    public static final String KEY_USER_EMAIL = "sessionKeyUserEmail";
    public static final String KEY_USER_ID = "sessionKeyUserId";

    private ConnectivityManager mConnManager;

    protected SharedPreferences session;

    public RequestQueue mRequestQueue;
    public ImageLoader mImageLoader;
    public ArrayList<Model.Product> mProducts = new ArrayList<>();

    public void setRequestQueue(RequestQueue queue) {
        this.mRequestQueue = queue;
    }

    public static AppState getInstance() {
        return ourInstance;
    }

    private AppState() {

    }

    public boolean isConfigured() {
        return configured;
    }

    public void setConfigured(boolean configured) {
        this.configured = configured;
    }

    public void configure(Application appContext) {
        mApp = appContext;

        session = mApp.getSharedPreferences(APP_SESSION, Context.MODE_PRIVATE);

        mConnManager = (ConnectivityManager) mApp.getSystemService(Context.CONNECTIVITY_SERVICE);

        mRequestQueue = Volley.newRequestQueue(mApp);
        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap> mCache = new LruCache<>(50);

            public void putBitmap(String url, Bitmap bitmap) {
                mCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return mCache.get(url);
            }
        });

        setConfigured(true);
    }

    public SharedPreferences getSession() {
        return session;
    }


    public Model.Product getProductById(final String id) {
        return Iterators.find(mProducts.iterator(), new Predicate<Model.Product>() {
            @Override
            public boolean apply(Model.Product input) {
                return input.id.equals(id);
            }
        }, null);
    }

    public boolean isSignedIn() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user != null && !user.isAnonymous();
    }
}
