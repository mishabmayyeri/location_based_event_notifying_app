package com.rangetech.eventnotify.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.rangetech.eventnotify.R;

public class QRcode extends AppCompatActivity {
    String data="Hi Hello How are you";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_rcode);
        data="Hi How are you";
        data=getIntent().getStringExtra("data");

        final ImageView QRCodeImageView = findViewById(R.id.qr_code_imageview);

        final MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        if(data!=null) {
            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                QRCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Unable to generate QR Code", Toast.LENGTH_LONG);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                QRCodeImageView.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Unable to generate QR Code", Toast.LENGTH_LONG);
            }catch (NullPointerException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Unable to generate QR Code", Toast.LENGTH_LONG);

            }
        }else {
            Toast.makeText(getApplicationContext(), "Unable to generate QR Code ", Toast.LENGTH_LONG);
        }
    }
}
