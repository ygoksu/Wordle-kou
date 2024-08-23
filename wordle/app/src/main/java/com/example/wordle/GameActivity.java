package com.example.wordle;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private String email, oyunModu, kaclikOda = "", PlayerId, OdaIsmi, oldOdaIsmi = "4 Harfli", secondPlayerId;
    private Spinner spinner;
    private ArrayAdapter<CharSequence> Odalar;
    private ListView listView;
    private TextView txtGame;
    private Button don;
    FirebaseDatabase db;
    DatabaseReference RoomsRef, PlayersRef, ResponseRef, RejectRef, AcceptRef;
    List<String> playersList, playerIdList;
    ValueEventListener valueEventListener, rejectRefValEventListener;
    ValueEventListener valueEventListenerResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        email = intent.getStringExtra("email");
        PlayerId = intent.getStringExtra("PlayerId");
        oyunModu = intent.getStringExtra("oyunModu");
        txtGame = findViewById(R.id.textView3);
        db = FirebaseDatabase.getInstance();
        txtGame.setText("Seçilen oyun modu : " + oyunModu);
        don = findViewById(R.id.button_geri);
        spinner = findViewById(R.id.spinner);
        listView = findViewById(R.id.listView);
        Odalar = ArrayAdapter.createFromResource(this, R.array.Odalar, android.R.layout.simple_spinner_item);
        Odalar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(Odalar);
        playersList = new ArrayList<>();
        playerIdList = new ArrayList<>();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println(playersList.get(position));
                //İstek atma/Kendisine atamama
                if (!playerIdList.get(position).equals(PlayerId)) {
                    PlayersRef = db.getReference(OdaIsmi + "/" + oyunModu + "/" + playerIdList.get(position));//PlayersRef 2.oyuncu için
                    DatabaseReference me = db.getReference(OdaIsmi + "/" + oyunModu + "/" + PlayerId);
                    secondPlayerId = playerIdList.get(position);
                    PlayersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String musaitMi = snapshot.child("MüsaitMi").getValue(String.class);

                                if (musaitMi != null && musaitMi.equals("Evet")) {
                                    // Oyuncu müsaitse, isteği gönder
                                    Toast.makeText(GameActivity.this, "Oyuncuya istek gönderildi!", Toast.LENGTH_SHORT).show();
                                    PlayersRef.child("Istek").setValue(PlayerId);//2. telefon PlayersRef
                                    RejectRef = db.getReference(OdaIsmi + "/" + oyunModu + "/" + playerIdList.get(position));
                                    // secondPlayerId = playerIdList.get(position);
                                    RejectRef.addValueEventListener(rejectRefValEventListener = new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                            System.out.println("giriyor");
                                            if (!snapshot.hasChild("Istek") && snapshot.child("MüsaitMi").getValue().equals("Evet"))
                                                Toast.makeText(GameActivity.this, "İstek reddedildi!", Toast.LENGTH_SHORT).show();
                                            else if (snapshot.child("MüsaitMi").getValue().equals("Hayır")) { // İSTEK KABUL EDİLDİ 1. OYUNCUNUN(isteği yollayan) EKRAN DEĞİŞİMİ
                                                me.child("MüsaitMi").setValue("Hayır");
                                                Intent intent1 = new Intent(GameActivity.this, preGameActivity.class);
                                                intent1.putExtra("oyunModu", oyunModu);
                                                intent1.putExtra("odaIsmi", OdaIsmi);
                                                intent1.putExtra("email", email);
                                                intent1.putExtra("PlayerId", PlayerId);
                                                intent1.putExtra("secondPlayerId", secondPlayerId);
                                                RejectRef.removeEventListener(rejectRefValEventListener);
                                                finish();
                                                startActivity(intent1);
                                                PlayersRef.child("Istek").removeValue();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });

                                } else {
                                    Toast.makeText(GameActivity.this, "Oyuncu meşgul!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(GameActivity.this, "Oyuncu bulunamadı!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }

            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OdaIsmi = parent.getItemAtPosition(position).toString();
                final DatabaseReference selectedRoomRef = db.getReference(OdaIsmi + "/" + oyunModu);
                final DatabaseReference oldSelectedRoomRef = db.getReference(oldOdaIsmi + "/" + oyunModu);
                final DatabaseReference oldResponseRef = db.getReference(oldOdaIsmi + "/" + oyunModu + "/" + PlayerId);
                selectedRoomRef.child(PlayerId).child("MüsaitMi").setValue("Evet");
                selectedRoomRef.child(PlayerId).child("OyuncuAdi").setValue(email);
//                selectedRoomRef.child(PlayerId).child("kelime").setValue("");
//                selectedRoomRef.child(PlayerId).child("kelime").removeValue();
                kaclikOda = OdaIsmi;
                ResponseRef = db.getReference(OdaIsmi + "/" + oyunModu + "/" + PlayerId);
                //System.out.println(PlayerId);
                valueEventListenerResponse = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild("Istek")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
                            builder.setMessage("Size bir istek geldi. Kabul ediyor musunuz?");
                            builder.setCancelable(false);

                            builder.setPositiveButton("Evet", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //            ResponseRef.child("Istek").removeValue();
                                    ResponseRef.child("MüsaitMi").setValue("Hayır");//Kabul eden kişi PlayerId oluyomuş!!

                                    // İSTEK KABUL EDİLDİ 2. OYUNCUNUN(İsteği alan) EKRAN DEĞİŞİMİ
                                    Intent intent1 = new Intent(GameActivity.this, preGameActivity.class);
                                    intent1.putExtra("oyunModu", oyunModu);
                                    intent1.putExtra("odaIsmi", OdaIsmi);
                                    intent1.putExtra("email", email);
                                    intent1.putExtra("PlayerId", PlayerId);
                                    secondPlayerId = (String) snapshot.child("Istek").getValue();
                                    System.out.println(secondPlayerId);
                                    intent1.putExtra("secondPlayerId", secondPlayerId);//istek silinmeden önce de alınabilir.
                                    if (valueEventListener != null) {
                                        oldSelectedRoomRef.removeEventListener(valueEventListener);
                                        oldResponseRef.removeEventListener(valueEventListenerResponse);
                                    }
                                    finish();
                                    startActivity(intent1);
                                }
                            });
                            builder.setNegativeButton("Hayır", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Your negative button click handling
                                    ResponseRef.child("Istek").removeValue();

//                                  ResponseRef.child("MüsaitMi").setValue("Reddedildi");
                                }
                            });
                            final AlertDialog dialog = builder.create();
                            if (!isFinishing()) {
                                dialog.show();
                            }
                            new CountDownTimer(10000, 1000) { // 10 seconds countdown with 1 second interval
                                public void onTick(long millisUntilFinished) {
                                    dialog.setMessage("Size bir istek geldi. Kabul ediyor musunuz? (" + millisUntilFinished / 1000 + " saniye içinde otomatik reddedilecek)");
                                }

                                public void onFinish() {
                                    if (dialog.isShowing()) {
                                        dialog.dismiss();
                                        ResponseRef.child("Istek").removeValue();
//                                        ResponseRef.child("MüsaitMi").setValue("Reddedildi");
                                    }
                                }
                            }.start();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                // Eğer eski bir dinleyici varsa, onu kaldırıyoruz // bunu Response için de yap!!!!!!!!!!!
                if (valueEventListener != null) {
                    oldSelectedRoomRef.removeEventListener(valueEventListener);
                    oldResponseRef.removeValue();
                    oldResponseRef.removeEventListener(valueEventListenerResponse);
                }
                // Yeni bir ValueEventListener oluşturuyoruz
                valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        playersList.clear();
                        playerIdList.clear(); // eş zamanlı id list de tutuyoruz.
                        ArrayList<Integer> dizi = new ArrayList<>();
                        int i = 0;
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            // String oyuncuAdi = snapshot1.getValue().toString();
                            if (snapshot1.hasChild("OyuncuAdi")) {
                                String oyuncuAdi = snapshot1.child("OyuncuAdi").getValue(String.class);
                                int endIndex = oyuncuAdi.indexOf('@');
                                if (endIndex != -1) { // '@' işareti bulunursa
                                    oyuncuAdi = oyuncuAdi.substring(0, endIndex);
                                }
                                if (snapshot1.child("MüsaitMi").getValue().toString().equals("Hayır"))//arkaplanı değiştirmek için indeks tutma
                                    dizi.add(i);
                                i++;
                                playersList.add(oyuncuAdi);
                                playerIdList.add(snapshot1.getKey());
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(GameActivity.this, android.R.layout.simple_list_item_1, playersList) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = super.getView(position, convertView, parent);
                                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                                if (dizi.contains(position)) {
                                    textView.setBackgroundColor(Color.RED);
                                } else {
                                    textView.setBackgroundColor(Color.GREEN);
                                }
                                return view;
                            }
                        };
                        listView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                // Yeni ValueEventListener'ı ekleyin
                selectedRoomRef.addValueEventListener(valueEventListener);
                ResponseRef.addValueEventListener(valueEventListenerResponse);
                oldOdaIsmi = OdaIsmi;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                final DatabaseReference selectedRoomRef = db.getReference("4 Harfli/" + oyunModu);
                // Yeni bir ValueEventListener oluşturun
                //      valueEventListener = new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot snapshot) {
