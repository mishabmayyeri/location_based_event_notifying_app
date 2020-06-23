package com.rangetech.eventnotify;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.crowdfire.cfalertdialog.CFAlertDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.androidhive.barcode.BarcodeReader;

public class QRCodeActivityReader extends AppCompatActivity implements BarcodeReader.BarcodeReaderListener {
    private static final String QR_CODE_ = "QR_CODE_READER";
    ProgressBar qrcodeReaderProgressbar;
    private BarcodeReader barcodeReader;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String currentUser;
    private DocumentReference docIdRef;
    private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_code_reader);

        qrcodeReaderProgressbar = findViewById(R.id.qrcodeReaderProgressbar);
        barcodeReader = (BarcodeReader) getSupportFragmentManager().findFragmentById(R.id.barcode_fragment);
        firebaseFirestore=FirebaseFirestore.getInstance();
        firebaseAuth=FirebaseAuth.getInstance();
        currentUser=firebaseAuth.getUid();
        albumId=getIntent().getStringExtra("data");
    }

    @Override
    public void onScanned(final Barcode barcode) {
        barcodeReader.pauseScanning();
        // play beep sound
        barcodeReader.playBeep();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                CFAlertDialog.Builder builder = new CFAlertDialog.Builder(QRCodeActivityReader.this)
                        .setDialogStyle(CFAlertDialog.CFAlertStyle.BOTTOM_SHEET)
                        .setTitle("Admit User ?")
                        .setIcon(R.drawable.ic_check_circle_black_24dp)
                        .setMessage(barcode.displayValue)
                        .setCancelable(false)
                        .addButton("    YES   ", Color.parseColor("#fafafa"), Color.parseColor("#3e3d63"), CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, int which) {

                                        docIdRef = firebaseFirestore.collection("Users")
                                                .document(barcode.displayValue)
                                                .collection("MyEvents")
                                                .document(albumId);

                                        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull final Task<DocumentSnapshot> task) {

                                                if (task.isSuccessful()) {

                                                    final DocumentSnapshot document = task.getResult();

                                                    if (document.exists()) {
                                                        Log.i(QR_CODE_, "Document exists!");
                                                        Map<String, Object> postMap = new HashMap<>();
                                                        postMap.put("participated","yes");
                                                        docIdRef.update(postMap)
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    Toast.makeText(getApplicationContext(),"Successfully admitted",Toast.LENGTH_LONG).show();
                                                                    dialog.dismiss();

                                                                }
                                                            }
                                                        });
                                                    }else{
                                                        Toast.makeText(getApplicationContext(),"This user has not registered , Please ask him to register the event.",Toast.LENGTH_SHORT).show();
                                                        dialog.dismiss();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }).addButton("    NO  ", Color.parseColor("#3e3d63"), Color.parseColor("#e0e0e0"), CFAlertDialog.CFAlertActionStyle.DEFAULT,
                                CFAlertDialog.CFAlertActionAlignment.JUSTIFIED,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();

                                    }
                                });
                builder.show();
            }
        });
    }

    @Override
    public void onScannedMultiple(List<Barcode> barcodes) {

    }

    @Override
    public void onBitmapScanned(SparseArray<Barcode> sparseArray) {

    }

    @Override
    public void onScanError(String errorMessage) {

    }

    @Override
    public void onCameraPermissionDenied() {

    }
}

