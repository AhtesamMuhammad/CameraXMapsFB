package com.ahtesam.cameraxmapsfb;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_menu, container, false);

        view.findViewById(R.id.btnMap).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_menuFragment_to_locationFragment);
        });

        view.findViewById(R.id.btnCamera).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_menuFragment_to_CameraFragment);
        });

        view.findViewById(R.id.btnGallery).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_menuFragment_to_imageListFragment);
        });

        return view;
    }
}
