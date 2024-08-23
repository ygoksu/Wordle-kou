package com.example.wordle;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.view.KeyEvent;
import android.view.View;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;


public class InGameActivity extends AppCompatActivity {

    private String email, PlayerId, oyunModu, odaIsmi, kelime = "", arananKelime = "", secondPlayerId, kendiKelimem, oyunhakki = "", rakipoyunhakki = "";
    private List<EditText> editTextList = new ArrayList<>();
    private List<String> kelimeler = new ArrayList<>();
    List<String> rakipkelimeler2 = new ArrayList<>();
    private int adım = 1, skor = 0, skor2 = 0;
    TextView textViewSure;
    private int oda_size = 0,kalansure = 0;
    FirebaseDatabase db;
    DatabaseReference ref, ref2;
    ValueEventListener val1, val2,surever;
    private CountDownTimer countDownTimer, reconnect, countDown10SecTimer;
    private boolean isCountDown10SecTimerRunning = false;

    private Button onaylaButton, cıkısButton, rakipGorButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_in_game);
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
        kendiKelimem = intent.getStringExtra("kendiKelimem");
//        arananKelime = intent.getStringExtra("kelime");
        oyunModu = intent.getStringExtra("oyunModu");
        onaylaButton = findViewById(R.id.onaylaButton);
        cıkısButton = findViewById(R.id.CıkısButton);
        rakipGorButton = findViewById(R.id.rakipGorButton);
        textViewSure = findViewById(R.id.textView_sure);
        db = FirebaseDatabase.getInstance();
        ref = db.getReference(odaIsmi + "/" + oyunModu + "/" + PlayerId);
        ref2 = db.getReference(odaIsmi + "/" + oyunModu + "/" + secondPlayerId);
        ref.child("oyunBittiMi").setValue("Hayır");

        char firstChar = odaIsmi.charAt(0);
        int OdaNumarasi = Character.getNumericValue(firstChar);
        System.out.println("odaismi" + OdaNumarasi);

        AlertDialog.Builder builder = new AlertDialog.Builder(InGameActivity.this);
        final AlertDialog dialog = builder.create();

        DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);

                if (connected) {
                    Log.d("baglanti", "connected");
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                        reconnect.cancel();
                    }
                } else {
                    dialog.setMessage("Bağlantınız koptu! (10 saniye içinde otomatik olarak yenilmiş sayılacaksınız!)");
                    dialog.show(); // Dialogu göster
                    reconnect = new CountDownTimer(10000, 1000) { // 10 saniye (10000 milisaniye) ve her bir adım 1 saniye (1000 milisaniye)
                        public void onTick(long millisUntilFinished) {
                            // Her adımda geriye kalan süreyi kontrol edin ve ekrana yazdırın
                            Log.d("baglanti", "Geriye sayım: " + millisUntilFinished / 1000 + " saniye kaldı");
                            dialog.setMessage("Bağlantınız koptu! (" + millisUntilFinished / 1000 + " saniye içinde otomatik olarak yenilmiş sayılacaksınız!)");

                        }

                        public void onFinish() {
                            // Geriye sayım tamamlandığında dialog penceresi görüntüleme
                            if (dialog.isShowing())
                                dialog.dismiss();
                            Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("oyunModu", oyunModu);
                            intent.putExtra("odaIsmi", odaIsmi);
                            intent.putExtra("PlayerId", PlayerId);
                            intent.putExtra("secondPlayerId", secondPlayerId);
                            cancelTimer();
                            ref2.child("oyunBittiMi").setValue("Evet");
                            ref.child("oyunBittiMi").setValue("Evet");
                            ref2.child("skor").setValue(++skor2);
                            ref2.removeEventListener(val1);
                            finish();
                            startActivity(intent);
                        }
                    }.start(); // Geriye sayımı başlat
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("baglanti", "Listener was cancelled");
            }
        });


        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("skor")) {
                    long longVal = (long) snapshot.child("skor").getValue();
                    skor = Math.toIntExact(longVal);
                } else {
                    ref.child("skor").setValue(0);
                    ref.child("kelimeler").setValue("");
                }
                if (snapshot.hasChild("kelime")) {
                    arananKelime = snapshot.child("kelime").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


//
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                if(snapshot.exists())
//                {
//                    if(snapshot.hasChild("ekstraZaman"))
//                    {
//                    cancelTimer();
//                    int sonKalanSure = (kalansure + (10*(oda_size - (adım-1))))*1000;
//                    new CountDownTimer(sonKalanSure,1000)
//                    {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
//                            int remainingSeconds = (int) (millisUntilFinished / 1000);
//                            textViewSure.setText(String.valueOf(remainingSeconds));
//                        }
//
//                        @Override
//                        public void onFinish() {
//
//                        }
//                    }.start();
//                    }
//                }
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });



        ref2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild("skor")) {
                    long longVal = (long) snapshot.child("skor").getValue();
                    skor2 = Math.toIntExact(longVal);
                } else {
                    ref2.child("skor").setValue(0);
                    ref2.child("kelimeler").setValue("");
                }
