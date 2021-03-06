package ch.epfl.sweng.radius.utils.customLists;

import ch.epfl.sweng.radius.R;

public class CustomListItem {
    private String itemId;
    private String itemName;
    private String convId;
    private int profilePic;

    public CustomListItem(String itemId, String convId,String itemName){
        this.itemId = itemId;
   //     Log.e("CustomListItem: ", itemId);
        this.itemName = itemName;
        this.convId = convId;
        this.profilePic = R.drawable.user_photo_default; // Getting the Profile pictures here.
    }

    public int getProfilePic() {
        return profilePic;
    }

    public String getItemId() {
        return itemId;
    }

    public String getConvId() {
        return convId;
    }

    public String getItemName() {
        return itemName;
    }
}
