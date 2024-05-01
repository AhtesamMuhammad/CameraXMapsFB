package com.ahtesam.cameraxmapsfb;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class LocationFragment extends Fragment {

    private GoogleMap googleMap;
    private final List<LatLng> locationList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            downloadUserLocationMetadata();
            mapFragment.getMapAsync(this::onMapReady);
        } else
            Log.e("mapFragment Error", "mapFragment is null");

    }

    private void onMapReady(GoogleMap map) {
        googleMap = map;

        for (LatLng location : locationList) {
            googleMap.addMarker(new MarkerOptions().position(location));
        }
    }

    private void downloadUserLocationMetadata() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            downloadLocationMetadata(userId);
        } else {
            Log.e("CurrentUserError", "Current user is null");
        }
    }

    private void downloadLocationMetadata(String userId) {
        downloadMetadataFromStorage("images", userId);
        downloadMetadataFromStorage("videos", userId);
    }

    private void downloadMetadataFromStorage(String folderName, String userId) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("user_galleries").child(userId).child(folderName);

        storageRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                processMetadata(item);
            }
        }).addOnFailureListener(exception -> {
            Log.e("StorageListError:", "Error en " + folderName + ": " + exception.getMessage());
        });
    }

    private void processMetadata(StorageReference item) {
        item.getMetadata().addOnSuccessListener(storageMetadata -> {
            String locationString = storageMetadata.getCustomMetadata("location");

            if (locationString != null) {
                LatLng location = parseLocationString(locationString);
                if (location != null) {
                    locationList.add(location);
                } else {
                    Log.e("LocationParseError", "Error location string: " + locationString);
                }
            } else {
                Log.e("LocationStringError", "Location metadata is null");
            }

            if (googleMap != null) {
                googleMap.clear();
                for (LatLng location : locationList) {
                    googleMap.addMarker(new MarkerOptions().position(location));
                }
            }

            if (!locationList.isEmpty()) {
                LatLng lastLocation = locationList.get(locationList.size() - 1);
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 12));
            }

        }).addOnFailureListener(exception -> {
            Log.e("MetadataError", "Error getting metadata: " + exception.getMessage());
        });
    }

    private LatLng parseLocationString(String locationString) {
        String[] locationArray = locationString.split(",");
        if (locationArray.length == 2) {
            double latitude = Double.parseDouble(locationArray[0].trim());
            double longitude = Double.parseDouble(locationArray[1].trim());
            return new LatLng(latitude, longitude);
        } else {
            return null;
        }
    }

}