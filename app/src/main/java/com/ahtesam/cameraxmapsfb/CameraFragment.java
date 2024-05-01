package com.ahtesam.cameraxmapsfb;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.MediaStoreOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoRecordEvent;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.ahtesam.cameraxmapsfb.databinding.FragmentCameraBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraFragment extends Fragment {

    private static final String[] REQUIRED_PERMISSIONS = {
            android.Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss";
    private final String FILENAME = new SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis());
    private static final String TAG = "CameraApp";
    private static final String DESK = " DESC";


    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;

    //Firebase
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    public FusedLocationProviderClient fusedLocationClient;


    private ImageView videoBinding;
    private ImageView imageBinding;
    private FragmentCameraBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        imageBinding = binding.imageCaptureButton;
        videoBinding = binding.videoCaptureButton;

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageBinding.setOnClickListener(v -> takePhoto());
        videoBinding.setOnClickListener(v -> captureVideo());

        ExecutorService cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private Context safeContext() {
        if (getContext() == null) {
            throw new IllegalStateException("Context is null");
        }
        return getContext();
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(safeContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        safeContext();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(safeContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(binding.viewFinder.getSurfaceProvider());

                Recorder recorder = new Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build();

                videoCapture = VideoCapture.withOutput(recorder);
                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, videoCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Use case binding failed", e);
            }
        }, ContextCompat.getMainExecutor(safeContext()));
    }

    private void takePhoto() {
        ImageCapture imageCapture = this.imageCapture;

        if (imageCapture == null) return;

        ContentValues contentValues = createMediaContentValues(FILENAME, "image/jpeg");
        ImageCapture.OutputFileOptions outputOptions = createImageOutputOptions(contentValues);

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(safeContext()), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(safeContext(), "Photo capture succeeded", Toast.LENGTH_SHORT).show();
                loadLastImagePreview();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exc) {
                Log.e(TAG, "Photo capture failed: " + exc.getMessage(), exc);
            }
        });
    }

    private ImageCapture.OutputFileOptions createImageOutputOptions(ContentValues contentValues) {
        return new ImageCapture.OutputFileOptions.Builder(safeContext().getContentResolver(),
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                .build();
    }

    private MediaStoreOutputOptions createVideoOutputOptions(ContentValues contentValues) {
        return new MediaStoreOutputOptions.Builder(safeContext().getContentResolver(), MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build();
    }

    private void captureVideo() {
        VideoCapture<Recorder> videoCapture = this.videoCapture;
        if (videoCapture == null) {
            Log.e(TAG, "VideoCapture is null");
            return;
        }

        videoBinding.setEnabled(false);

        Recording curRecording = recording;
        if (curRecording != null) {
            curRecording.stop();
            recording = null;
            return;
        }

        ContentValues contentValues = createMediaContentValues(FILENAME, "video/mp4");
        MediaStoreOutputOptions mediaStoreOutputOptions = createVideoOutputOptions(contentValues);

        if (ActivityCompat.checkSelfPermission(safeContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        recording = videoCapture.getOutput()
                .prepareRecording(safeContext(), mediaStoreOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(safeContext()), recordEvent -> {
                    if (recordEvent != null) {
                        if ((VideoRecordEvent) recordEvent instanceof VideoRecordEvent.Start) {
                            videoBinding.setImageResource(R.drawable.stop_circle_vector);
                        } else if ((VideoRecordEvent) recordEvent instanceof VideoRecordEvent.Finalize) {
                            VideoRecordEvent.Finalize finalizeEvent = (VideoRecordEvent.Finalize) recordEvent;
                            if (!finalizeEvent.hasError()) {
                                Toast.makeText(safeContext(), "Video capture succeeded", Toast.LENGTH_SHORT).show();
                                loadLastVideoPreview();
                                Uri videoUri = finalizeEvent.getOutputResults().getOutputUri();
                                getLocationAndUploadMedia(videoUri, "video");
                            } else {
                                recording.close();
                                recording = null;
                                Log.e(TAG, "Video capture ends with error: " + finalizeEvent.getError());
                            }
                            videoBinding.setImageResource(R.drawable.videocam_vector);
                        }
                        videoBinding.setEnabled(true);
                    }
                });
    }

    private void uploadMediaToStorage(Uri mediaUri, String type, String format, String location, String userId) {

        StorageReference storageRef = storage.getReference().child("user_galleries").child(userId).child(type);
        String mediaName = type + "_" + FILENAME + format;
        StorageReference mediaRef = storageRef.child(mediaName);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata("location", location)
                .build();

        mediaRef.putFile(mediaUri, metadata)
                .addOnSuccessListener(taskSnapshot -> Log.d(TAG, "Media upload successfully to Firebase"))
                .addOnFailureListener(exception -> Log.e(TAG, "Error uploading the media to Firebase: " + exception.getMessage()));

    }

    private ContentValues createMediaContentValues(String nameMedia, String format) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nameMedia);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, format);
        return contentValues;
    }

    private void loadLastImagePreview() {
        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Images.Media._ID};
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + DESK;

        @SuppressLint("Recycle")
        Cursor cursor = safeContext().getContentResolver().query(mediaUri, projection, null, null, sortOrder);

        if (!isCursorNull(cursor)) {
            @SuppressLint("Range") long mediaId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            Uri contentUri = Uri.withAppendedPath(mediaUri, String.valueOf(mediaId));
            binding.previewGallery.setImageURI(contentUri);
            getLocationAndUploadMedia(contentUri, "image");
        } else {
            Log.e(TAG, "Do not find any image");
        }
    }

    private void loadLastVideoPreview() {
        Uri mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};
        String sortOrder = MediaStore.Video.Media.DATE_TAKEN + DESK;

        @SuppressLint("Recycle") Cursor cursor = safeContext().getContentResolver().query(mediaUri, projection, null, null, sortOrder);

        if (!isCursorNull(cursor)) {
            @SuppressLint("Range") String videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));

            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoPath);

            Bitmap firstFrame = retriever.getFrameAtTime();

            binding.previewGallery.setImageBitmap(firstFrame);
        } else {
            Log.e(TAG, "Do not find any videos");
        }
    }

    private Boolean isCursorNull(Cursor cursor) {
        if (cursor != null && cursor.moveToFirst())
            return false;
        else return true;
    }

    private void getLocationAndUploadMedia(Uri mediaUri, String type) {

        if (ActivityCompat.checkSelfPermission(safeContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(safeContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String locationString = String.format(Locale.getDefault(), "%f,%f", latitude, longitude);

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    String mediaType = type.equals("image") ? "images" : "videos";
                    uploadMediaToStorage(mediaUri, mediaType, type.equals("image") ? ".jpg" : ".mp4", locationString, userId);
                }
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Error getting location: " + e.getMessage()));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}