//                if (snapshot.hasChild("kelime")) {
//                    arananKelime = snapshot.child("kelime").getValue().toString();
//                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        ref2.addValueEventListener(val1 = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                boolean yonlen = true;
                if (snapshot.hasChild("oyunhakki")) {
//                    System.out.println("Rakibin oyun Hakkı Bitti");
                    if (snapshot.child("oyunhakki").getValue().equals("kalmadi")) {
//                        System.out.println("Rakibin oyun Hakkı Bitti2");
                        rakipoyunhakki = "kalmadi";
//                        if (oyunhakki.equals("kalmadi")) {
//                            //Oyun hakkım kalmadı rakibinde kalmamış.
//
//                            yonlen = false;
//                            // İkimizinde oyun hakki kalmadi
//                            int puan = 0;
//                            for (int j = (adım - 1) * oda_size; j < ((adım - 1) * oda_size) + oda_size; j++) {
//                                System.out.println("Renk Kontrol");
//                                EditText editText = editTextList.get(j);
//                                Drawable background = editText.getBackground();
//                                int color = Color.TRANSPARENT;
//                                if (background instanceof ColorDrawable) {
//                                    color = ((ColorDrawable) background).getColor();
//                                }
//                                if (color == 0xFF00FF00) {
//                                    // EditText'in rengi 0xFF00FF00 ise puanı artırın
//                                    puan += 10;
//                                } else if (color == 0xFFFFFF00) {
//                                    // EditText'in rengi 0xFFFFFF00 ise puanı daha fazla artırın
//                                    puan += 5;
                                }
                            }
