package ch.epfl.sweng.radius.utils.customLists.customGroups;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.radius.R;
import ch.epfl.sweng.radius.database.CallBackDatabase;
import ch.epfl.sweng.radius.database.Database;
import ch.epfl.sweng.radius.database.User;
import ch.epfl.sweng.radius.utils.customLists.CustomListAdapter;
import ch.epfl.sweng.radius.utils.customLists.CustomListItem;
import ch.epfl.sweng.radius.utils.customLists.CustomTab;


public abstract class CustomGroupTab extends CustomTab {

    public CustomListAdapter getAdapter(List<CustomListItem> items){
        return new CustomGroupListAdapter(items, getContext());
    }

    public CallBackDatabase getAdapterCallback(){
        return new CallBackDatabase() {
            @Override
            public void onFinish(Object value) {
                ArrayList<CustomListItem> usersItems = new ArrayList<>();
                String convId;
                String userId = database.getCurrent_user_id();

                //TODO REMOVE THIS LINE WHEN NOT HARDCODED NEEDED ANYMORE
                usersItems.add(new CustomListItem("GroupId","GroupConvId","EPFL_GROUP"));

                adapter.setItems(usersItems);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(DatabaseError error) {
                Log.e("Firebase", error.getMessage());
            }
        };
    }


    public CustomGroupTab() { }

    protected abstract List<String> getIds(User current_user);
}