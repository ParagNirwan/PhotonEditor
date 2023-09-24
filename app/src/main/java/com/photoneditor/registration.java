package com.photoneditor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class registration extends AppCompatActivity {
    Button button;
    EditText fullname, email, password, retypePassword;
    ImageView imageView;
    FirebaseAuth auth;

    TextView AccountAlready;
    FirebaseDatabase database;
    FirebaseStorage storage;
    Uri imageURI = null;
    String imageuri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        fullname = findViewById(R.id.editTextName);
        email = findViewById(R.id.editTextTextEmailAddress);
        password = findViewById(R.id.editTextTextPassword);
        retypePassword = findViewById((R.id.editTextTextPassword2));
        button = findViewById(R.id.button);
        AccountAlready = findViewById(R.id.AccountAlready);
        imageView = findViewById(R.id.imageView);


        //Image picker
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent.createChooser(intent,"Select image"),200);
            }
        });



        //login button Code
        AccountAlready.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(registration.this, login.class);
                startActivity(intent);
                finish();
            }
        });

        //Signup Button code
        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String name = fullname.getText().toString();
                String emailId = email.getText().toString();
                String passwort = password.getText().toString();
                String rePasswort = retypePassword.getText().toString();
                auth = FirebaseAuth.getInstance();
                storage = FirebaseStorage.getInstance();
                database = FirebaseDatabase.getInstance();

                String emailRegex = "[A-Za-z0-9+_-]+@[a-z]+\\.+[a-z]";
                if(TextUtils.isEmpty(name) || TextUtils.isEmpty(emailId) || TextUtils.isEmpty(passwort) ||TextUtils.isEmpty(rePasswort)){
                    Toast.makeText(registration.this, "All fields are required",Toast.LENGTH_SHORT).show();
                }
                else if(emailId.matches(emailRegex)){
                    email.setError("Invalid email");
                } else if (passwort.length()<7) {
                    password.setError("Password must be more than 7 characters");

                }
                else if(!rePasswort.matches(passwort)){
                    retypePassword.setError("Passwords don't match");
                }else {
                    auth.createUserWithEmailAndPassword(emailId,passwort).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                String id = task.getResult().getUser().getUid();
                                DatabaseReference reference = database.getReference().child("user").child(id);
                                StorageReference storageReference = storage.getReference().child("Upload").child(id);

                                if(imageURI!=null){
                                    storageReference.putFile(imageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            if(task.isSuccessful()){
                                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        imageuri = uri.toString();
                                                        Users users = new Users(id,imageuri,emailId,name,passwort);
                                                        reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(registration.this,"Registration successful. Login to continue.",Toast.LENGTH_LONG).show();
                                                                    Intent intent = new Intent(registration.this,login.class);
                                                                    startActivity(intent);
                                                                    finish();

                                                                }else{
                                                                    Toast.makeText(registration.this,"Some error occured.",Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                });

                                            }
                                        }
                                    });
                                }else {
                                    imageuri = "https://firebasestorage.googleapis.com/v0/b/mc-home-a8367.appspot.com/o/user.png?alt=media&token=7c324fb4-f2b7-4ec4-8ffc-6a0d1c78266b";
                                    Users users = new Users(id,imageuri,emailId,name,passwort);
                                    reference.setValue(users).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){

                                                Intent intent = new Intent(registration.this,MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }else{
                                                Toast.makeText(registration.this,"Some Error Occured",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                                Toast.makeText(registration.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode,@Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==200){
            if(data!= null){
                imageURI = data.getData();
                imageView.setImageURI(imageURI);
            }
        }
    }

}