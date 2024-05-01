package com.ahtesam.cameraxmapsfb.Authentification;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AuthentificationManager {

    private final FirebaseAuth auth;
    private final AuthenticationListener listener;
    private String TAG = "AuthentificationManager";

    public interface AuthenticationListener {
        void onAuthentificationSuccess(String successMessage);
        void onAuthentificationFailure(String failMessage, Task task);
    }

    public AuthentificationManager(AuthenticationListener listener) {
        this.auth = FirebaseAuth.getInstance();
        this.listener = listener;
    }

    public void createAccount(String email, String password, Activity activity) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onAuthentificationSuccess("Creacion de la cuenta exitosa");
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            listener.onAuthentificationFailure("La cuenta ya existe para ese correo electrónico.", task);
                        } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            listener.onAuthentificationFailure("El correo electrónico es inválido.", task);
                        } else {
                            listener.onAuthentificationFailure("Registro fallido desconocido", task);
                        }
                    }
                });
    }

    public void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        createImageAndVideoFoldersForUser(auth.getUid());
                        listener.onAuthentificationSuccess("Inicio de sesión exitoso.");

                    } else {
                        String errorMessage = getErrorMessage(task);
                        listener.onAuthentificationFailure(errorMessage, task);
                    }
                });
    }

    @NonNull
    private static String getErrorMessage(Task<AuthResult> task) {
        String errorMessage = "Inicio de sesión fallido: ";
        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage += "Credenciales inválidas.";
        } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
            errorMessage += "Usuario no encontrado.";
        } else {
            errorMessage += "Error desconocido.";
        }
        return errorMessage;
    }

    private void createImageAndVideoFoldersForUser(String userId) {
        StorageReference userGalleryRef = FirebaseStorage.getInstance().getReference().child("user_galleries").child(userId);
        StorageReference imagesRef = userGalleryRef.child("images/");
        imagesRef.putBytes(new byte[]{})
                .addOnSuccessListener(taskSnapshot1 -> {
                    Log.d(TAG, "Images folder created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating images folder", e);
                });

        StorageReference videosRef = userGalleryRef.child("videos/");
        videosRef.putBytes(new byte[]{})
                .addOnSuccessListener(taskSnapshot2 -> {
                    Log.d(TAG, "Videos folder created successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating videos folder", e);
                });
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
}