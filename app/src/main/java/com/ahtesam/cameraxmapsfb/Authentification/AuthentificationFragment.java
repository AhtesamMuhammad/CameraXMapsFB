package com.ahtesam.cameraxmapsfb.Authentification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.ahtesam.cameraxmapsfb.R;
import com.ahtesam.cameraxmapsfb.databinding.FragmentAuthentificationBinding;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

public class AuthentificationFragment extends Fragment implements AuthentificationManager.AuthenticationListener {

    private FragmentAuthentificationBinding binding;
    private AuthentificationManager authManager;

    private String userEmail;
    private String userPassword;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAuthentificationBinding.inflate(inflater, container, false);

        authManager = new AuthentificationManager(this);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button registrar = binding.singUpButton;
        Button iniciarSesion = binding.loginButton;

        registrar.setOnClickListener(v -> comprobacionDatosRellenados(true));
        iniciarSesion.setOnClickListener(v -> comprobacionDatosRellenados(false));

    }

    private void comprobacionDatosRellenados(boolean isRegistered) {
        userEmail = binding.editTextEmail.getText().toString();
        userPassword = binding.editTextPassword.getText().toString();

        if (userEmail.isEmpty()) {
            Toast.makeText(getActivity(), "El email está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userPassword.isEmpty()) {
            Toast.makeText(getActivity(), "La contraseña está vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isRegistered) {
            authManager.createAccount(userEmail, userPassword, getActivity());
        } else {
            authManager.signIn(userEmail, userPassword);
        }
    }

    @Override
    public void onAuthentificationSuccess(String successMessage) {
        Toast.makeText(getActivity(), successMessage, Toast.LENGTH_SHORT).show();
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_authentificationFragment_to_menuFragment);
    }

    @Override
    public void onAuthentificationFailure(String failMessage, Task task) {
        Toast.makeText(getActivity(), failMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}