//                        playersList.clear();
//                        playerIdList.clear();//eş zamanlı id listesi de tutuyoruz
//                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
//                            String oyuncuAdi = snapshot1.child("OyuncuAdi").getValue(String.class);
//                            playersList.add(oyuncuAdi);
//                            playerIdList.add(snapshot1.getKey());
//                        }
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(GameActivity.this, android.R.layout.simple_list_item_1, playersList);
//                        listView.setAdapter(adapter);
//                    }
//
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError error) {
//
//                    }
//                };
//                // Yeni ValueEventListener'ı ekleyin
//                selectedRoomRef.addValueEventListener(valueEventListener);
            }
        });
        don.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                RoomsRef = db.getReference(OdaIsmi + "/" + oyunModu);
//                RoomsRef.child(PlayerId).child("MüsaitMi").setValue("Evet");
//                RoomsRef.child(PlayerId).child("OyuncuAdi").setValue(email);
//                kaclikOda = OdaIsmi;
//                System.out.println(kaclikOda);
                RoomsRef = db.getReference(OdaIsmi + "/" + oyunModu + "/" + PlayerId);
                RoomsRef.removeValue();
                Intent intent = new Intent(GameActivity.this, MainActivity2.class);// BURASI DÜZELTİLECEK
                intent.putExtra("email", email);
                intent.putExtra("PlayerId", PlayerId);
                finish();
                startActivity(intent);
            }
        });

    }

    protected void onPause() {
        super.onPause();
        System.out.println("Pause girdi");
        // deletePlayerData();
    }

    private void deletePlayerData() {
        if (kaclikOda.equals("4 Harfli")) { //SİLİNMELER YAPILACAK
            RoomsRef.child(PlayerId).removeValue();
        } else if (kaclikOda.equals("5 Harfli")) {
            // Letters5Ref.child(id).removeValue(); Bunları değiştirmeyi unutma
        } else if (kaclikOda.equals("6 Harfli")) {
            // Letters6Ref.child(id).removeValue();
        } else if (kaclikOda.equals("7 Harfli")) {
            // Letters7Ref.child(id).removeValue();
        } else {
            System.out.println("Oyuncu bir odada değil!!");
        }
    }

}