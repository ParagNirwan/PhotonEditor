package com.photoneditor;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.photoneditor.databinding.ActivityFinalBinding;

public class FinalActivity extends AppCompatActivity {
ActivityFinalBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        Uri imageUri = getIntent().getParcelableExtra("imageUri");
        if (imageUri != null) {
            // Now you can use the imageUri as needed
            // For example, display the image in an ImageView
            try{
                ImageView imageView = findViewById(R.id.imageView);
                imageView.setImageURI(imageUri);
            }
            catch(Exception e){
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }
}