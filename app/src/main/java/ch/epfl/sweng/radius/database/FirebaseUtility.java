/** Represents an interface to communicate with the Firebase Realtime Database
 * @usage : 1 . Instantiate class by providing the table we need to access in the DB
 *              ("user", "message", ...)
 *          2 . Read objects of this table by providing an object with the id we want to match.
 *          3 . Write/Update by providing the object we need to store
 */
package ch.epfl.sweng.radius.database;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.radius.storage.FirebaseStorageUtility;

class ChildListener implements ChildEventListener {

    CallBackDatabase callback;
    Pair<String, Class> child;

    ChildListener(CallBackDatabase callback, Pair<String, Class> child){
        this.callback = callback;
        this.child = child;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        callback.onFinish(dataSnapshot.getValue(child.second));
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        Log.e("Firebase", "Child Changed !");
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        callback.onError(databaseError);

    }


}

public class FirebaseUtility extends Database{

    public FirebaseUtility(){}

    @Override
    public String getCurrent_user_id() {
        if(FirebaseAuth.getInstance().getCurrentUser() == null)
            Log.e("DEBUGG","DEBUG VALUE IS" + Database.DEBUG_MODE);
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }


    @Override
    public void writeToInstanceChild(final DatabaseObject obj, Tables tablename,
                                              final String childName, final Object child){
        FirebaseDatabase.getInstance()
                .getReference(tablename.toString())
                .child(obj.getID())
                .child(childName)
                .setValue(child);
    }

    @Override
    public void listenObjChild(final DatabaseObject obj,
                               final Tables tableName,
                               final Pair<String, Class> child,
                               final CallBackDatabase callback) {

        ChildListener childListener = new ChildListener(callback, child);

        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .child(obj.getID())
                .child(child.first)
                .addChildEventListener(childListener);
    }



    @Override
    public void readObjOnce(final DatabaseObject obj,
                            final Tables tableName,
                            final CallBackDatabase callback) {
        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .child(obj.getID())
                .addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void  onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getValue() == null) {
                    writeInstanceObj(obj, tableName);
                    callback.onFinish(obj);
                }
                else
                    callback.onFinish(dataSnapshot.getValue(obj.getClass()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        });
    }

    @Override
    public void readObj(final DatabaseObject obj,
                        final Tables tableName,
                        final CallBackDatabase callback) {

  //      Log.w("Firebase Message", "Read " + obj.getClass() + "Called by "+ getLogTagWithMethod());

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void  onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    writeInstanceObj(obj, tableName);
                    callback.onFinish(obj);
                }
                else
                    callback.onFinish(dataSnapshot.getValue(obj.getClass()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);
            }
        };

        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .child(obj.getID())
                .addValueEventListener(listener);
    }

    @Override
    public void readListObjOnce(final List<String> ids,
                            final Tables tableName,
                            final CallBackDatabase callback) {

        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void  onDataChange(@NonNull DataSnapshot dataSnapshot) {
            //    Log.e("DEBUG", getLogTagWithMethod());

                List<DatabaseObject> allItems = new ArrayList<>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            //        Log.e("DEBUG", postSnapshot.getKey());
                    DatabaseObject snap = (DatabaseObject)postSnapshot
                            .getValue(tableName.getTableClass());
                    if (ids.contains(snap.getID())) {
                        allItems.add((DatabaseObject)postSnapshot
                                .getValue(tableName.getTableClass()));
                    }
                }

                callback.onFinish(allItems);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                callback.onError(databaseError);

            }
        });
    }

    @Override
    public void readAllTableOnce(final Tables tableName, final CallBackDatabase callback) {
        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .addListenerForSingleValueEvent( new ValueEventListener() {
                    @Override
                    public void  onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.d("DEBUG", getLogTagWithMethod());
                        List<DatabaseObject> allItems = new ArrayList<>();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                            DatabaseObject snap = (DatabaseObject)postSnapshot
                                    .getValue(tableName.getTableClass());
                                allItems.add((DatabaseObject)postSnapshot
                                        .getValue(tableName.getTableClass()));
                        }
                        Log.e("DEBUGG0", "Real Firebase my man");
                        callback.onFinish(allItems);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        callback.onError(databaseError);

                    }
                });
    }

    @Override
    public void writeInstanceObj(final DatabaseObject obj, final Tables tableName){

        Log.d("Firebase Message", "Called for " + obj.getID());
        if(obj.getClass() == ChatLogs.class) {
            ChatLogs test = (ChatLogs) obj;
            Log.d("Firebase Message", "Called for " + ((ChatLogs) obj).getMessages().size() +getLogTagWithMethod());
        }
        Log.d( "writeInstance", "moveCamerafetchh: ");
        FirebaseDatabase.getInstance()
                .getReference(tableName.toString())
                .child(obj.getID()).setValue(obj);
    }




    private String getLogTagWithMethod() {

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        String res = "";
        for (int i = 0; i < trace.length  && i < 30; i++)
            res +=  trace[i].getClassName() + "." + trace[i].getMethodName() + ":" + trace[i].getLineNumber() + "\n";
        return res;
    }

}
