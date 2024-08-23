package com.example.wordle;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.gridlayout.widget.GridLayout;

import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class preGameActivity extends AppCompatActivity {
    private String email, oyunModu, PlayerId, OdaIsmi, secondPlayerId, kelime = "";
    private TextView timerText;
    private Button onayla;
    private List<EditText> editTextList = new ArrayList<>();

    private int kalansure = 0 ;
    FirebaseDatabase db;
    DatabaseReference ref, ref2;
    ValueEventListener valueEventListener, valueEventListener2;
    private int rastgeleSayi = -1;
    private char rastgeleHarf;
    private CountDownTimer countDownTimer;
    private ProgressDialog progressDialog = null;

    private void startTimer(long extraTimeInMillis) {
        // Ekstra süreyi ekleyerek geri sayımı başlat
        countDownTimer = new CountDownTimer(extraTimeInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                kalansure = secondsRemaining;
                timerText.setText(String.valueOf(secondsRemaining));
            }

            @Override
            public void onFinish() {
                // kalan süreyi bir değişkene ata
                timerText.setText("0");
                // Ek süre de biterse
                Toast.makeText(preGameActivity.this, "İki taraf da kelime bulamadı. ", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(preGameActivity.this, GameActivity.class);
                intent.putExtra("email", email);
                intent.putExtra("oyunModu", oyunModu);
                intent.putExtra("PlayerId", PlayerId);
                finish();
                startActivity(intent);
            }
        };
        // Geri sayımı başlat

        countDownTimer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pre_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        db = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        PlayerId = intent.getStringExtra("PlayerId");
        oyunModu = intent.getStringExtra("oyunModu");
        secondPlayerId = intent.getStringExtra("secondPlayerId");
        OdaIsmi = intent.getStringExtra("odaIsmi");
        ref = db.getReference(OdaIsmi + "/" + oyunModu + "/" + PlayerId);
        ref2 = db.getReference(OdaIsmi + "/" + oyunModu + "/" + secondPlayerId);
        ref.child("oyunBittiMi").setValue("Hayır");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("kelime"))
                    ref.child("kelime").removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        char firstChar = OdaIsmi.charAt(0);
        int OdaNo = Character.getNumericValue(firstChar);
        onayla = findViewById(R.id.button);
        timerText = findViewById(R.id.timerText);
        long millisInFuture = 60000; // 60 saniye (60 * 1000 millisaniye)
        long countDownInterval = 1000; // 1 saniye (1000 millisaniye)

        countDownTimer = new CountDownTimer(millisInFuture, countDownInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Her bir zaman dilimi değiştiğinde burası çağrılır
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                kalansure = secondsRemaining;
                timerText.setText(String.valueOf(secondsRemaining));

                if (secondsRemaining == 0) {
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.hasChild("kelime")) {
                                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (!dataSnapshot.hasChild("kelime")) {
                                            cancel(); // Geri sayımı durdur
                                            startTimer(60000); // Bir dakika ek süreli geri sayımı başlat
                                        } else {
                                            ref.child("kelimebulunamadi").setValue("");
                                            Toast.makeText(preGameActivity.this, "Rakibiniz kelime buldu! Kaybettiniz...", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(preGameActivity.this, MainActivity2.class);
                                            intent.putExtra("email", email);
                                            intent.putExtra("oyunModu", oyunModu);
                                            intent.putExtra("PlayerId", PlayerId);
                                            finish();
                                            startActivity(intent);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("Firebase", "Veritabanı işlemi iptal edildi: " + databaseError.getMessage());
                                    }
                                });
                            } else {
                                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot2) {
                                        if (!dataSnapshot2.hasChild("kelime")) {
                                            Toast.makeText(preGameActivity.this, "Rakibiniz kelime bulamadı kazandınızz!!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(preGameActivity.this, MainActivity2.class);
                                            intent.putExtra("email", email);
                                            intent.putExtra("oyunModu", oyunModu);
                                            intent.putExtra("PlayerId", PlayerId);
                                            finish();
                                            startActivity(intent);
                                        }
                                        //iki taraf da buldu oyun başlatılıyor

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("Firebase", "Veritabanı işlemi iptal edildi: " + databaseError.getMessage());
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Veritabanı işleminde hata oluştuğunda burası çağrılır
                            Log.e("Firebase", "Veritabanı işlemi iptal edildi: " + databaseError.getMessage());
                        }
                    });


                }
            }

            @Override
            public void onFinish() {
                // Geri sayım tamamlandığında burası çağrılır
                timerText.setText("0");
                // İşlemlerinizi buraya ekleyin
            }
        };
        // Geri sayımı başlat
        countDownTimer.start();


        onayla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < OdaNo; i++) {
                    if (editTextList.get(i).getText().toString().isEmpty()) {
                        Toast.makeText(preGameActivity.this, "Kelimeyi tamamlayınız!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (i == OdaNo - 1) {

                        for (int j = 0; j < OdaNo; j++)
                            kelime += editTextList.get(j).getText().toString();
                        Log.d("kelime", kelime);
                        boolean kelimeVarMi = false;
                        try (InputStream inputStream = getAssets().open("turkce_kelimeler.txt");
                             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {

                            String line;
                            while ((line = br.readLine()) != null) {
                                line = line.trim();
                                line = line.toLowerCase();
                                if (line.equals(kelime.toLowerCase())) {
                                    System.out.println(line);
                                    kelimeVarMi = true;
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (kelimeVarMi) {
                            Log.d("Kelimeler", "Dosya içerisinde " + kelime + " bulunmaktadır.");
                            ref = db.getReference(OdaIsmi + "/" + oyunModu + "/" + PlayerId);
                            ref2 = db.getReference(OdaIsmi + "/" + oyunModu + "/" + secondPlayerId);
                            System.out.println(secondPlayerId);
                            ref2.child("kelime").setValue(kelime);
                            ref.addValueEventListener(valueEventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    System.out.println("girdii" + snapshot.hasChild("kelime"));
//                                    System.out.println(snapshot.getChildrenCount());
                                    if (snapshot.hasChild("kelime")) {
                                        if (progressDialog != null)
                                            progressDialog.dismiss();
                                        Toast.makeText(preGameActivity.this, "Oyun Başlatılıyorrrr!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(preGameActivity.this, InGameActivity.class);
                                        intent.putExtra("email", email);
                                        intent.putExtra("oyunModu", oyunModu);
                                        intent.putExtra("odaIsmi", OdaIsmi);
                                        intent.putExtra("PlayerId", PlayerId);
                                        intent.putExtra("secondPlayerId", secondPlayerId);
                                        intent.putExtra("kendiKelimem", kelime);
                                        countDownTimer.cancel();
                                        ref.child("kalansure").setValue(kalansure);
//                                        intent.putExtra("kelime", kelime);
                                        finish();
                                        ref.removeEventListener(valueEventListener);
                                        startActivity(intent);
                                    } else {
                                        System.out.println(kalansure);
                                        progressDialog = new ProgressDialog(preGameActivity.this);
                                        progressDialog.setMessage("Rakibin kelime girmesi bekleniyor...");
//                                      progressDialog.setCancelable(false);
                                        progressDialog.show();
                                        if (snapshot.hasChild("kelimebulunamadi")) {
//                                            Toast.makeText(preGameActivity.this, "Rakibiniz kelime bulamadı kazandınızz!!", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(preGameActivity.this, MainActivity2.class);
                                            intent.putExtra("email", email);
                                            intent.putExtra("oyunModu", oyunModu);
                                            intent.putExtra("PlayerId", PlayerId);

                                            finish();
                                            startActivity(intent);
                                        }

                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });

                        } else {
                            Log.d("Kelimeler", "Dosya içerisinde " + kelime + " yok.");
                            Toast.makeText(preGameActivity.this, "Lütfen geçerli bir kelime giriniz!", Toast.LENGTH_SHORT).show();
                            kelime = "";
                        }
                    }
                }
            }
        });
        GridLayout gridLayout2 = findViewById(R.id.gridLayout2);
        //rastgele
        Random random = new Random();
        rastgeleSayi = random.nextInt(OdaNo); // 0 ile OdaNo arasında rastgele bir sayı üretir
        char[] alfabe = "abcçdefghıijklmnoöprstuüvyz".toCharArray();
        int rastgeleIndeks = random.nextInt(alfabe.length);
        rastgeleHarf = alfabe[rastgeleIndeks];


        System.out.println("Rastgele sayı: " + rastgeleSayi);
        System.out.println("Rastgele harf: " + rastgeleHarf);


        int editTextSize = dpToPx(30); // converting dp to pixels
//         gridLayout.setLayoutParams(new GridLayout.LayoutParams());
        gridLayout2.setColumnCount(OdaNo);
        gridLayout2.setPadding(5, 5, 5, 5);
        for (int i = 0; i < OdaNo; i++) {
            EditText editText = new EditText(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(0, GridLayout.FILL, 1f),
                    GridLayout.spec(i, GridLayout.FILL, 1f)
            );

            params.width = editTextSize;
            params.height = editTextSize;
            editText.setGravity(Gravity.CENTER);
            params.setMargins(5, 5, 5, 5); // Margin değerleri
            editText.setLayoutParams(params);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)}); // Tek bir karakter sınırlaması
            editText.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
            // editText.setTextSize(24);
            editText.setMaxLines(1);
            editText.setSingleLine(true);
            editText.setEnabled(true);
            if (rastgeleSayi == i && oyunModu.equals("sabitli")) {
                editText.setText("" + rastgeleHarf);
                editText.setEnabled(false);
            }
            editTextList.add(editText);
            gridLayout2.addView(editText);
        }


        for (int i = 0; i < OdaNo; i++) {
            final EditText currentEditText = editTextList.get(i);
            currentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Burası metin değiştiğinde çağrılır
                    if (s.length() > 0) {
                        int currentIndex = editTextList.indexOf(currentEditText);
                        EditText currentEditText = editTextList.get(currentIndex);
                        currentEditText.setOnKeyListener(new View.OnKeyListener() {
                            @Override
                            public boolean onKey(View v, int keyCode, KeyEvent event) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                                    if (currentEditText.getText().toString().isEmpty()) {
                                        int currentIndex = editTextList.indexOf(currentEditText);
                                        if (currentIndex > 0 && editTextList.get(currentIndex - 1).isEnabled()) {
                                            EditText previousEditText = editTextList.get(currentIndex - 1);
                                            previousEditText.requestFocus();
                                        } else if (currentIndex > 1 && !editTextList.get(currentIndex - 1).isEnabled()) {
                                            EditText previousEditText = editTextList.get(currentIndex - 2);
                                            previousEditText.requestFocus();
                                        }
                                    }
                                }
                                return false;
                            }
                        });

                        if (currentIndex < editTextList.size() - 1) {
//                            editTextList.get(currentIndex + 1).setEnabled(true);
                            if (editTextList.get(currentIndex + 1).isEnabled()) {
                                EditText nextEditText = editTextList.get(currentIndex + 1);
                                nextEditText.requestFocus();
                            } else if (!editTextList.get(currentIndex + 1).isEnabled() && currentIndex < editTextList.size() - 2) {
                                EditText nextEditText = editTextList.get(currentIndex + 2);
                                nextEditText.requestFocus();
                            }

                        } else {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), 0);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}