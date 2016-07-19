package ng.gearone.kakivaessentials;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;

/**
 * Created by anthony on 19/07/16.
 */
public class KeDatabase implements DbContext {

    StorageReference storageRef;
    FirebaseDatabase dbRef;

    public KeDatabase(FirebaseDatabase dbRef, StorageReference strageRef){
        this.storageRef = strageRef;
        this.dbRef = dbRef;
    }

    @Override
    public StorageReference getStorageRef() {
        return storageRef;
    }

    @Override
    public FirebaseDatabase getDbRef() {
        return dbRef;
    }
}
