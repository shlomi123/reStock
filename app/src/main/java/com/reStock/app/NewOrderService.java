package com.reStock.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class NewOrderService extends Service {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String company_email;
    private SharedPreferences sharedPreferences;

    private int NOTIFICATION = 1; // Unique identifier for our notification
    public static boolean isRunning = false;
    public static NewOrderService instance = null;
    private NotificationManager notificationManager = null;

    public NewOrderService() {
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        instance = this;
        isRunning = true;
        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        instance = null;

        notificationManager.cancel(NOTIFICATION); // Remove notification

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent,flags,startId);


        company_email = intent.getStringExtra("DISTRIBUTOR_EMAIL");

        db.collection("Companies")
                .document(company_email)
                .collection("Orders")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }

                        // create recycler view to show stores and their data
                        if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange documentChange : queryDocumentSnapshots.getDocumentChanges()) {
                                int index = documentChange.getNewIndex();
                                DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(index);
                                String id = sharedPreferences.getString("OLD_DOCUMENT_ID", null);

                                if (id != null){
                                    if (!id.equals(documentSnapshot.getId())){
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString("OLD_DOCUMENT_ID", documentSnapshot.getId());
                                        editor.apply();

                                        Order order = documentSnapshot.toObject(Order.class);
                                        sendNotification(order);
                                    }
                                }else {
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("OLD_DOCUMENT_ID", documentSnapshot.getId());
                                    editor.apply();

                                    Order order = documentSnapshot.toObject(Order.class);
                                    sendNotification(order);
                                }

                                /*switch (change){
                                    case ADDED:
                                        Order order = documentChange.getDocument().toObject(Order.class);
                                        sendNotification(order);
                                }*/
                            }
                        }
                    }
                });

        /*int i = 10000000;
        for (int e = 0; e < i; e++) {
            Toast.makeText(getApplicationContext(), Integer.toString(e), Toast.LENGTH_SHORT).show();
        }*/

        return Service.START_STICKY;
    }

    private void sendNotification(Order order){
        String msg = order.get_store_name() + " ordered " + order.get_quantity() + " " + order.get_product();

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, ADMIN_MAIN_PAGE.class), 0);

        // Set the info for the views that show in the notification panel.
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher_round)          // the status text
                .setWhen(System.currentTimeMillis())       // the time stamp
                .setContentTitle("Order")                 // the label of the entry
                .setContentText(msg)      // the content of the entry
                .setAutoCancel(true)
                .setContentIntent(contentIntent)           // the intent to send when the entry is clicked
                .build();

        // Start service in foreground mode
        startForeground(NOTIFICATION, notification);
    }
}
