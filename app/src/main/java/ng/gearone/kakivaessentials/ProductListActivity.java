package ng.gearone.kakivaessentials;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An activity representing a list of Products. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ProductDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ProductListActivity extends KEActivityBase {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static final String TAG = ProductListActivity.class.getSimpleName();
    private boolean mTwoPane;
    private GridView mGridView;
    DatabaseReference mProductsRef;
    private ProductAdapter mAdapter;
    private View loginFormView;

    private ChildEventListener mPrdsChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded: " + dataSnapshot.getKey());
            Model.Product prd = dataSnapshot.getValue(Model.Product.class);
            getAppState().mProducts.add(prd);
            mAdapter.notifyDataSetChanged();
            Log.d(TAG, "Product: "+prd.imageUrl);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: " + dataSnapshot.getKey());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved: ");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved: " + dataSnapshot.getKey());
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e(TAG, "onCancelled: ", databaseError.toException());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        mGridView = (GridView) findViewById(R.id.gridview);
        mAdapter = new ProductAdapter(this);
        assert mGridView != null;
        mGridView.setAdapter(mAdapter);
        mAdapter.mValues = getAppState().mProducts;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (findViewById(R.id.product_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        signInAnon();
        mProductsRef = mDb.getReference("products");
        loginFormView = findViewById(R.id.login_form);
        assert loginFormView != null;

    }

    @Override
    public void onStart() {
        super.onStart();
        mProductsRef.addChildEventListener(mPrdsChildEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProductsRef.removeEventListener(mPrdsChildEventListener);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        loginFormView.setVisibility(getAppState().isSignedIn() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_logout).setVisible(getAppState().isSignedIn());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "Menu clicked " + item);
        switch (item.getItemId()) {
            case R.id.menu_logout:
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                recreate();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.login_btn:
                Log.d(TAG, "doing log in...");
                Intent loginActivity = new Intent(this, SignInActivity.class);
                startActivity(loginActivity);
                break;
        }
    }

    public class ProductAdapter extends BaseAdapter {
        private Context mContext;
        public List<Model.Product> mValues = Collections.emptyList();

        public ProductAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mValues.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout view;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                view = (RelativeLayout) getLayoutInflater().inflate(R.layout.product_grid_item, parent, false);
            } else {
                view = (RelativeLayout) convertView;
            }

            final Model.Product item = mValues.get(position);

            TextView titleView = (TextView) view.findViewById(R.id.text_view_title);
            titleView.setText(item.title);

            NetworkImageView mImageView = (NetworkImageView) view.findViewById(R.id.imageView);

            String url = item.imageUrl;
            if (mImageView.getImageURL() == null || !mImageView.getImageURL().equals(url)) {
                mImageLoader.get(url, ImageLoader.getImageListener(mImageView, 0, android.R.drawable.ic_dialog_alert));
                mImageView.setImageUrl(url, mImageLoader);
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ProductDetailFragment.ARG_ITEM_ID, item.id);
                        ProductDetailFragment fragment = new ProductDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.product_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ProductDetailActivity.class);
                        intent.putExtra(ProductDetailFragment.ARG_ITEM_ID, item.id);

                        context.startActivity(intent);
                    }
                }
            });
            return view;
        }

    }

}
