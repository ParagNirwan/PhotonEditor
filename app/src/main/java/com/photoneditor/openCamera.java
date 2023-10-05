package com.photoneditor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class openCamera extends AppCompatActivity {

    private static final String TAG = "OpenCameraX";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA,  Manifest.permission.READ_EXTERNAL_STORAGE};

    private PreviewView cameraPreview;
    private ImageButton captureButton;
    private ImageButton toggleFlash;
    private ImageButton flipCamera;
    private File outputDirectory;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private boolean isFlashOn = false;
    private int currentCameraLensFacing = CameraSelector.LENS_FACING_BACK;
    private LocationManager locationManager;
    private LocationListener locationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);


        //Location
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Handle location updates if needed
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Handle location provider status changes if needed
            }

            @Override
            public void onProviderEnabled(String provider) {
                // Handle location provider enabled if needed
            }

            @Override
            public void onProviderDisabled(String provider) {
                // Handle location provider disabled if needed
            }
        };

        cameraPreview = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.capture);
        toggleFlash = findViewById(R.id.toggleFlash);
        outputDirectory = getOutputDirectory();
flipCamera = findViewById(R.id.flipCamera);
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing camera", e);
            }
        }, ContextCompat.getMainExecutor(this));

        captureButton.setOnClickListener(view -> {
            takePhoto();
        });

        toggleFlash.setOnClickListener(view -> {
            toggleFlash();
        });

        flipCamera.setOnClickListener(view -> {
            // Toggle between front and back camera
            currentCameraLensFacing = (currentCameraLensFacing == CameraSelector.LENS_FACING_BACK) ?
                    CameraSelector.LENS_FACING_FRONT : CameraSelector.LENS_FACING_BACK;

            // Unbind the camera use cases first
            try {
                unbindCameraUseCases(cameraProviderFuture.get());
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Rebind camera use cases with the new camera selector
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindCameraUseCases(cameraProvider);
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing camera", e);
                }
            }, ContextCompat.getMainExecutor(this));
        });

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private File getOutputDirectory() {

        File mediaDir = new File(getExternalMediaDirs()[0], "PhotonEditorCaptures");
        mediaDir.mkdirs();
        return mediaDir;
    }

    private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        // Use the currentCameraLensFacing to select the desired camera
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(currentCameraLensFacing)
                .build();

        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getDisplayRotation())
                .build();

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageCapture);
    }

    private int getDisplayRotation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        return degrees;
    }

    private void takePhoto() {
        String x = "Parag";
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.MediaColumns.DISPLAY_NAME,x);
        cv.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");



        File photoFile = new File(outputDirectory, System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputFileOptions, executorService, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                File savedPhoto = outputFileResults.getSavedUri() != null ?
                        new File(outputFileResults.getSavedUri().getPath()) : photoFile;

                // Get the last known location
                Location lastKnownLocation = getLastKnownLocation();

                // Add location information to the saved photo
                if (lastKnownLocation != null) {
                    addLocationToImage(savedPhoto, lastKnownLocation);
                }

                runOnUiThread(() -> {
                    Toast.makeText(openCamera.this, "Image Captured",
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
            }
        });
    }

    private void toggleFlash() {
        isFlashOn = !isFlashOn;
        imageCapture.setFlashMode(
                isFlashOn ? ImageCapture.FLASH_MODE_ON : ImageCapture.FLASH_MODE_OFF
        );
        int flashIconResource = isFlashOn ? R.drawable.baseline_flash_on_24 : R.drawable.baseline_flash_off_24;
        toggleFlash.setImageResource(flashIconResource);
    }
    private Location getLastKnownLocation() {
        Location lastKnownLocation = null;
        try {
            // Check for permission here if needed
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Request the last known location from the location manager
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return lastKnownLocation;
    }
    private void unbindCameraUseCases(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
    }
    private void addLocationToImage(File imageFile, Location location) {
        try {

            //
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertLocationToDMS(latitude));
            exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertLocationToDMS(longitude));

            if (latitude >= 0) {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
            } else {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
            }

            if (longitude >= 0) {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
            } else {
                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
            }

            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String convertLocationToDMS(double location) {
        location = Math.abs(location);
        int degrees = (int) location;
        location = (location - degrees) * 60;
        int minutes = (int) location;
        location = (location - minutes) * 60;
        int seconds = (int) (location * 1000);

        return degrees + "/1," + minutes + "/1," + seconds + "/1000";
    }
}
