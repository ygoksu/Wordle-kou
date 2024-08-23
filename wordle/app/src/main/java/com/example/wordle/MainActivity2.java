package com.example.wordle;

import android.content.Intent;
import android.icu.text.SymbolTable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Clock;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    private  String email;
    private  String id;
    private TextView txtEmail;
    private Button sabitli;
    private Button sabitsiz;
    List<String> roonmsList;
    FirebaseDatabase db;
    DatabaseReference roomRef;
    DatabaseReference roomsRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        id = intent.getStringExtra("PlayerId");
        txtEmail = (TextView) findViewById(R.id.textView3);
        txtEmail.setText(email + " Hoşgeldiniz.");
        sabitli  = findViewById(R.id.sabitli);
        sabitsiz  = findViewById(R.id.sabitsiz);


        sabitli.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Harf sabitli oyuna giriliyor.");
                oyunModuSec("sabitli");
            }
        });
        sabitsiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("Harf sabitsiz oyuna giriliyor.");
                oyunModuSec("sabitsiz");
            }
        });
    }

    private void oyunModuSec(String oyunModu) {
        Intent intent = new Intent(MainActivity2.this,GameActivity.class);// BURASI DÜZELTİLECEK
        intent.putExtra("email",email);
        intent.putExtra("oyunModu",oyunModu);
        intent.putExtra("PlayerId",id);
//        intent.putExtra("odaIsmi","4 Harfli");// BURASI DÜZELTİLECEK
//        intent.putExtra("secondPlayerId","aVw3bRvZn9RhbpiJgSzxd9SCKc03");
        finish();
        startActivity(intent);
    }
}