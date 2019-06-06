package com.reStock.app;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;

public class ADMIN_EDIT_PRODUCT extends AppCompatActivity {

    private ImageView edit4;
    private ImageView edit3;
    private ImageView edit2;
    private ImageView check2;
    private ImageView check3;
    private ImageView check4;
    private TextView product_name;
    private EditText edit_product_name;
    private TextView price_per_unit;
    private EditText edit_price_per_unit;
    private TextView units_per_package;
    private EditText edit_units_per_package;
    private ProgressBar progress_price;
    private ProgressBar progress_name;
    private ProgressBar progress_units;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sharedPreferences;
    private String email;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private CircleImageView product_image;
    private ImageView placeholder;
    private Button choose_file;
    private CircularProgressDrawable circularProgressDrawable;
    private String image_path;
    private Uri mImageUri;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String product_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin__edit__product);

        Gson gson = new Gson();
        final Product product = gson.fromJson(getIntent().getStringExtra("JSON"), Product.class);
        image_path = product.getImageUrl();

        sharedPreferences = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        email = mAuth.getCurrentUser().getEmail();

        progressBar = (ProgressBar) findViewById(R.id.progressBar_product_new_image);
        product_image = (CircleImageView) findViewById(R.id.imageView_edit_product_image);
        placeholder = (ImageView) findViewById(R.id.imageView_edit_product_image_placeholder);
        choose_file = (Button) findViewById(R.id.button_open_file_chooser_for_product);
        edit4 = (ImageView) findViewById(R.id.imageView_edit_4);
        edit3 = (ImageView) findViewById(R.id.imageView_edit_3);
        edit2 = (ImageView) findViewById(R.id.imageView_edit_2);
        check2 = (ImageView) findViewById(R.id.imageView_check_2);
        check3 = (ImageView) findViewById(R.id.imageView_check_3);
        check4 = (ImageView) findViewById(R.id.imageView_check_4);
        progress_name = (ProgressBar) findViewById(R.id.progressBar_update_product_name);
        progress_price = (ProgressBar) findViewById(R.id.progressBar_update_price_per_unit);
        progress_units = (ProgressBar) findViewById(R.id.progressBar_update_units_per_package);
        product_name = (TextView) findViewById(R.id.textView_edit_product_name);
        edit_product_name = (EditText) findViewById(R.id.editText_edit_product_name);
        price_per_unit = (TextView) findViewById(R.id.textView_price_per_unit);
        edit_price_per_unit = (EditText) findViewById(R.id.editText_price_per_unit);
        units_per_package = (TextView) findViewById(R.id.textView_units_per_package);
        edit_units_per_package = (EditText) findViewById(R.id.editText_units_per_package);

        progressBar.setVisibility(View.INVISIBLE);
        product_image.setVisibility(View.INVISIBLE);

        product_name.setText(product.getName());
        edit_product_name.setVisibility(View.INVISIBLE);
        check2.setVisibility(View.INVISIBLE);
        progress_name.setVisibility(View.INVISIBLE);

        price_per_unit.setText(String.valueOf(product.getCost()));
        edit_price_per_unit.setVisibility(View.INVISIBLE);
        check3.setVisibility(View.INVISIBLE);
        progress_price.setVisibility(View.INVISIBLE);

        units_per_package.setText(String.valueOf(product.getUnits_per_package()));
        edit_units_per_package.setVisibility(View.INVISIBLE);
        check4.setVisibility(View.INVISIBLE);
        progress_units.setVisibility(View.INVISIBLE);

        // set image of product
        circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
        circularProgressDrawable.start();
        final StorageReference storageReference = storage.getReferenceFromUrl(image_path);
        storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {

                GlideApp.with(getApplicationContext())
                        .load(storageReference)
                        .fitCenter()
                        .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                        .placeholder(circularProgressDrawable)
                        .into(product_image);

                product_image.setVisibility(View.VISIBLE);
                placeholder.setVisibility(View.INVISIBLE);
            }
        });

        //set product id
        getProductId();

        // open file chooser
        choose_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });


        // edit name
        edit4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                product_name.setVisibility(View.INVISIBLE);
                edit_product_name.setVisibility(View.VISIBLE);
                edit_product_name.setText("");
                edit4.setVisibility(View.INVISIBLE);
                check4.setVisibility(View.VISIBLE);
            }
        });

        check4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_product_name.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "please enter something", Toast.LENGTH_SHORT).show();
                } else {
                    progress_name.setVisibility(View.VISIBLE);
                    check4.setVisibility(View.INVISIBLE);

                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    db.collection("Companies")
                            .document(email)
                            .collection("Products")
                            .document(product_id)
                            .update("name", edit_product_name.getText().toString().trim())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    product_name.setVisibility(View.VISIBLE);
                                    edit_product_name.setVisibility(View.INVISIBLE);
                                    edit4.setVisibility(View.VISIBLE);
                                    progress_name.setVisibility(View.INVISIBLE);
                                    product_name.setText(edit_product_name.getText().toString());
                                    Toast.makeText(getApplicationContext(), "succesfully upated", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // edit units per package
        edit3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                units_per_package.setVisibility(View.INVISIBLE);
                edit_units_per_package.setVisibility(View.VISIBLE);
                edit_units_per_package.setText("");
                edit3.setVisibility(View.INVISIBLE);
                check3.setVisibility(View.VISIBLE);
            }
        });

        check3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_units_per_package.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "please enter something", Toast.LENGTH_SHORT).show();
                } else {
                    progress_units.setVisibility(View.VISIBLE);
                    check3.setVisibility(View.INVISIBLE);

                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    db.collection("Companies")
                            .document(email)
                            .collection("Products")
                            .document(product_id)
                            .update("units_per_package", Integer.parseInt(edit_units_per_package.getText().toString().trim()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    units_per_package.setVisibility(View.VISIBLE);
                                    edit_units_per_package.setVisibility(View.INVISIBLE);
                                    edit3.setVisibility(View.VISIBLE);
                                    check3.setVisibility(View.INVISIBLE);
                                    units_per_package.setText(edit_units_per_package.getText().toString());
                                    progress_units.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), "succesfully upated", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // edit price per unit
        edit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                price_per_unit.setVisibility(View.INVISIBLE);
                edit_price_per_unit.setVisibility(View.VISIBLE);
                edit_price_per_unit.setText("");
                edit2.setVisibility(View.INVISIBLE);
                check2.setVisibility(View.VISIBLE);
            }
        });

        check2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edit_price_per_unit.getText().toString().trim().equals("")) {
                    Toast.makeText(getApplicationContext(), "please enter something", Toast.LENGTH_SHORT).show();
                } else {
                    progress_price.setVisibility(View.VISIBLE);
                    check2.setVisibility(View.INVISIBLE);

                    try {
                        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    db.collection("Companies")
                            .document(email)
                            .collection("Products")
                            .document(product_id)
                            .update("cost", Double.parseDouble(edit_price_per_unit.getText().toString().trim()))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    price_per_unit.setVisibility(View.VISIBLE);
                                    edit_price_per_unit.setVisibility(View.INVISIBLE);
                                    edit2.setVisibility(View.VISIBLE);
                                    check2.setVisibility(View.INVISIBLE);
                                    price_per_unit.setText(edit_price_per_unit.getText().toString());
                                    progress_price.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), "succesfully upated", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();

            product_image.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);

            StorageReference storageReference = storage.getReferenceFromUrl(image_path);

            storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        StorageReference newStorageReference = FirebaseStorage.getInstance()
                                .getReference("Products")
                                .child(product_name.getText().toString().trim() + "." + getFileExtension(mImageUri));

                        newStorageReference.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String path = uri.toString();
                                        image_path = path;


                                        db.collection("Companies")
                                                .document(email)
                                                .collection("Products")
                                                .document(product_id)
                                                .update("imageUrl", path)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            // show new image
                                                            placeholder.setVisibility(View.VISIBLE);
                                                            progressBar.setVisibility(View.INVISIBLE);

                                                            circularProgressDrawable = new CircularProgressDrawable(getApplicationContext());
                                                            circularProgressDrawable.start();
                                                            final StorageReference storageReference = storage.getReferenceFromUrl(image_path);
                                                            storageReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                                                @Override
                                                                public void onSuccess(StorageMetadata storageMetadata) {

                                                                    GlideApp.with(getApplicationContext())
                                                                            .load(storageReference)
                                                                            .fitCenter()
                                                                            .signature(new ObjectKey(storageMetadata.getCreationTimeMillis()))
                                                                            .placeholder(circularProgressDrawable)
                                                                            .into(product_image);
                                                                    placeholder.setVisibility(View.INVISIBLE);
                                                                    product_image.setVisibility(View.VISIBLE);
                                                                }
                                                            });
                                                        }
                                                    }
                                                });
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                                progressBar.setProgress((int) progress);
                            }
                        });

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void getProductId() {
        db.collection("Companies")
                .document(email)
                .collection("Products")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            Product currProduct = documentSnapshot.toObject(Product.class);

                            if (currProduct.getName().equals(product_name.getText().toString()) && currProduct.getImageUrl().equals(image_path)) {
                                product_id = documentSnapshot.getId();
                            }
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(0, 0);
    }
}
