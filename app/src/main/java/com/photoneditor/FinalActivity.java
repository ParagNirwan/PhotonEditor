package com.photoneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.dsphotoeditor.sdk.activity.DsPhotoEditorActivity;
import com.dsphotoeditor.sdk.utils.DsPhotoEditorConstants;
import com.photoneditor.databinding.ActivityFinalBinding;

public class FinalActivity extends AppCompatActivity {
ActivityFinalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityFinalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Uri selectedImageUri = getIntent().getParcelableExtra("imageUri");
        Intent dsPhotoEditorIntent = new Intent(this, DsPhotoEditorActivity.class);
        dsPhotoEditorIntent.setData(selectedImageUri);
        dsPhotoEditorIntent.putExtra(DsPhotoEditorConstants.DS_PHOTO_EDITOR_OUTPUT_DIRECTORY, "Photon Editor");

        startActivityForResult(dsPhotoEditorIntent, 200);
        }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case 200:

                    Uri outputUri = data.getData();

                     binding.imageView3.setImageURI(outputUri);

                    break;

            }

        }

    }

    public static class Users {
        String id,profilepic, mail, fullname, password;
        public Users(){}

        public Users(String id, String imageuri, String emailId, String name, String passwort) {
            this.id = id;
            this.profilepic =imageuri;
            this.mail = emailId;
            this.fullname = name;
            this.password = passwort;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Users(String profilepic, String mail, String fullname, String password) {
           this.profilepic = profilepic;
           this.mail = mail;
           this.fullname = fullname;
           this.password = password;
        }

        public String getProfilepic() {
            return profilepic;
        }

        public void setProfilepic(String profilepic) {
            this.profilepic = profilepic;
        }

        public String getMail() {
            return mail;
        }

        public void setMail(String mail) {
            this.mail = mail;
        }

        public String getFullname() {
            return fullname;
        }

        public void setFullname(String fullname) {
            this.fullname = fullname;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
