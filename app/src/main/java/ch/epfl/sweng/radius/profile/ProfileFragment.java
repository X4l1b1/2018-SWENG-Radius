package ch.epfl.sweng.radius.profile;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ch.epfl.sweng.radius.R;
import ch.epfl.sweng.radius.database.DBObservable;
import ch.epfl.sweng.radius.database.DBObserver;
import ch.epfl.sweng.radius.database.Database;
import ch.epfl.sweng.radius.database.User;
import ch.epfl.sweng.radius.home.HomeFragment;
import ch.epfl.sweng.radius.database.UserInfo;
import ch.epfl.sweng.radius.utils.profileFragmentUtils.TextFileReader;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements DBObserver {
    private static int userRadius;

    CircleImageView userPhoto;
    TextView userNickname;
    TextView userStatus;
    TextInputEditText statusInput;
    TextInputEditText nicknameInput;
    SeekBar radiusBar;
    TextView radiusValue;
    MaterialButton saveButton;
    TextView userInterests;
    TextInputEditText interestsInput;

    private Button selectLanguagesButton;
    private static ArrayList<String> selectableLanguages;
    private static boolean[] checkedLanguages;
    private static ArrayList<Integer> spokenLanguages;
    private static TextView selectedLanguages;
    private static String languagesText;

    public ProfileFragment() {
        spokenLanguages = new ArrayList<Integer>();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance() { // currently useless
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get the UI elements
        radiusBar = view.findViewById(R.id.radiusBar);
        radiusValue = view.findViewById(R.id.radiusValue);
        selectedLanguages =  view.findViewById(R.id.spokenLanguages);
        selectLanguagesButton = view.findViewById(R.id.languagesButton);
        userStatus = view.findViewById(R.id.userStatus);
        userNickname = view.findViewById(R.id.userNickname);
        userInterests = view.findViewById(R.id.userInterests);
        nicknameInput = view.findViewById(R.id.nicknameInput);
        statusInput = view.findViewById(R.id.statusInput);
        interestsInput = view.findViewById(R.id.interestsInput);
        saveButton = view.findViewById(R.id.saveButton);
        userPhoto = view.findViewById(R.id.userPhoto);

        // set a change listener on the SeekBar
        radiusBar.setOnSeekBarChangeListener(seekBarChangeListener);

        // Load the selectableLanguages
        selectableLanguages = TextFileReader.readLanguagesFromFile(getActivity());

        // Languages selection button
        checkedLanguages = new boolean[selectableLanguages.size()];
        selectLanguagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBuilder();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSaveButton();
            }
        });

        // Load the users info and display
        setUpInfos();

        // Listen to changes in the DB
        UserInfo.getInstance().addObserver(this);

        // Inflate the layout for this fragment
        return view;
    }

    public void setUpInfos(){
        //Get the current user
        User current_user = UserInfo.getInstance().getCurrentUser();

        // Fill the labels with the user info
        userNickname.setText(current_user.getNickname());
        userStatus.setText(current_user.getStatus());
        userInterests.setText(current_user.getInterests());
        selectedLanguages.setText(current_user.getSpokenLanguages());
        radiusValue.setText(current_user.getRadius() + "Km");
        radiusBar.setProgress(current_user.getRadius());

        setUpProfilePhoto();
    }

    private void createBuilder() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Pick the languages you speak");
        setBuilderMultiChoiceItems(builder);
        builder.setCancelable(true);
        setBuilderPositiveButton(builder);
        setBuilderNegativeButton(builder);
        setBuilderNeutralButton(builder);

        AlertDialog languageDialog = builder.create();
        languageDialog.show();
    }

    private void setBuilderMultiChoiceItems(AlertDialog.Builder builder) {
        builder.setMultiChoiceItems(selectableLanguages
                        .toArray(new String[selectableLanguages.size()])
                , checkedLanguages, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int position, boolean isChecked) {
                        if (isChecked) {
                            if (!spokenLanguages.contains(position)) {
                                spokenLanguages.add(new Integer(position));
                            }
                        } else if (spokenLanguages.contains(position)) {
                            spokenLanguages.remove(new Integer(position));
                        }
                    }
                });
    }

    private void setUpProfilePhoto() {
        User current_user = UserInfo.getInstance().getCurrentUser();
        byte[] decodedString = Base64.decode(current_user.getUrlProfilePhoto(), Base64.DEFAULT);
        Bitmap profilePictureUri = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        if (profilePictureUri != null) {
            userPhoto.setImageBitmap(profilePictureUri);
        }

        userPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent,
                        "Select Profile Picture"), 1);

            }
        });
    }

    private void setBuilderPositiveButton(AlertDialog.Builder builder) {
        builder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String languagesText = UserInfo.getInstance().getCurrentUser().getSpokenLanguages();
                for (int i = 0; i < spokenLanguages.size() ; i++) {
                    if (!languagesText.contains(selectableLanguages.get(spokenLanguages.get(i)))) {
                        languagesText = languagesText + " " +selectableLanguages.get(spokenLanguages.get(i));
                        if (i != spokenLanguages.size() - 1) {
                            languagesText = languagesText + " ";
                        }
                    }
                }
                selectedLanguages.setText(languagesText);
            }
        });
    }

    private void setBuilderNegativeButton(AlertDialog.Builder builder) {
        builder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
    }

    private void setBuilderNeutralButton(AlertDialog.Builder builder) {
        builder.setNeutralButton(R.string.clearAll_label, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < checkedLanguages.length; i++) {
                    checkedLanguages[i] = false;
                }
                spokenLanguages.clear();
                languagesText = "";
                selectedLanguages.setText(languagesText);
            }
        });
    }


    private void onClickSaveButton() {
        String nicknameString = getDataFromTextInput(nicknameInput);
        String statusString = getDataFromTextInput(statusInput);
        String interestsString = getDataFromTextInput(interestsInput);

        User currentUser = UserInfo.getInstance().getCurrentUser();

        if (!nicknameString.isEmpty()) {
            currentUser.setNickname(nicknameString);
            userNickname.setText(nicknameString);
        }
        if (!statusString.isEmpty()) {
            currentUser.setStatus(statusString);
            userStatus.setText(statusString);
        }

        if (!interestsString.isEmpty()) {
            currentUser.setInterests(interestsString);
            userInterests.setText("Interests: " + interestsString);
        }

        currentUser.setRadius(userRadius);
        currentUser.setSpokenLanguages(languagesText);
        //Write to DB
        Database.getInstance().writeInstanceObj(currentUser, Database.Tables.USERS);
    }

    private String getDataFromTextInput(TextInputEditText input) {
        if (input != null) {
            Editable inputText = input.getText();
            if (inputText != null) {
                return inputText.toString();
            }
        }
        return "";
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && requestCode == 1) {
            Uri imageUri = intent.getData();
            userPhoto.setImageURI(imageUri);

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getActivity().getContentResolver(), imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); //"bitmap" is the bitmap object
                String encodedImage = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

                UserInfo.getInstance().getCurrentUser().setUrlProfilePhoto(encodedImage);
                Database.getInstance().writeInstanceObj(UserInfo.getInstance().getCurrentUser(), Database.Tables.USERS);

            } catch (IOException e) {
            }

        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            // updated continuously as the user slides the thumb
            radiusValue.setText(progress + " Km");
            userRadius = progress;

            HomeFragment.newInstance(radiusBar.getProgress());
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // called when the user first touches the SeekBar
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // called after the user finishes moving the SeekBar
        }
    };

    public String getLanguagesText() {
        return languagesText;
    }

    public void onDataChange(String id){
        setUpInfos();
    }
}