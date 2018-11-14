package ch.epfl.sweng.radius.browseProfiles;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import ch.epfl.sweng.radius.messages.MessageListActivity;

import ch.epfl.sweng.radius.database.User;

public class CustomListener {
    private int clickedPic;
    private String clickedName;
    private String userUID;


    public CustomListener(int clickedPic, User itemUser){
        this.clickedPic = clickedPic;
        this.clickedName = itemUser.getNickname();
        this.userUID = itemUser.getID();
    }

    public void setCustomOnClick(ImageView imageView, final Context context) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, BrowseProfilesActivity.class);
                intent.putExtra("Clicked Picture", clickedPic);
                intent.putExtra("Clicked Name", clickedName);
                intent.putExtra("UID", userUID);
                context.startActivity(intent);
            }
        });
    }

    public void setCustomOnClick(TextView textView, final Context context, final String userId, final String convId) {
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, MessageListActivity.class);
                Bundle b = new Bundle();
                b.putString("otherUserId",userId);
                b.putString("chatId", convId);
                intent.putExtras(b); //Put your id to your next Intent
                context.startActivity(intent);

            }
        });


    }

}