//
//                            ref.child("puan").setValue(puan);
//
//
//                            ref.child("oyunBittiMi").setValue("Evet");
////                            ref2.child("oyunBittiMi").setValue("Evet");
//                            ref.child("MüsaitMi").setValue("Evet");
////                            ref2.child("MüsaitMi").setValue("Evet");
//
////                                ref.child("MüsaitMi").setValue("Evet");
////                                ref2.child("MüsaitMi").setValue("Evet");
//                            Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
//                            intent.putExtra("email", email);
//                            intent.putExtra("oyunModu", oyunModu);
//                            intent.putExtra("odaIsmi", odaIsmi);
//                            intent.putExtra("PlayerId", PlayerId);
//                            intent.putExtra("secondPlayerId", secondPlayerId);
//                            cancelTimer();
////                                intent.putExtra("kelime", arananKelime);
////                            finish();
////                            startActivity(intent);
//
//
//                        } else {
//                            //Rakibim oyunhakki bitti benim oyun hakkim var.
//                        }
//
//                    }
//                }
                System.out.println("Girdi 1 ");

                if (snapshot.hasChild("oyunBittiMi") && yonlen) {
                    System.out.println("Girdi 2 ");
                    if (snapshot.child("oyunBittiMi").getValue().equals("Evet")) {
                        System.out.println("Girdi 3 ");
                        Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("oyunModu", oyunModu);
                        intent.putExtra("odaIsmi", odaIsmi);
                        intent.putExtra("PlayerId", PlayerId);
                        intent.putExtra("secondPlayerId", secondPlayerId);
                        if (isCountDown10SecTimerRunning)
                            countDown10SecTimer.cancel();
                        cancelTimer();
//                        intent.putExtra("kelime", arananKelime);
                        cancelTimer();
                        ref2.removeEventListener(val1);
                        finish();
                        startActivity(intent);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        startTimer();

        cıkısButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTimer();
                AlertDialog.Builder builder = new AlertDialog.Builder(InGameActivity.this);
                builder.setTitle("Oyundan Çıkış");
                builder.setMessage("Oyundan çıkmak istiyor musunuz?");
                builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Kullanıcı "Evet" butonuna bastığında yapılacak işlemler
                        // Kaybettiniz uyarısı göster
                        AlertDialog.Builder kaybettinizBuilder = new AlertDialog.Builder(InGameActivity.this);
                        kaybettinizBuilder.setTitle("Kaybettiniz");
                        kaybettinizBuilder.setMessage("Oyunu kaybettiniz!");
                        kaybettinizBuilder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Burada yapılacak işlemler (örneğin, uygulamadan çıkabilir)
                                Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("oyunModu", oyunModu);
                                intent.putExtra("odaIsmi", odaIsmi);
                                intent.putExtra("PlayerId", PlayerId);
                                intent.putExtra("secondPlayerId", secondPlayerId);
//                        intent.putExtra("kelime", arananKelime);
                                cancelTimer();
                                ref2.child("oyunBittiMi").setValue("Evet");
                                ref.child("oyunBittiMi").setValue("Evet");
                                ref2.child("skor").setValue(++skor2);
                                ref2.removeEventListener(val1);
                                finish();
                                startActivity(intent);

                            }
                        });
                        kaybettinizBuilder.show();
                    }
                });
                builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Oyuna devam et
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        rakipGorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("kelimeler")) {
                            //List<String> rakipkelimeler = new ArrayList<>();
                            //rakipkelimeler = snapshot.child("kelimeler").getValue();
                            try {
                                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {
                                };
                                List<String> rakipkelimeler = snapshot.child("kelimeler").getValue(t);
                                List<EditText> rakipEditTextList = new ArrayList<>();
                                System.out.println(rakipkelimeler);

                                ////////////////////////

                                Dialog dialog = new Dialog(InGameActivity.this);
                                dialog.setContentView(R.layout.custom_dialog_layout);

                                // Tamam butonuna tıklanıldığında dialog penceresini kapat

                                oda_size = OdaNumarasi;
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
                                        EditText editText2 = new EditText(InGameActivity.this);
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
                                for (int i = 0; i < rakipkelimeler.size(); i++) {
                                    for (int j = 0; j < rakipkelimeler.get(i).length(); j++) {
                                        rakipEditTextList.get(tut).setText(String.valueOf(rakipkelimeler.get(i).charAt(j)));
                                        tut++;
                                    }
                                }
                                int rakipadım = 1;
                                for (int i = 0; i < rakipkelimeler.size(); i++) {
                                    for (int j = 0; j < rakipkelimeler.get(i).length(); j++) {
                                        System.out.println(j);
                                        for (int k = 0; k < rakipkelimeler.get(i).length(); k++) {
                                            if (k != j) {
                                                if (rakipkelimeler.get(i).charAt(j) == kendiKelimem.charAt(k)) {
                                                    rakipEditTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFFFFFF00);
                                                }
                                            }
                                        }
                                        if (rakipkelimeler.get(i).charAt(j) == kendiKelimem.charAt(j)) {
                                            System.out.println("Değişiklik");
                                            rakipEditTextList.get(((rakipadım - 1) * oda_size) + j).setBackgroundColor(0xFF00FF00);
                                        }

                                    }
                                    rakipadım++;
                                }


//                            for (int j = 0; j < kelime.length(); j++) {
//                                System.out.println("Kelime kontrole girdik");
//                                System.out.println(kelime.charAt(j));
//                                for (int k = 0; k < kelime.length(); k++) {
//                                    if (k != j) {
//                                        if (kelime.charAt(j) == arananKelime.charAt(k)) {
//                                            editTextList.get(((adım - 1) * oda_size) + j).setBackgroundColor(0xFFFFFF00);
//                                        }
//                                    }
//                                }
//                                if (kelime.charAt(j) == arananKelime.charAt(j)) {
//                                    System.out.println("Değişiklik");
//                                    editTextList.get(((adım - 1) * oda_size) + j).setBackgroundColor(0xFF00FF00);
//                                }
//
//
//                            }

                                //////////////////////////////////////77
                                Button kapatButton = dialog.findViewById(R.id.kapatButton);
                                kapatButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();

                            } catch (Exception e) {
                                //  Block of code to handle errors
                            }

                        } else {
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        onaylaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int kontroltut = adım*oda_size;
                for (int i = (adım - 1) * oda_size; i < (adım * oda_size); i++) {
//                    System.out.println("Buton ici");
                    if(i == kontroltut)
                    {
                        break;
                    }
                    else if (editTextList.get(i).getText().toString().isEmpty()) {
                        Toast.makeText(InGameActivity.this, "Kelimeyi tamamlayınız!", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    if (i == ((adım * oda_size) - 1)) {
//                        System.out.println("Buton ici2");
                        kelime = "";
                        for (int j = (adım - 1) * oda_size; j < (adım * oda_size); j++)
                            kelime += editTextList.get(j).getText().toString();
                        Log.d("kelime", kelime);
                        System.out.println("kelime " + kelime);
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
//                        System.out.println("Okuduk " + kelimeVarMi);

                        if (kelimeVarMi) {
                            kelimeler.add(kelime);
                            ref.child("kelimeler").setValue(kelimeler);
                            Log.d("Kelimeler", "Dosya içerisinde " + kelime + " bulunmaktadır.");
                            System.out.println("Dosya içerisinde " + kelime + " bulunmaktadır.");
                            if (isCountDown10SecTimerRunning) {
                                countDown10SecTimer.cancel();
                                textViewSure.setText("");
                            }
                            resetTimer();
                            for (int j = 0; j < kelime.length(); j++) {
//                                System.out.println("Kelime kontrole girdik");
                                System.out.println(kelime.charAt(j));
                                for (int k = 0; k < kelime.length(); k++) {
                                    if (k != j) {
                                        if (kelime.charAt(j) == arananKelime.charAt(k)) {
                                            editTextList.get(((adım - 1) * oda_size) + j).setBackgroundColor(0xFFFFFF00);
                                        }
                                    }
                                }
                                if (kelime.charAt(j) == arananKelime.charAt(j)) {
                                    System.out.println("Değişiklik");
                                    editTextList.get(((adım - 1) * oda_size) + j).setBackgroundColor(0xFF00FF00);
                                }

                            }
                            System.out.println("Cekmediysen");
                            for (int j = (adım - 1) * oda_size; j < ((adım - 1) * oda_size) + oda_size; j++) {
                                System.out.println("Cekmediysen2");
                                editTextList.get(j).setEnabled(false);
                            }


                            if (kelime.equals(arananKelime)) {
                                System.out.println("Cekmediysen4");
                                for (int j = 0; j < editTextList.size(); j++) {
                                    editTextList.get(j).setEnabled(false);
                                }
                                Toast.makeText(InGameActivity.this, "Oyun Bitti Kazandınız", Toast.LENGTH_SHORT).show();
                                ref2.child("oyunBittiMi").setValue("Evet");
                                ref.child("oyunBittiMi").setValue("Evet");
                                ref.child("MüsaitMi").setValue("Evet");
                                ref2.child("MüsaitMi").setValue("Evet");

//                                ref.child("MüsaitMi").setValue("Evet");
//                                ref2.child("MüsaitMi").setValue("Evet");
                                Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                                intent.putExtra("email", email);
                                intent.putExtra("oyunModu", oyunModu);
                                intent.putExtra("odaIsmi", odaIsmi);
                                intent.putExtra("PlayerId", PlayerId);
                                intent.putExtra("secondPlayerId", secondPlayerId);
                                cancelTimer();
//                                intent.putExtra("kelime", arananKelime);
                                ref.child("skor").setValue(++skor);
//                                finish();
//                                startActivity(intent);
                                break;
                            }


                            if (adım == oda_size) {
                                System.out.println("Cekmediysen4");
                                for (int j = 0; j < editTextList.size(); j++) {
                                    editTextList.get(j).setEnabled(false);
                                }
                                Toast.makeText(InGameActivity.this, "Oyun Bitti Doğru Kelime Bulunamadı", Toast.LENGTH_SHORT).show();
                                ref.child("oyunhakki").setValue("kalmadi");
                                oyunhakki = "kalmadi";

                                if (rakipoyunhakki.equals("kalmadi")) {
                                    // İkimizinde oyun hakki kalmadi
                                    int puan = 0;
                                    for (int j = (adım - 1) * oda_size; j < ((adım - 1) * oda_size) + oda_size; j++) {
                                        System.out.println("Renk Kontrol");
                                        EditText editText = editTextList.get(j);
                                        Drawable background = editText.getBackground();
                                        int color = Color.TRANSPARENT;
                                        if (background instanceof ColorDrawable) {
                                            color = ((ColorDrawable) background).getColor();
                                        }
                                        if (color == 0xFF00FF00) {
                                            // EditText'in rengi 0xFF00FF00 ise puanı artırın
                                            puan += 10;
                                        } else if (color == 0xFFFFFF00) {
                                            // EditText'in rengi 0xFFFFFF00 ise puanı daha fazla artırın
                                            puan += 5;
                                        }
                                    }

                                    ref.child("puan").setValue(puan);

                                    int puan2 = 0;
                                    ref2.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild("kelimeler")) {
                                                GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                                                rakipkelimeler2 = snapshot.child("kelimeler").getValue(t);

                                                int size = rakipkelimeler2.size() -1;


                                                char dizi[] = new char[oda_size];
                                                int puan2 = 0;
                                                for (int j = 0; j < dizi.length; j++) {
                                                    dizi[j] = 'g';
                                                }

                                                for (int j = 0; j < rakipkelimeler2.get(size).length(); j++) {
                                                    for (int k = 0; k < rakipkelimeler2.get(size).length(); k++) {
                                                        if (j!=k){
                                                            if(rakipkelimeler2.get(size).charAt(j) == kendiKelimem.charAt(k)){
                                                                dizi[j]= 's';
                                                            }
                                                        }
                                                    }
                                                    if (rakipkelimeler2.get(size).charAt(j) == kendiKelimem.charAt(j)){
                                                        dizi[j] = 'y';
                                                    }
                                                }

                                                for (int j = 0; j < dizi.length; j++) {
                                                    if(dizi[j]=='s'){
                                                        puan2+=5;
                                                    }
                                                    else if(dizi[j]=='y'){
                                                        puan2+=10;
                                                    }
                                                }

                                                ref2.child("puan").setValue(puan2);

                                                ref.child("oyunBittiMi").setValue("Evet");
                                                ref2.child("oyunBittiMi").setValue("Evet");
                                                ref.child("MüsaitMi").setValue("Evet");
                                                ref2.child("MüsaitMi").setValue("Evet");

                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

//                                    int size = rakipkelimeler2.size() -1;
//
//
//                                    char dizi[] = new char[oda_size];
//                                    for (int j = 0; j < dizi.length; j++) {
//                                        dizi[j] = 'g';
//                                    }
//
//                                    for (int j = 0; j < rakipkelimeler2.get(size).length(); j++) {
//                                        for (int k = 0; k < rakipkelimeler2.get(size).length(); k++) {
//                                            if (j!=k){
//                                                if(rakipkelimeler2.get(size).charAt(j) == kendiKelimem.charAt(k)){
//                                                    dizi[j]= 's';
//                                                }
//                                            }
//                                        }
//                                        if (rakipkelimeler2.get(size).charAt(j) == kendiKelimem.charAt(j)){
//                                            dizi[j] = 'y';
//                                        }
//                                    }
//
//                                    for (int j = 0; j < dizi.length; j++) {
//                                        if(dizi[j]=='s'){
//                                            puan2+=5;
//                                        }
//                                        else if(dizi[j]=='y'){
//                                            puan2+=10;
//                                        }
//                                    }
//
//                                    ref2.child("puan").setValue(puan2);





//                                    ref.child("oyunBittiMi").setValue("Evet");
//                                    ref2.child("oyunBittiMi").setValue("Evet");
//                                    ref.child("MüsaitMi").setValue("Evet");
//                                    ref2.child("MüsaitMi").setValue("Evet");

//                                ref.child("MüsaitMi").setValue("Evet");
//                                ref2.child("MüsaitMi").setValue("Evet");
                                    Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                                    intent.putExtra("email", email);
                                    intent.putExtra("oyunModu", oyunModu);
                                    intent.putExtra("odaIsmi", odaIsmi);
                                    intent.putExtra("PlayerId", PlayerId);
                                    intent.putExtra("secondPlayerId", secondPlayerId);
                                    cancelTimer();
//                                intent.putExtra("kelime", arananKelime);
//                                    finish();
//                                    startActivity(intent);

                                } else {
                                    // Benim oyun hakkim bitti senin oyun hakkin bitmedi
//                                    ref2.child("ekstraZaman").setValue("Verildi");

                                }

                                break;
                            } else {
                                for (int j = (adım) * oda_size; j < ((adım) * oda_size) + oda_size; j++) {
                                    System.out.println("Cekmediysen3");
                                    editTextList.get(j).setEnabled(true);
                                }
                                EditText previousEditText = editTextList.get(((adım - 1) * oda_size) + oda_size);
                                previousEditText.requestFocus();
                                adım++;
                            }


                        } else {
                            Log.d("Kelimeler", "Dosya içerisinde " + kelime + " yok.");
                            Toast.makeText(InGameActivity.this, "Lütfen geçerli bir kelime giriniz!", Toast.LENGTH_SHORT).show();
                            kelime = "";
                        }


                    }
                }
//                String currentDirectory = System.getProperty("user.dir");
//                System.out.println("Current working directory: " + currentDirectory);
//                int baslangic = 4*kalinan;
//                System.out.println("Girdi");
//                System.out.println(baslangic);
//                editTextList.get(baslangic-1).setEnabled(false);
//                editTextList.get(baslangic-2).setEnabled(false);
//                editTextList.get(baslangic-3).setEnabled(false);
//                editTextList.get(baslangic-4).setEnabled(false);
//                editTextList.get(baslangic).setEnabled(true);
//                editTextList.get(baslangic).requestFocus();
//                izin(baslangic,baslangic+3);
//                kalinan++;
            }
        });


