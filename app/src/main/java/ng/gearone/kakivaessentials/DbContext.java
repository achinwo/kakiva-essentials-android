package ng.gearone.kakivaessentials;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

/**
 * Created by anthony on 17/07/16.
 */
public interface DbContext {
    StorageReference getStorageRef();
    FirebaseDatabase getDbRef();
}
