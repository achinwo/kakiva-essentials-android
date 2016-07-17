package ng.gearone.kakivaessentials;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Date;

/**
 * Created by anthony on 03/04/16.
 */
public class Model {
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    public static Gson getGson() {
        return gson;
    }

    public static class User extends ModelBase {
        public String username, gender, location;

        @SerializedName("registered_at")
        public Date registeredAt;

        @Exclude
        @Override
        public String getDbDirName() {
            return "users";
        }
    }

    public static class Product extends ModelBase {
        @SerializedName("Author")
        public User author;

        @SerializedName("image_url")
        public String imageUrl;

        public Integer price;

        @SerializedName("posted_at")
        public Date postedAt;

        public String name;
        public String details, title, currency;

        @Exclude
        public Context mContext;

        @Exclude
        @Override
        public String getDbDirName() {
            return "products";
        }

        @Exclude
        public Bitmap getImage() {
            int resId = mContext.getResources().getIdentifier(imageUrl, "drawable", mContext.getPackageName());
            if (resId > 0) {
                return BitmapFactory.decodeResource(mContext.getResources(), resId);
            }
            Log.d("Product", "unable to get resource id of " + imageUrl);
            return null;

        }

        @Override
        public void save(final DbContext db) {
            super.save(db);
            Bitmap bitmap = getImage();

            if (bitmap != null) {
                Utils.uploadImage(bitmap, imageUrl + ".jpg")
                        .done(new DoneCallback<String>() {
                            @Override
                            public void onDone(String result) {
                                imageUrl = result;
                                db.getDbRef().getReference(getDbDirName()).child(id).child("imageUrl").setValue(imageUrl);
                            }
                        })
                        .fail(new FailCallback<Exception>() {
                            @Override
                            public void onFail(Exception result) {
                                Log.e("Product", "Image upload failed for " + imageUrl + ": ", result);
                            }
                        });
            }
        }
    }
}

abstract class ModelBase implements Serializable, Comparable<ModelBase> {
    private static final String TAG = ModelBase.class.getSimpleName();

    public String id;
    public String url;

    @SerializedName("url_path")
    public String urlPath;

    public Date createdAt;
    public Date updatedAt;

    public abstract String getDbDirName();

    public void save(DbContext db) {
        DatabaseReference baseDirRef = db.getDbRef().getReference(getDbDirName());
        if (id == null) {
            id = baseDirRef.push().getKey();
        }
        baseDirRef.child(id).setValue(this);
    }

    public static <T> T fromJson(Class<T> type, String json) {
        T obj = null;
        try {
            obj = Model.getGson().fromJson(json, type);
        } catch (Exception e) {
            Log.e(TAG, "from json failed: " + e);
        }
        return obj;
    }

    public String toJson() {
        return Model.getGson().toJson(this);
    }

    @Override
    public int compareTo(@NonNull ModelBase o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && (o.getClass().equals(this.getClass())) && ((ModelBase) o).id.equals(this.id) && ((ModelBase) o).urlPath.equals(this.urlPath);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(getClass().getSimpleName()).append("{");
        for (Field field : getClass().getFields()) {
            try {
                Object value = field.get(this);
                if (value instanceof ModelBase) value = ((ModelBase) value).url;
                b.append(String.format("%s=%s, ", field.getName(), value));
            } catch (IllegalAccessException e) {
                Log.e(TAG, "toString Error: " + e);
            }
        }
        return b.append("}\n").toString();
    }

}
