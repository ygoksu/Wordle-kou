package com.example.wordle;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class endGameActivity extends AppCompatActivity {
    private String email, PlayerId, oyunModu, odaIsmi, kelime, rakipKelime, secondPlayerId, rakipEmail;
    TextView oyuncuAdi1, oyuncuAdi2;
    int oda_size, skor, rakipskor,puan = 0,rakipPuan = 0;
    Button don, rematch, rakipOyunuGor, kendiOyunuGor;
    FirebaseDatabase db;
    private List<String> kelimeler = new ArrayList<>();
    private List<String> rakipKelimeler = new ArrayList<>();
    private List<EditText> editTextList = new ArrayList<>();
    private List<EditText> rakipEditTextList = new ArrayList<>();
    DatabaseReference ref, ref2, RejectRef;
    CountDownTimer timer;
    ValueEventListener valueEventListener, val1, val2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_end_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        PlayerId = intent.getStringExtra("PlayerId");
        secondPlayerId = intent.getStringExtra("secondPlayerId");
        odaIsmi = intent.getStringExtra("odaIsmi");
//        arananKelime = intent.getStringExtra("kelime");
        oyunModu = intent.getStringExtra("oyunModu");
        oyuncuAdi1 = findViewById(R.id.textViewOyuncuAdi);
        oyuncuAdi2 = findViewById(R.id.textViewOyuncuAdi2);
//        oyuncuAdi1.setText("Oyuncu Adı: " + email);
//        oyuncuAdi2.setText("Oyuncu Adı: " + secondPlayerId);
        kendiOyunuGor = findViewById(R.id.kendiOyunuGor);
        rakipOyunuGor = findViewById(R.id.rakipOyunuGor);
        db = FirebaseDatabase.getInstance();
        ref = db.getReference(odaIsmi + "/" + oyunModu + "/" + PlayerId);
        ref2 = db.getReference(odaIsmi + "/" + oyunModu + "/" + secondPlayerId);
        //ref.child("oyunBittiMi").removeValue();
        char firstChar = odaIsmi.charAt(0);
        oda_size = Character.getNumericValue(firstChar);
        timer = new CountDownTimer(200000, 1000) { // 1 dakika (60 saniye)
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(endGameActivity.this, MainActivity2.class);
                intent.putExtra("email", email);
                intent.putExtra("PlayerId", PlayerId);
                ref.child("kelimeler").removeValue();
                ref.child("puan").removeValue();
                ref.child("oyunhakki").removeValue();
                finish();
                startActivity(intent);
                Toast.makeText(endGameActivity.this, "30 saniye sona erdi ana menüye dönülüyor!", Toast.LENGTH_SHORT).show();


            }
        }.start();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long longVal = (long) snapshot.child("skor").getValue();
                skor = Math.toIntExact(longVal);
                oyuncuAdi1.setText("Oyuncu Adı: " + email + " Skor: " + skor);
                kelime = (String) snapshot.child("kelime").getValue();
                if (snapshot.child("kelimeler").getValue() instanceof List) {
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                    };
                    kelimeler = snapshot.child("kelimeler").getValue(t);
                }
                if (snapshot.hasChild("puan")) {
                    long longVal2 = (long) snapshot.child("puan").getValue();
                    long longVal3 = (long) snapshot.child("kalansure").getValue();
                    puan= Math.toIntExact(longVal2) + Math.toIntExact(longVal3);
//                    oyuncuAdi1.setText("Oyuncu Adı: " + email + " Skor: " + skor + " Puan: " + puan);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("OyuncuAdi")) {
                        rakipEmail = (String) snapshot.child("OyuncuAdi").getValue();
                        long longVal = (long) snapshot.child("skor").getValue();
                        rakipskor = Math.toIntExact(longVal);
                        oyuncuAdi2.setText("Oyuncu Adı: " + rakipEmail + " Skor: " + rakipskor);
                        rakipKelime = (String) snapshot.child("kelime").getValue();
                        if (snapshot.child("kelimeler").getValue() instanceof List) {
                            GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                            };
                            rakipKelimeler = snapshot.child("kelimeler").getValue(t);
                        }
                        if (snapshot.hasChild("puan")) {
                            long longVal2 = (long) snapshot.child("puan").getValue();
                            long longVal3 = (long) snapshot.child("kalansure").getValue();
                            rakipPuan= Math.toIntExact(longVal2) + Math.toIntExact(longVal3);

//                            oyuncuAdi2.setText("Oyuncu Adı: " + rakipEmail + " Skor: " + rakipskor + " Puan: " + rakipPuan);
                            if(puan > rakipPuan)
                            {
                                skor++;
                                ref.child("skor").setValue(skor);

                            }
                            else if( rakipPuan > puan){
                                rakipskor++;
                            }
                            oyuncuAdi1.setText("Oyuncu Adı: " + email + " Skor: " + skor + " Puan: " + puan);
                            oyuncuAdi2.setText("Oyuncu Adı: " + rakipEmail + " Skor: " + rakipskor + " Puan: " + rakipPuan);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });





        rakipOyunuGor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!rakipKelimeler.isEmpty()) {
                    Dialog dialog = new Dialog(endGameActivity.this);
                    dialog.setContentView(R.layout.custom_dialog_layout);

                    GridLayout gridLayout2 = dialog.findViewById(R.id.gridLayoutCustom);
                    int editTextSize = dpToPx(30); // converting dp to pixels
                    // gridLayout.setLayoutParams(new GridLayout.LayoutParams());
                    gridLayout2.setColumnCount(oda_size);
                    gridLayout2.setRowCount(oda_size);
                    gridLayout2.setPadding(5, 5, 5, 5);

                    for (int i = 0; i < oda_size; i++) {
                        System.out.println("dışa girdi");
                        for (int j = 0; j < oda_size; j++) {
                            System.out.println("içe girdi");
                            EditText editText2 = new EditText(endGameActivity.this);
                            GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(
                                    GridLayout.spec(i, GridLayout.FILL, 1f),
                                    GridLayout.spec(j, GridLayout.FILL, 1f)
                            );
                            params2.width = editTextSize;
                            params2.height = editTextSize;
                            editText2.setGravity(Gravity.CENTER);
                            params2.setMargins(5, 5, 5, 5); // Margin değerleri
                            editText2.setLayoutParams(params2);
                            editText2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)}); // Tek bir karakter sınırlaması
                            editText2.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                            // editText.setTextSize(24);
                            editText2.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                            editText2.setMaxLines(1);
                            editText2.setSingleLine(true);
                            rakipEditTextList.add(editText2);
                            //editText.setHint("Edit Text " + ((i * 4) + j + 1));
                            editText2.setEnabled(false);

                            gridLayout2.addView(editText2);
                        }
                    }

                    int tut = 0;
                    System.out.println("Donguden çıktı");
                    for (int i = 0; i < rakipKelimeler.size(); i++) {
                        for (int j = 0; j < rakipKelimeler.get(i).length(); j++) {
                            rakipEditTextList.get(tut).setText(String.valueOf(rakipKelimeler.get(i).charAt(j)));
                            tut++;
                        }
                    }


                    int rakipadım = 1;
                    for (int i = 0; i < rakipKelimeler.size(); i++) {
                        for (int j = 0; j < rakipKelimeler.get(i).length(); j++) {
                            System.out.println(j);
                            for (int k = 0; k < rakipKelimeler.get(i).length(); k++) {
                                if (k != j) {
                                    if (rakipKelimeler.get(i).charAt(j) == rakipKelime.charAt(k)) {
                                        rakipEditTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFFFFFF00);
                                    }
                                }
                            }
                            if (rakipKelimeler.get(i).charAt(j) == rakipKelime.charAt(j)) {
                                System.out.println("Değişiklik");
                                rakipEditTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFF00FF00);
                            }

                        }
                        rakipadım++;
                    }


                    Button kapatButton = dialog.findViewById(R.id.kapatButton);
                    kapatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    rakipEditTextList.clear();

                }


            }
        });


        kendiOyunuGor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!kelimeler.isEmpty()) {
                    Dialog dialog = new Dialog(endGameActivity.this);
                    dialog.setContentView(R.layout.custom_dialog_layout);

                    GridLayout gridLayout2 = dialog.findViewById(R.id.gridLayoutCustom);
                    int editTextSize = dpToPx(30); // converting dp to pixels
                    // gridLayout.setLayoutParams(new GridLayout.LayoutParams());
                    gridLayout2.setColumnCount(oda_size);
                    gridLayout2.setRowCount(oda_size);
                    gridLayout2.setPadding(5, 5, 5, 5);

                    for (int i = 0; i < oda_size; i++) {
                        System.out.println("dışa girdi");
                        for (int j = 0; j < oda_size; j++) {
                            System.out.println("içe girdi");
                            EditText editText2 = new EditText(endGameActivity.this);
                            GridLayout.LayoutParams params2 = new GridLayout.LayoutParams(
                                    GridLayout.spec(i, GridLayout.FILL, 1f),
                                    GridLayout.spec(j, GridLayout.FILL, 1f)
                            );
                            params2.width = editTextSize;
                            params2.height = editTextSize;
                            editText2.setGravity(Gravity.CENTER);
                            params2.setMargins(5, 5, 5, 5); // Margin değerleri
                            editText2.setLayoutParams(params2);
                            editText2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)}); // Tek bir karakter sınırlaması
                            editText2.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                            // editText.setTextSize(24);
                            editText2.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                            editText2.setMaxLines(1);
                            editText2.setSingleLine(true);
                            editTextList.add(editText2);

                            //editText.setHint("Edit Text " + ((i * 4) + j + 1));
                            editText2.setEnabled(false);

                            gridLayout2.addView(editText2);
                        }
                    }

                    int tut = 0;
                    System.out.println("Donguden çıktı");
                    for (int i = 0; i < kelimeler.size(); i++) {
                        for (int j = 0; j < kelimeler.get(i).length(); j++) {
                            editTextList.get(tut).setText(String.valueOf(kelimeler.get(i).charAt(j)));
                            tut++;
                        }
                    }


                    int rakipadım = 1;
                    for (int i = 0; i < kelimeler.size(); i++) {
                        for (int j = 0; j < kelimeler.get(i).length(); j++) {
                            System.out.println(j);
                            for (int k = 0; k < kelimeler.get(i).length(); k++) {
                                if (k != j) {
                                    if (kelimeler.get(i).charAt(j) == kelime.charAt(k)) {
                                        editTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFFFFFF00);
                                    }
                                }
                            }
                            if (kelimeler.get(i).charAt(j) == kelime.charAt(j)) {
                                System.out.println("Değişiklik");
                                editTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFF00FF00);
                            }

                        }
                        rakipadım++;
                    }


                    Button kapatButton = dialog.findViewById(R.id.kapatButton);
                    kapatButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    editTextList.clear();

                }


            }
        });

        don = findViewById(R.id.button_don);
        don.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(endGameActivity.this, MainActivity2.class);
                intent.putExtra("email", email);
                intent.putExtra("PlayerId", PlayerId);
                timer.cancel();
                ref.removeValue();
                finish();
                startActivity(intent);
            }
        });

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("Istek")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(endGameActivity.this);
                    builder.setMessage("Size bir istek geldi. Kabul ediyor musunuz?");
                    builder.setCancelable(false);
                    builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ref.child("MüsaitMi").setValue("Hayır");
                            ref2.child("MüsaitMi").setValue("Hayır");
                            ref.child("kelimeler").removeValue();
                            ref.child("puan").removeValue();
                            ref.child("oyunhakki").removeValue();
                            timer.cancel();
                            ref.child("kelime").removeValue();
                            ref.child("kalansure").removeValue();
                            // İSTEK KABUL EDİLDİ 2. OYUNCUNUN(İsteği alan) EKRAN DEĞİŞİMİ
                            Intent intent1 = new Intent(endGameActivity.this, preGameActivity.class);
                            intent1.putExtra("oyunModu", oyunModu);
                            intent1.putExtra("odaIsmi", odaIsmi);
                            intent1.putExtra("email", email);
                            intent1.putExtra("PlayerId", PlayerId);
                            secondPlayerId = (String) snapshot.child("Istek").getValue();
                            System.out.println(secondPlayerId);
                            intent1.putExtra("secondPlayerId", secondPlayerId);//istek silinmeden önce de alınabilir.
                            finish();
                            startActivity(intent1);
                        }
                    });
                    builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // İstek reddedildiğinde yapılacak işlemler
                            ref.child("Istek").removeValue();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    if (!isFinishing()) {
                        dialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        ref.addValueEventListener(valueEventListener);
        rematch = findViewById(R.id.button_rematch);
        rematch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Oyuncu müsaitse, isteği gönder
                Toast.makeText(endGameActivity.this, "Oyuncuya istek gönderildi!", Toast.LENGTH_SHORT).show();
                ref2.child("Istek").setValue(PlayerId);//2. telefon PlayersRef
                RejectRef = db.getReference(odaIsmi + "/" + oyunModu + "/" + secondPlayerId);
                // secondPlayerId = playerIdList.get(position);
                RejectRef.addValueEventListener(val1 = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            if (!snapshot.hasChild("Istek") && snapshot.child("MüsaitMi").getValue().equals("Evet"))
                                Toast.makeText(endGameActivity.this, "İstek reddedildi!", Toast.LENGTH_SHORT).show();
                            else if (snapshot.child("MüsaitMi").getValue().equals("Hayır")) {
                                Intent intent1 = new Intent(endGameActivity.this, preGameActivity.class);
                                intent1.putExtra("oyunModu", oyunModu);
                                intent1.putExtra("odaIsmi", odaIsmi);
                                intent1.putExtra("email", email);
                                intent1.putExtra("PlayerId", PlayerId);
                                intent1.putExtra("secondPlayerId", secondPlayerId);
                                RejectRef.removeEventListener(val1);
                                ref.child("puan").removeValue();
                                timer.cancel();
                                ref.child("oyunhakki").removeValue();
                                ref.child("kelimeler").removeValue();
                                ref2.child("Istek").removeValue();
                                finish();
                                startActivity(intent1);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}