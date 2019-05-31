package com.reStock.app;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

import static android.content.Context.WINDOW_SERVICE;

public class ScanFragment extends Fragment {

    private String code;
    private Button generate;
    private ImageView qr;
    private String company_email;
    private String company_name;
    private Bitmap bitmap;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String savePath;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.scan_fragment, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        company_email = mAuth.getCurrentUser().getEmail();
        company_name = mAuth.getCurrentUser().getDisplayName();
        generate = (Button) getActivity().findViewById(R.id.button_save_qr);
        qr = (ImageView) getActivity().findViewById(R.id.imageView_text_value_option);
        savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getPath() + "/" + company_name +"/QRCode/";

        code = company_name + "##" + company_email;

        WindowManager manager = (WindowManager) getActivity().getSystemService(WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int width = point.x;
        int height = point.y;
        int smallerDimension = width < height ? width : height;
        smallerDimension = smallerDimension * 3 / 4;

        QRGEncoder qrgEncoder = new QRGEncoder(code, null, QRGContents.Type.TEXT, smallerDimension);

        try {
            bitmap = qrgEncoder.encodeAsBitmap();
            qr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Log.d("blaaaa", e.toString());
        }


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean save;
                String result;
                try {
                    save = QRGSaver.save(savePath, "qr code", bitmap, QRGContents.ImageType.IMAGE_JPEG);
                    result = save ? "Image Saved to documents" : "Image Not Saved";
                    Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
