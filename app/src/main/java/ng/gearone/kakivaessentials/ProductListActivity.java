package ng.gearone.kakivaessentials;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import ng.gearone.kakivaessentials.dummy.DummyContent;

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
    private ChildEventListener mProductsListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildAdded: " + s);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildChanged: " + s);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved: ");
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            Log.d(TAG, "onChildMoved: " + s);
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
        final ProductAdapter adapter = new ProductAdapter(this);
        assert mGridView != null;
        mGridView.setAdapter(adapter);

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


    }

    @Override
    public void onStart() {
        super.onStart();
        mProductsRef.addChildEventListener(mProductsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProductsRef.removeEventListener(mProductsListener);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        super.onAuthStateChanged(firebaseAuth);
        if (getUser() != null)
            ((ProductAdapter) mGridView.getAdapter()).mValues = DummyContent.ITEMS;
    }

    public class ProductAdapter extends BaseAdapter {
        private Context mContext;
        public List<Model.Product> mValues;

        public ProductAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mValues != null ? mValues.size() : 0;
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

            final Model.Product item = DummyContent.ITEMS.get(position);

            Button mBtn = (Button) view.findViewById(R.id.btn);
            //mIdView = (TextView) view.findViewById(R.id.text_view_topic_title);
            TextView titleView = (TextView) view.findViewById(R.id.text_view_title);
            titleView.setText(item.title);

            NetworkImageView mImageView = (NetworkImageView) view.findViewById(R.id.imageView);

            Uri.Builder b = new Uri.Builder();
            //https://firebasestorage.googleapis.com/v0/b/kakiva-essentials.appspot.com/o/images%2Fkakiva_conditioner.jpg?alt=media&token=e9245588-789d-440f-b14c-41477d60f68a
            b.scheme("https").encodedAuthority("firebasestorage.googleapis.com").appendEncodedPath("v0/b/kakiva-essentials.appspot.com/o/").appendPath("images/" + item.imageUrl + ".jpg")
                    .appendQueryParameter("alt", "media");
            String url = b.toString();
            if (mImageView.getImageURL() == null || !mImageView.getImageURL().equals(url)) {

                Log.d(TAG, "setting url " + url);
                mImageLoader.get(url, ImageLoader.getImageListener(mImageView, 0, android.R.drawable.ic_dialog_alert));
                mImageView.setImageUrl(url, mImageLoader);
            }

            mBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "upload: " + item.imageUrl);
                    item.mContext = mContext;
                    item.save(ProductListActivity.this);
                }
            });

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


//        @Override
//        public void onBindViewHolder(final ViewHolder holder, int position) {
//            final Model.Product item = mValues.get(position);
////            item.id = "16";
////            item.imageUrl = "https://drive.google.com/uc?id=0BxFEUzGuILdKaGlZQ1FOMGFENW8";
////            item.title = "Kakiva Conditioner";
////            item.details = "Our super shea butter, natural oils & neem enriched conditioner for moisturising, conditioning & reviving experience of haircare leaving it soft & lustrious. Intensive haircare especially dry & damaged hair\n" +
////                    "Paraben free! 500ml";
//            holder.mItem = item;
//            //holder.mIdView.setText(mValues.get(position).id);
//            holder.mContentView.setText(item.title);
//
//            //LinearLayout.LayoutParams imgParams = (LinearLayout.LayoutParams) holder.mImageView.getLayoutParams();
//            //imgParams.width = (int) (mRecyclerView.getWidth() * 0.35);
//            //holder.mImageView.setLayoutParams(imgParams);
//
//
}
