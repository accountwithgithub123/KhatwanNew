package com.example.khatwan;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.firebase.ui.auth.AuthUI;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Collections;
import java.util.List;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity {

    private static final int REQ_CODE = 1;
    MaterialButton btnGoogle,btnSignIn;
    EditText etemail,etpass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences preferences = getSharedPreferences("Loged",MODE_PRIVATE);
        boolean isloged =  preferences.getBoolean("isloged",false);
        if (isloged){
            startActivity(new Intent(SplashScreen.this,MainActivity.class));
        }
        btnGoogle = findViewById(R.id.btngoogle);
        btnSignIn = findViewById(R.id.btnLogin);
        etemail = findViewById(R.id.etnamech);
        etpass = findViewById(R.id.etNewPassch);

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        windowInsetsController.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars());
        windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars());
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars());


        //if (ContextCompat.checkSelfPermission(SplashScreen.this,Manifest.permission.WRITE_EXTERNAL_STORAGE))
        btnGoogle.setOnClickListener(v -> {
            List<AuthUI.IdpConfig> providers = Collections.singletonList(
                    new AuthUI.IdpConfig.GoogleBuilder().build());

            // Create and launch sign-in intent
            Intent signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(providers)
                    .build().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(signInIntent,REQ_CODE);
        });
        btnSignIn.setOnClickListener(v -> {
            try {
                if (allDataCorrect()){
                    btnSignIn.setEnabled(false);
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(etemail.getText().toString().trim(), etpass.getText().toString().trim()).addOnSuccessListener(authResult -> {
                        kepUserLogged();
                        startActivity(new Intent(SplashScreen.this, MainActivity.class));
                        finish();
                    })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            btnSignIn.setEnabled(true);
                        });
                }
            }
            catch (Exception e){
                Log.e(TAG, "SignInFragment : " + e.getMessage() );
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_CODE){
            if (resultCode==RESULT_OK){
                kepUserLogged();
                startActivity(new Intent(SplashScreen.this,MainActivity.class));
                finish();
            }
            else{
                Toast.makeText(this, "Request cancelled!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void kepUserLogged() {
        SharedPreferences.Editor editor = getSharedPreferences("Loged",MODE_PRIVATE).edit();
        editor.putBoolean("isloged",true);
        editor.apply();
    }
    private boolean allDataCorrect() {
        String email = etemail.getText().toString().trim();
        String pass = etpass.getText().toString().trim();
        if (email.isEmpty()){
            etemail.setError("Please enter email address!");
            etemail.requestFocus();
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etemail.setError("Invalid email address!");
            etemail.requestFocus();
        }
        else if (pass.isEmpty()){
            etpass.setError("Please enter password!");
            etpass.requestFocus();
        }
        else
            return true;
        return false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}