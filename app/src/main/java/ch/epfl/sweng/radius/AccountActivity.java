package ch.epfl.sweng.radius;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import ch.epfl.sweng.radius.database.Database;
import ch.epfl.sweng.radius.database.MLocation;
import ch.epfl.sweng.radius.database.User;
import ch.epfl.sweng.radius.database.UserInfo;
import ch.epfl.sweng.radius.friends.FriendsFragment;
import ch.epfl.sweng.radius.home.HomeFragment;
import ch.epfl.sweng.radius.messages.MessagesFragment;
import ch.epfl.sweng.radius.profile.ProfileFragment;

public class AccountActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private Fragment homeFragment;
    private Fragment messageFragment;
    private Fragment friendsFragment;
    private Fragment profileFragment;
    private Timer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);
        timer = new Timer();
        // To load the current user infos
        UserInfo.getInstance().fetchDataFromDB();

        // Set the layout
        setContentView(R.layout.activity_account);

        homeFragment = new HomeFragment();
        messageFragment = new MessagesFragment();
        friendsFragment = new FriendsFragment();
        profileFragment = new ProfileFragment();


        loadFragment(homeFragment);

        BottomNavigationView bottomNavigationView = findViewById(R.id.navigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView
                                                            .OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        //fragment = new HomeFragment();
                        loadFragment(homeFragment);
                        break;
                    case R.id.navigation_messages:
                        //fragment = new MessagesFragment();
                        loadFragment(messageFragment);
                        break;
                    case R.id.navigation_friends:
                        //fragment = new SettingsFragment();
                        loadFragment(friendsFragment);
                        break;
                    case R.id.navigation_profile:
                        //fragment = new ProfileFragment();
                        loadFragment(profileFragment);
                        break;
                    default:
                        System.out.println("Unknown item id selected: " + item.getItemId());
                }
                return true;
            }
        });

        // ToolBar initialization
        toolbar = findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
    }

    @Override
    public void onStart(){
        super.onStart();
        enterApp();
        timer.cancel();

    }

    @Override
    public void onStop() {
        super.onStop();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                leaveApp();

            }
        }, 20*60*1000);
    }

    private void leaveApp(){
        Log.e("SAVE SATE", "save UserInfo in external storage");
        UserInfo.getInstance().saveState();

        MLocation current_user_location = UserInfo.getInstance().getCurrentPosition();
        current_user_location.setVisible(false);
        Database.getInstance().writeInstanceObj(current_user_location, Database.Tables.LOCATIONS);
    }

    private void enterApp(){
        MLocation current_user_location = UserInfo.getInstance().getCurrentPosition();
        current_user_location.setVisible(true);
        Database.getInstance().writeInstanceObj(current_user_location, Database.Tables.LOCATIONS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        leaveApp();
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fcontainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, PreferencesActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

}
