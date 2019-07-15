package com.reStock.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

//TODO work on ui
public class STORE_ORDER_PRODUCT extends AppCompatActivity {

    private String store_email;
    private String store_name;
    private String distributor_email;
    private String distributor_name;
    private String product_name;
    private double product_cost;
    private String product_img_url;
    private int product_units_per_package;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button button;
    private ImageView imageView;
    private TextView textView;
    private TextView textView_units;
    private EditText editText;
    private CircularProgressDrawable circularProgressDrawable;
    private boolean mailClientOpened = false;
    private int quantity;
    private double total_cost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store__order__product);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        store_email = sharedPreferences.getString("STORE_EMAIL", null);
        store_name = sharedPreferences.getString("STORE_NAME", null);

        distributor_email = getIntent().getStringExtra("DISTRIBUTOR_EMAIL");
        distributor_name = getIntent().getStringExtra("DISTRIBUTOR_NAME");
        product_cost = getIntent().getDoubleExtra("PRODUCT_COST", 0);
        product_name = getIntent().getStringExtra("PRODUCT_NAME");
        product_img_url = getIntent().getStringExtra("PRODUCT_IMG_URL");
        product_units_per_package = getIntent().getIntExtra("PRODUCT_UNITS_PER_PACKAGE", 0);

        //initialize views
        button = (Button) findViewById(R.id.buttonOrderSpecificProduct);
        imageView = (ImageView) findViewById(R.id.imageViewImageOfProduct);
        textView = (TextView) findViewById(R.id.textViewProductName1);
        textView_units = (TextView) findViewById(R.id.textViewUnitsPerPackage);
        editText = (EditText) findViewById(R.id.editTextQuantity);
        circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
        circularProgressDrawable.setStrokeWidth(10f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();

        //set text view to product name
        textView.setText(product_name);
        textView_units.setText("one order contains " + String.valueOf(product_units_per_package) + " units of product");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editText.getText().toString().trim().equals(""))
                {
                    Toast.makeText(getApplicationContext(), "Pick a Quantity to Order", Toast.LENGTH_LONG).show();
                }
                else
                {
                    quantity = Integer.parseInt(editText.getText().toString());
                    //first verify that the users wants to place the order
                    showAlertDialog(STORE_ORDER_PRODUCT.this);
                }
            }
        });

        setProductPicture();
    }

    //getting picture of product
    private void setProductPicture()
    {
        //load pic into image view with url
        StorageReference storageReference = storage.getReferenceFromUrl(product_img_url);
        GlideApp.with(getApplicationContext())
                .load(storageReference)
                .fitCenter()
                .placeholder(circularProgressDrawable)
                .into(imageView);
    }

    //alert dialog
    private void showAlertDialog(final Context context)
    {
        total_cost = product_units_per_package * product_cost * quantity;

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Place Order");
        // make text according to entered quantity
        if (quantity == 1) {
            alertDialog.setMessage("You want to order " + editText.getText().toString() + " box of product\n\n"
                    + "Total cost: " + String.valueOf(total_cost) + "$");
        }
        else if(quantity == 0) {
            alertDialog.setMessage("Please place an order of more that one product");
        }
        else {
            alertDialog.setMessage("You want to order " + editText.getText().toString() + " boxes of product\n\n"
                    + "Total cost: " + String.format("%.2f", total_cost) + "$");
        }

        //if the user verifies the order
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (quantity == 0)
                {
                    dialogInterface.dismiss();
                }
                else
                {
                    //first make sure he sends the email before order is logged
                    orderProduct(getApplicationContext());
                    dialogInterface.dismiss();
                }
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    //log the order
    private void orderProduct(final Context context)
    {
        mailClientOpened = true;
        final Order order = new Order(Timestamp.now().toDate(), product_name, quantity, distributor_name, product_img_url, total_cost, store_email, store_name);

        db.collection("Companies")
                .document(distributor_email)
                .collection("Orders")
                .add(order)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        db.collection("Stores")
                                .document(store_email)
                                .collection("Orders")
                                .add(order)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            //send mail
                                            SendMail sendMail = new SendMail(STORE_ORDER_PRODUCT.this, distributor_email, "Product Order",
                                                    "Retailer: " + store_name + "\n" +
                                                            "Order: " + String.valueOf(quantity) + " boxes of " + product_name + "\n\n" +
                                                            "Total cost: " + String.format("%.2f", total_cost) + "$", store_name, 0);

                                            sendMail.execute();
                                        } else {
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }
}
