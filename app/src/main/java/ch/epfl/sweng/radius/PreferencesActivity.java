package ch.epfl.sweng.radius;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import ch.epfl.sweng.radius.database.MLocation;
import ch.epfl.sweng.radius.database.OthersInfo;
import ch.epfl.sweng.radius.database.UserInfo;

/**
 * Activity that handle all the users preferences
 */
public class PreferencesActivity extends PreferenceActivity {

    private static final String INCOGNITO = "incognitoSwitch";
    private static final String INVISIBLE = "You are currently invisible, nobody can see you in the map.";
    private static final String VISIBLE = "You are visible, people can see your location in the map.";
    private static final String DELETINGACCOUNTMESSAGE = "All your conversations and friends will be deleted.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment(), "preferencesFragment").commit();
    }

    public static void deleteUser(){
        MLocation currentLocation = UserInfo.getInstance().getCurrentPosition();
        currentLocation.setDeleted(true);
        currentLocation.setTitle("Deleted User - " + currentLocation.getTitle());
        UserInfo.getInstance().updateLocationInDB();

    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            // Load the Preferences from the XML file
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_preferences);

            //Initialize the preferences activity
            initializeIncognitoPreference();
            setupLogoutButton();
            setupDeleteAccountButton();
        }

        public void setupLogoutButton() {
            Preference logOutButton = findPreference("logOutButton");

            logOutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    logOut();
                    return true;
                }
            });
        }

        public void logOut() {
            if (MainActivity.googleSignInClient != null) {
                UserInfo.getInstance().getCurrentPosition().setVisible(false);
                UserInfo.getInstance().updateLocationInDB();
                FirebaseAuth.getInstance().signOut();
                MainActivity.googleSignInClient.signOut()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                revokeAccess();
                            }
                        });
            }


        }

        private void setupDeleteAccountButton() {
            Preference deleteAccountButton = findPreference("deleteAccount");
            deleteAccountButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //Prepare alert dialog
                    AlertDialog.Builder dialog = setupAlertDialogBuilder();
                    AlertDialog alertDialog = dialog.create();
                    alertDialog.show();

                    return true;
                }
            });
        }

        /*
        * Creates a pop up to ask the user if they want
        * to delete their account one more time and warn them.
        * */
        private AlertDialog.Builder setupAlertDialogBuilder() {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Are you sure?");
            dialog.setMessage(DELETINGACCOUNTMESSAGE);

            setupPositiveButton(dialog);
            setupNegativeButton(dialog);

            return dialog;
        }

        /*
        * Deletes account and takes the user to the sign in page
        * */
        public void setupPositiveButton(AlertDialog.Builder dialog) {
            final FirebaseAuth auth = FirebaseAuth.getInstance();
            dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    auth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getActivity(), "Account Deleted", Toast.LENGTH_SHORT).show();
                                deleteUser();
                                OthersInfo.getInstance().removeAllObservers();
                                UserInfo.deleteDataStorage();
                                logOut();
                                UserInfo.getInstance().resetCurrentData();
                                FirebaseDatabase.getInstance().goOffline();
                                //delete user

                            } else {
                                Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });
        }



        /*
        * Cancels the operation to delete the account.
        * */
        private void setupNegativeButton(AlertDialog.Builder dialog) {
            dialog.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        private void initializeIncognitoPreference() {
            SwitchPreference incognitoPref = (android.preference.SwitchPreference) findPreference(INCOGNITO);
            if (!incognitoPref.isChecked()) {
                incognitoPref.setSummaryOff(VISIBLE);
            } else {
                incognitoPref.setSummaryOn(INVISIBLE);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        // TODO: New File with settings actions and call also in mainActivity
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key){
                case "incognitoSwitch":
                    changeInvisibility();
                    break;
                case "notificationCheckbox":
                    break;
            }
        }

        private void changeInvisibility() {
            SwitchPreference incognitoPref = (android.preference.SwitchPreference) findPreference(INCOGNITO);
            boolean invisible = incognitoPref.isChecked();
            UserInfo.getInstance().getCurrentPosition().setVisible(!invisible);
            UserInfo.getInstance().updateLocationInDB();
            if (!invisible) {
                incognitoPref.setSummaryOff(VISIBLE);
            } else {
                incognitoPref.setSummaryOn(INVISIBLE);
            }
        }



        private void revokeAccess() {
            MainActivity.googleSignInClient.revokeAccess()
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
        }
    }

}