//        if (odaIsmi.equals("4 Harfli")) {
        oda_size = OdaNumarasi;
        GridLayout gridLayout = findViewById(R.id.gridLayout);
        int editTextSize = dpToPx(30); // converting dp to pixels
        // gridLayout.setLayoutParams(new GridLayout.LayoutParams());
        gridLayout.setColumnCount(oda_size);
        gridLayout.setRowCount(oda_size);
        gridLayout.setPadding(5, 5, 5, 5);
        for (int i = 0; i < oda_size; i++) {
            for (int j = 0; j < oda_size; j++) {
                EditText editText = new EditText(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(i, GridLayout.FILL, 1f),
                        GridLayout.spec(j, GridLayout.FILL, 1f)
                );
                params.width = editTextSize;
                params.height = editTextSize;
                editText.setGravity(Gravity.CENTER);
                params.setMargins(5, 5, 5, 5); // Margin değerleri
                editText.setLayoutParams(params);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)}); // Tek bir karakter sınırlaması
                editText.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                // editText.setTextSize(24);
                editText.setBackgroundResource(R.drawable.edittext_border); // Arka plan drawable'ını ayarlayın
                editText.setMaxLines(1);
                editText.setSingleLine(true);
                editTextList.add(editText);
                //editText.setHint("Edit Text " + ((i * 4) + j + 1));
                editText.setEnabled(false);

                gridLayout.addView(editText);
            }
        }

