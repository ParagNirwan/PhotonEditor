package com.photoneditor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class openCamera extends AppCompatActivity {
    private static final String TAG = "openCamera";
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION};

    private PreviewView cameraPreview;
    private ImageButton captureButton,toggleFlash, flipCamera;
    private LocationManager locationManager;
    private Location currentLocation;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private File outputDirectory;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);

        cameraPreview = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.capture);

        outputDirectory = getOutputDirectory();

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        // Initialize Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Request location updates continuously until a location is available
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener, Looper.getMainLooper());
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing camera", e);
            }
        }, ContextCompat.getMainExecutor(this));

        captureButton.setOnClickListener(view -> {
            takePhoto();
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
        File mediaDir = new File(getExternalMediaDirs()[0], "Photon Editor");
        mediaDir.mkdirs();
        return mediaDir;
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        // Set the surface provider on the preview
        preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

        imageCapture = new ImageCapture.Builder()
                .setTargetRotation(getDisplayRotation())
                .build();

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
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

    // LocationListener to get GPS coordinates
    private final LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                currentLocation = location;
                // Remove location updates as we have a valid location
                locationManager.removeUpdates(this);
            } else {
                currentLocation = location;
                Log.e(TAG, "Location is null");
            }
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                currentLocation = null;
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            // GPS provider enabled
        }

        @Override
        public void onProviderDisabled(String provider) {
            currentLocation = null;
        }
    };

    private void takePhoto() {
        File photoFile = new File(outputDirectory, System.currentTimeMillis() + ".jpg");

        ImageCapture.OutputFileOptions outputFileOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Add GPS location data to the captured image
        if (currentLocation != null) {
            imageCapture.takePicture(outputFileOptions, executorService,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                            // Add GPS location to the saved image
                            try {
                                ExifInterface exifInterface = new ExifInterface(photoFile.getAbsolutePath());
                                exifInterface.setAttribute(ExifInterface.TAG_GPS_LATITUDE, Double.toString(currentLocation.getLatitude()));
                                exifInterface.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, Double.toString(currentLocation.getLongitude()));
                                exifInterface.saveAttributes();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            runOnUiThread(() -> {
                                Toast.makeText(openCamera.this, "Photo saved: " + photoFile.getAbsolutePath(),
                                        Toast.LENGTH_LONG).show();
                            });
                        }

                        @Override
                        public void onError(@NonNull ImageCaptureException exception) {
                            Log.e(TAG, "Photo capture failed: " + exception.getMessage(), exception);
                        }
                    });
        } else {
            runOnUiThread(() -> {
                Toast.makeText(openCamera.this, "Photo saved: " + photoFile.getAbsolutePath(),
                        Toast.LENGTH_LONG).show();
            });
            // Handle the case when GPS location is not available
            Log.e(TAG, "GPS location not available");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
