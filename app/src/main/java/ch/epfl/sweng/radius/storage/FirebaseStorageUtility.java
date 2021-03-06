package ch.epfl.sweng.radius.storage;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import ch.epfl.sweng.radius.database.Database;
import ch.epfl.sweng.radius.database.MLocation;
import ch.epfl.sweng.radius.database.UserInfo;

public class FirebaseStorageUtility extends Storage{

    private StorageReference storageReference;
    protected StorageTask mUploadTask;

    public FirebaseStorageUtility() {
        storageReference = FirebaseStorage.getInstance().getReference(StorageFile.PROFILE_PICTURES.toString());
    }

    private String getFileExtension(Uri uri, Activity activity) {
        ContentResolver cR = activity.getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //I just realized I have to test this :_(
    public void uploadFile(Uri mImageUri, Activity activity) {
        if (mImageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri, activity));

            mUploadTask = fileReference.putFile(mImageUri).
                    addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String uploadUrl = uri.toString();
                                    MLocation currentUser = UserInfo.getInstance().getCurrentPosition();
                                    currentUser.setUrlProfilePhoto(uploadUrl);
                                    Database.getInstance().writeInstanceObj(currentUser, Database.Tables.LOCATIONS);
                                }
                            });
                        }
                    });
        }
    }
}
