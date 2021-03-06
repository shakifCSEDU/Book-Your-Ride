package com.example.bus;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CanPayActivity extends AppCompatActivity {

    private TextView busNameTextView,journeyDateTextView,busConditionTextView,numberOfSeatsTextView,totalCostsTextView,fromTextView,toTextView;
    private Button button;
    private CardView bKashCardView,rocketCardView,mCashCardView,nagadCardView;
    private LayoutInflater layoutInflater;
    private View view;
    private  EditText userNumber;
    private String BusName,JourneyDate,BusCondition,FromCity, ToCity, totalCosts,numberOfSeats,seatsName,busID ,time,fare,issueDate,issueTime;
    private  String payContactNo ;
    Map<String,String> seatMap;
    private String payContactNumber = "";


    private String isSelected = null;



    private FirebaseDatabase db;
    private DatabaseReference root;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_can_pay);
        Intent intent = getIntent();
        BusName = intent.getStringExtra("busName").toString();
        JourneyDate = intent.getStringExtra("journeyDate").toString();
        BusCondition = intent.getStringExtra("busCondition").toString();
        FromCity = intent.getStringExtra("fromCity").toString();
        ToCity = intent.getStringExtra("toCity").toString();
        busID = intent.getStringExtra("busID").toString();
        time = intent.getStringExtra("time").toString();
        fare = intent.getStringExtra("Fare".toString());
        numberOfSeats = intent.getStringExtra("numberOfSeats").toString();
        totalCosts = intent.getStringExtra("totalCosts").toString();
        seatMap = (Map<String, String>)intent.getSerializableExtra("seatMap");
        seatsName = "";


        for (Map.Entry<String, String> entry : seatMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if(value.equals("1")){
                seatsName = seatsName + " " + key;
            }
        }

        busNameTextView = (TextView)findViewById(R.id.busFareID);
        fromTextView =(TextView)findViewById(R.id.fromID);
        toTextView = (TextView)findViewById(R.id.toID);
        journeyDateTextView = (TextView)findViewById(R.id.busJourneyDateId);
        busConditionTextView = (TextView)findViewById(R.id.busConditionId);
        numberOfSeatsTextView = (TextView)findViewById(R.id.totalSeatId);
        totalCostsTextView = (TextView)findViewById(R.id.totalCostId);
        button = (Button)findViewById(R.id.payButtonId);

        bKashCardView = (CardView)findViewById(R.id.bkashCardViewId);
        mCashCardView = (CardView)findViewById(R.id.mCashCardViewId);
        rocketCardView = (CardView)findViewById(R.id.rocketCardViewId);
        nagadCardView = (CardView)findViewById(R.id.nagadCardViewId);



        layoutInflater = LayoutInflater.from(getApplicationContext());
        busNameTextView.setText(BusName);
        fromTextView.setText(FromCity);
        toTextView.setText(ToCity);
        journeyDateTextView.setText(JourneyDate);
        busConditionTextView.setText(BusCondition);
        numberOfSeatsTextView.setText(seatsName + " (" +numberOfSeats+")");
        totalCostsTextView.setText(totalCosts);




        bKashCardView.setOnClickListener(this::onClick);
        nagadCardView.setOnClickListener(this::onClick);
        rocketCardView.setOnClickListener(this::onClick);
        mCashCardView.setOnClickListener(this::onClick);
        button.setOnClickListener(this::onClick);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel("BookYourRide","BookYourRide",NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }


    }


    public void onClick(View v) {

        if(v.getId() == R.id.bkashCardViewId){
            setCardView("Bkash",bKashCardView,R.drawable.bkash);
        }
        if(v.getId() == R.id.nagadCardViewId){
            setCardView("Nagad",nagadCardView,R.drawable.nagad);
        }
        if(v.getId() == R.id.mCashCardViewId){
            setCardView("Mcash",mCashCardView,R.drawable.mcash);
        }
        if(v.getId() == R.id.rocketCardViewId){
            setCardView("Rocket",rocketCardView,R.drawable.rocket);
        }



        //System.out.println(payContactNo);
        if(v.getId() == R.id.payButtonId) {
            if(!payContactNumber.isEmpty()){
                String message = "Your seat booking for " + BusName + " bus is confirmed";

                NotificationCompat.Builder builder = new NotificationCompat.Builder(CanPayActivity.this, "BookYourRide");
                builder.setContentTitle("Ticket Booking Successfull");
                builder.setContentText(message);
                builder.setSmallIcon(R.drawable.ic_messagenoti);
                builder.setAutoCancel(true);
                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(CanPayActivity.this);
                notificationManagerCompat.notify(1, builder.build());


                Intent intent1 = new Intent(getApplicationContext(), BookingFinish.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent1.putExtra("noti", message);

                db = FirebaseDatabase.getInstance("https://buss-886c2-default-rtdb.asia-southeast1.firebasedatabase.app/");
                root = db.getReference("SeatDetails").child(busID).child(JourneyDate);
                intent1.putExtra("busName", BusName);
                intent1.putExtra("from", FromCity);
                intent1.putExtra("to", ToCity);
                intent1.putExtra("noti", message);
                intent1.putExtra("journeyDate", JourneyDate);
                intent1.putExtra("Fare", fare);
                intent1.putExtra("seatName", seatsName);
                intent1.putExtra("time", time);
                intent1.putExtra("busCondition", BusCondition);


                root.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            int index;
                            for (index = 1; index <= 24; index++) {
                                String seatIndex = "A" + index;
                                if (seatMap.get(seatIndex).equals("1")) {
                                    root.child(seatIndex).setValue("1");
                                }
                            }
                        } else {
                            root.setValue(seatMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    //  Toast.makeText(CanPayActivity.this, "Databsse created", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull @NotNull Exception e) {
                                    // Toast.makeText(CanPayActivity.this, "Databse creation failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
                intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
            }

            else{
                Toast.makeText(this, "Please give your payment contact number", Toast.LENGTH_SHORT).show();
            }

        }


    }

    private void setCardView(String Name, CardView cardView, int image) {

        if(isSelected == null){
            cardView.setCardBackgroundColor(Color.parseColor("#6495ED"));
            isSelected = Name;
            SetAlertDialogue(Name,image);
        }else if(isSelected.equals(Name)){
            cardView.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
            Toast.makeText(getApplicationContext(),"Unselected "+Name+"",Toast.LENGTH_SHORT).show();
            isSelected = null;
        }
    }

    private void SetAlertDialogue(String name,int image){


        view = layoutInflater.inflate(R.layout.payment_set,null,false);
        userNumber = view.findViewById(R.id.phoneNumberId);

        EditText otp = view.findViewById(R.id.otpId);
        payContactNo = userNumber.getText().toString();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CanPayActivity.this);
        alertDialogBuilder.setTitle(name);
        alertDialogBuilder.setIcon(image);
        alertDialogBuilder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        payContactNumber = userNumber.getText().toString();
                    }
                });

        alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.setView(view);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }
}