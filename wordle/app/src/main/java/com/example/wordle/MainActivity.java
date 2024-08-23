package com.example.wordle;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.wordle.databinding.ActivityMainBinding;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private EditText getEmail, getPassword;
    private String strEmail, strPassword;
    private FirebaseAuth xAuth;
    private FirebaseUser xUser;
    Button bGiris, bKayit,bGoxu,bYusuf;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Write a message to the database
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
        navView.setVisibility(View.GONE);
        //Login atlama yeri
//        Intent intent = new Intent(MainActivity.this,MainActivity2.class);
//        intent.putExtra("email","yusuf@gmail.com");
//        intent.putExtra("PlayerId","1NDtxcCZjhTbjqfb0FLKBHJpxQa2");
//        intent.putExtra("odaIsmi","4 Harfli");
//        intent.putExtra("oyunModu","sabitli");
//        finish();
//        startActivity(intent);


        //
        bGiris = (Button) findViewById(R.id.button_Girisyap);
        bKayit = (Button) findViewById(R.id.button_Kayitol);
        bGoxu = findViewById(R.id.button_goxu);
        bYusuf = findViewById(R.id.button_yusuf);
        bYusuf.setVisibility(View.GONE);
        getEmail = (EditText) findViewById(R.id.editTextTextEmailAddress);
        getPassword = (EditText) findViewById(R.id.editTextTextPassword);
        xAuth = FirebaseAuth.getInstance();
        bGiris.setOnClickListener(this);
        bKayit.setOnClickListener(this);
        bGoxu.setVisibility(View.GONE);
        bYusuf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                intent.putExtra("email","yusuf@gmail.com");
                //   intent.putExtra("oyunModu","sabitli"); // BU BURADA OLMAMALIIIIIIIIIIIIIIIIIII
                intent.putExtra("PlayerId","1NDtxcCZjhTbjqfb0FLKBHJpxQa2");
                finish();
                startActivity(intent);
            }
        });
        bGoxu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                intent.putExtra("email","goxuyusuf@gmail.com");
                //   intent.putExtra("oyunModu","sabitli"); // BU BURADA OLMAMALIIIIIIIIIIIIIIIIIII
                intent.putExtra("PlayerId","aVw3bRvZn9RhbpiJgSzxd9SCKc03");
                finish();
                startActivity(intent);
            }
        });

    }

    @Override
    public void onClick(View v) {

        if (v.getId() == bGiris.getId()) {
            strEmail = getEmail.getText().toString();
            strPassword = getPassword.getText().toString();
            if (!TextUtils.isEmpty(strEmail) && !TextUtils.isEmpty((strPassword))) {
                xAuth.signInWithEmailAndPassword(strEmail, strPassword).addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        xUser = xAuth.getCurrentUser();
                        String id = xAuth.getUid();
                        String email =  xUser.getEmail();
                        Intent intent = new Intent(MainActivity.this,MainActivity2.class);
                        intent.putExtra("email",email);
                     //   intent.putExtra("oyunModu","sabitli"); // BU BURADA OLMAMALIIIIIIIIIIIIIIIIIII
                        intent.putExtra("PlayerId",id);
                        finish();
                        startActivity(intent);
                        //NavController navController = Navigation.findNavController(MainActivity.this, R.id.nav_host_fragment_activity_main);
                        //navController.navigate(R.id.navigation_dashboard);
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else
                Toast.makeText(this, "Eposta veya şifre boş!!", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == bKayit.getId()) {
            strEmail = getEmail.getText().toString();
            strPassword = getPassword.getText().toString();
            if (!TextUtils.isEmpty(strEmail) && !TextUtils.isEmpty((strPassword))) {
                xAuth.createUserWithEmailAndPassword(strEmail, strPassword).addOnCompleteListener(
                        this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(MainActivity.this, "Kayıt işlemi başarılı!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                );
            } else
                Toast.makeText(this, "Email veya şifre boş!!", Toast.LENGTH_SHORT).show();
        }
    }

}