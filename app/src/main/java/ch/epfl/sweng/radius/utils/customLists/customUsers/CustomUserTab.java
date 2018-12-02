package ch.epfl.sweng.radius.utils.customLists.customUsers;

import android.util.Log;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.radius.database.CallBackDatabase;
import ch.epfl.sweng.radius.database.DBLocationObserver;
import ch.epfl.sweng.radius.database.DBUserObserver;
import ch.epfl.sweng.radius.database.Database;
import ch.epfl.sweng.radius.database.MLocation;
import ch.epfl.sweng.radius.database.OthersInfo;
import ch.epfl.sweng.radius.database.User;
import ch.epfl.sweng.radius.database.UserInfo;
import ch.epfl.sweng.radius.database.UserUtils;
import ch.epfl.sweng.radius.utils.customLists.CustomListAdapter;
import ch.epfl.sweng.radius.utils.customLists.CustomListItem;
import ch.epfl.sweng.radius.utils.customLists.CustomTab;


public abstract class CustomUserTab extends CustomTab implements DBLocationObserver {

    public CustomListAdapter getAdapter(List<CustomListItem> items) {
        return new CustomUserListAdapter(items, getContext());
    }

    private MLocation getLoc(String userId){
        MLocation userLoc = OthersInfo.getInstance().getUsersInRadius().containsKey(userId) ?
                OthersInfo.getInstance().getUsersInRadius().get(userId) :
                OthersInfo.getInstance().getConvUsers().get(userId);
        return userLoc;
    }

    private ArrayList<CustomListItem> getItems (List<User> values, String userId){
        ArrayList<CustomListItem> ret = new ArrayList<>();
        for (User user :  values) {
     //       Log.e("Refactor CustomUserTab", "Current fetched userID is " + user.getID());
            MLocation userLoc = getLoc(userId);
            if (userLoc != null && !user.getID().equals(userId)) {
                Log.e("User added " , userLoc.getID());
                ret.add(new CustomListItem(user.getID(), user.getConvFromUser(userId), userLoc.getTitle()));
            }
        }

        return ret;
    }

    public CallBackDatabase getAdapterCallback() {
        return new CallBackDatabase() {
            @Override
            public void onFinish(Object value) {
                ArrayList<CustomListItem> usersItems = new ArrayList<>();
               // adapter = getAdapter(usersItems);
                String convId;
                String userId = UserInfo.getInstance().getCurrentUser().getID();

                usersItems = getItems((List<User>) value, userId);
                adapter.setItems(usersItems);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        };

    }


    public CustomUserTab() {
        UserUtils.getInstance().addLocationObserver(this);
    }


    @Override
    protected void setUpAdapterWithList(List<String> listIds){
        ArrayList<CustomListItem> usersItems = new ArrayList<>();
        List<MLocation> locs = new ArrayList<>(OthersInfo.getInstance().getUsersInRadius().values());
        for(MLocation loc : locs)
            if(loc.isVisible()){
                usersItems.add(new CustomListItem(loc.getID(), UserInfo.getInstance().getCurrentUser().getConvFromUser(loc.getID())
                        , loc.getTitle()));
            }
      //  database.readListObjOnce(listIds,
      //          Database.Tables.USERS, getAdapterCallback());
        adapter.setItems(usersItems);
        adapter.notifyDataSetChanged();
    }

    protected abstract List<String> getIds(User current_user);

    @Override
    public void onLocationChange(String id) {
        if (id.equals(Database.Tables.LOCATIONS.toString())){
            super.setUpAdapter();
        }
    }
}