//        }

        for (int i = 0; i < oda_size; i++) {
            editTextList.get(i).setEnabled(true);
        }

        // OdaNo değişkeni, EditText sayısını belirtir
        int OdaNo = editTextList.size();

// EditText'leri döngüyle dolaşın
        for (int i = 0; i < OdaNo; i++) {
            final EditText currentEditText = editTextList.get(i);
            currentEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Metin değiştiğinde çağrılacak işlemler
                    if (s.length() > 0) {
                        int currentIndex = editTextList.indexOf(currentEditText);
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
                                    return true; // işlendiğini belirtmek için true döndürün
                                }
                                return false; // diğer durumlarda false döndürün
                            }
                        });
                        if (currentIndex == ((adım * oda_size) - 1)) {
                            System.out.println("Here Buradayızzzzz");
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(currentEditText.getWindowToken(), 0);
                        } else {
                            if (currentIndex < editTextList.size() - 1) {
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
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }


    }

    private int sure = 60000;

    private void startTimer() {
        isCountDown10SecTimerRunning = false;
        countDownTimer = new CountDownTimer(sure, 1000) { // 1 dakika (60 saniye)
            @Override
            public void onTick(long millisUntilFinished) {
                kalansure = (int) (millisUntilFinished / 1000);
                textViewSure.setText(String.valueOf(kalansure));
            }

            @Override
            public void onFinish() {
                // Geri sayım tamamlandığında yapılacak işlemler
//                AlertDialog.Builder builder = new AlertDialog.Builder(InGameActivity.this);
//                builder.setTitle("Oyunu Kaybetmek üzeresiniz...");
//                builder.setMessage("Kelime girmek için son 10 saniye!");
                Toast.makeText(InGameActivity.this, "Kelime girmek için son 10 saniye!", Toast.LENGTH_SHORT).show();
                isCountDown10SecTimerRunning = true;
                countDown10SecTimer = new CountDownTimer(10000, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        int secondsRemaining = (int) (millisUntilFinished / 1000);
                        textViewSure.setText(String.valueOf(secondsRemaining));
                    }

                    @Override
                    public void onFinish() {
                        //BU INTENTLER KALDIRILACAK YERİNE RASTGELE KELİME GİRİŞİ YAPILACAK
                        Toast.makeText(InGameActivity.this, "Verilen süre içerisinde kelime giremediniz, kaybettiniz...", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(InGameActivity.this, endGameActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("oyunModu", oyunModu);
                        intent.putExtra("odaIsmi", odaIsmi);
                        intent.putExtra("PlayerId", PlayerId);
                        intent.putExtra("secondPlayerId", secondPlayerId);
                        cancelTimer();
                        ref2.child("oyunBittiMi").setValue("Evet");
                        ref.child("oyunBittiMi").setValue("Evet");
                        ref2.child("skor").setValue(++skor2);
                        ref2.removeEventListener(val1);
                        finish();
                        startActivity(intent);
                    }
                }.start();
//                builder.setPositiveButton("Tamam", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.show();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void resetTimer() {
        cancelTimer();
        startTimer();
    }


    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}