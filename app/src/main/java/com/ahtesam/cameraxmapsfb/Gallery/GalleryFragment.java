package com.ahtesam.cameraxmapsfb.Gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ahtesam.cameraxmapsfb.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private StorageReference userImageStorageRef;
    private StorageReference userVideoStorageRef;
    private GalleryAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = view.findViewById(R.id.gallery_list);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columnas en la cuadrícula
        adapter = new GalleryAdapter(getContext());
        recyclerView.setAdapter(adapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();

            userImageStorageRef = FirebaseStorage.getInstance().getReference().child("user_galleries").child(userId).child("images");
            userVideoStorageRef = FirebaseStorage.getInstance().getReference().child("user_galleries").child(userId).child("videos");

            importMediaUrls(userImageStorageRef, userVideoStorageRef);
        } else {
            Log.e("GalleryFragment", "No usuario obtenido.");
        }

        return view;
    }

    private void importMediaUrls(StorageReference imageStorageRef, StorageReference videoStorageRef) {
        List<String> mediaUrls = new ArrayList<>();

        importData(imageStorageRef, mediaUrls);
        importData(videoStorageRef, mediaUrls);
    }

    private void importData(StorageReference mediaRef, List<String> mediaUrls) {
        mediaRef.listAll().addOnSuccessListener(imageListResult -> {
            for (StorageReference item : imageListResult.getItems()) {
                item.getDownloadUrl().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        mediaUrls.add(task.getResult().toString());
                        adapter.setMediaUrls(mediaUrls);
                    } else {
                        Log.w("GalleryFragment", "Error URL de descarga: " + mediaRef.getName(), task.getException());
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Log.w("GalleryFragment", "Error elementos de imágenes storage: " + mediaRef.getName(), e);
        });
    }
}
