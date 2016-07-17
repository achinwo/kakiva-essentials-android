package ng.gearone.kakivaessentials;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * Created by anthony on 05/04/16.
 */
public class KEActivityBase extends AppCompatActivity implements DbContext, DialogInterface.OnCancelListener, SwipeRefreshLayout.OnRefreshListener, FirebaseAuth.AuthStateListener {

    public static final String TAG = KEActivityBase.class.getSimpleName();

    protected AppState appState;
    protected ProgressDialog mProgressDialog;
    protected ImageLoader mImageLoader;
    public RequestQueue mRequestQueue;
    protected FirebaseAuth mAuth;

    protected SwipeRefreshLayout mSwipeLayout;
    StorageReference mRootStorageRef;
    FirebaseDatabase mDb;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onReceiveBroadcast(context, intent);
        }

    };

    public void onReceiveBroadcast(Context context, Intent intent) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setOnCancelListener(this);
        mImageLoader = getAppState().mImageLoader;
        mRequestQueue = getAppState().mRequestQueue;
        mRootStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://kakiva-essentials.appspot.com");
        mDb = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
        } else {
            // User is signed out
            Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        // ...
    }

    public FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public void signInAnon() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInAnonymously:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInAnonymously", task.getException());
                            Toast.makeText(KEActivityBase.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d(TAG, "Anon sign in successful");
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);
    }

    public void setSwipeRefreshLayout(SwipeRefreshLayout mSwipeLayout) {
        this.mSwipeLayout = mSwipeLayout;
        this.mSwipeLayout.setOnRefreshListener(this);
        this.mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeLayout;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    public void showLoading(String message) {
        if (mSwipeLayout == null || !mSwipeLayout.isRefreshing()) { // no need to show dialog if refresh animation is active
            mProgressDialog.setMessage(message);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        }
    }

    public void hideLoading() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.hide();
        }

        if (mSwipeLayout != null && mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
    }

    public AppState getAppState() {
        appState = AppState.getInstance();

        if (!appState.isConfigured()) {
            Log.d(TAG, "configuring app state singleton...");
            appState.configure(getApplication());
            getApplicationContext();
        }
        return appState;
    }

    public SharedPreferences getSession() {
        return getAppState().getSession();
    }

    public void cancelPendingRequests() {
        getAppState().mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                Log.d(TAG, "request running: " + request);
                return true;
            }
        });
        if (mSwipeLayout != null && mSwipeLayout.isRefreshing()) mSwipeLayout.setRefreshing(false);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        Log.d(TAG, "onCancelDialog: cancelling pending requests...");
        cancelPendingRequests();
        Toast.makeText(this, "Request cancelled", Toast.LENGTH_SHORT).show();
    }

    public void onClick(View view) {
        Log.d(TAG, "view clicked: "+view);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "KEActivityBase: onRefresh");
    }

    @Override
    public StorageReference getStorageRef() {
        return mRootStorageRef;
    }

    @Override
    public FirebaseDatabase getDbRef() {
        return mDb;
    }
}

