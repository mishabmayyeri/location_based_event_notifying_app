package com.rangetech.eventnotify.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rangetech.eventnotify.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class PostActivity extends AppCompatActivity {

    private static final String TAG = "Places";
    private static final String POSTING = "Posting operation";
    private ImageView mSelectImage;
    private EditText mPostTitle;
    private EditText mPostDesc;
    private Button mSubmitBtn;
    private Uri mImageUri = null;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private StorageReference mDatabase;
    private ProgressDialog mProgress;
    private Toolbar newPostToolbar;
    private ProgressBar newPostProgress;
    private String current_user_id;
    private Bitmap compressedImageFile;
    private DatePickerDialog.OnDateSetListener dateSetListener;
    private String eventTime;
    private CheckBox dateofCompletionCheckbox;
    private String placeLong="";
    private String placeLat="";
    private String placeName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        newPostToolbar = findViewById(R.id.new_post_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        current_user_id = firebaseAuth.getCurrentUser().getUid();
        mDatabase = FirebaseStorage.getInstance().getReference().child("Event");

        mSelectImage = findViewById(R.id.imageSelect);
        mPostTitle = findViewById(R.id.titleField);
        mPostDesc = findViewById(R.id.descField);
        mSubmitBtn = findViewById(R.id.submitBtn);
        newPostProgress = findViewById(R.id.new_post_progress);
        dateofCompletionCheckbox=findViewById(R.id.timeField);

        mSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(360, 240)
                        .setAspectRatio(1, 1)
                        .start(PostActivity.this);


            }
        });

        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPosting();
            }
        });
        mProgress = new ProgressDialog(this);
        String apiKey = getString(R.string.api_key2);

        /**
         * Initialize Places. For simplicity, the API key is hard-coded. In a production
         * environment we recommend using a secure mechanism to manage API keys.
         */
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(this);
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME,Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+",  LAT LONG"+place.getLatLng().latitude +","+place.getLatLng().longitude);
                placeName=place.getName();
                placeLat=place.getLatLng().latitude+"";
                placeLong=place.getLatLng().longitude+"";

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        dateofCompletionCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dateofCompletionCheckbox.setChecked(false);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        PostActivity.this,
                        dateSetListener,
                        year, month, day
                );
                dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
                dialog.getDatePicker().setMaxDate(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 10);
                dialog.show();
            }
        });

        dateSetListener = (datePicker, year, month, day) -> {

            month = month + 1;
            if (month < 10) {
                eventTime = day + "-" + "0" + month + "-" + year;
                dateofCompletionCheckbox.setText(eventTime);
                dateofCompletionCheckbox.setChecked(true);
            } else {
                eventTime = day + "-" + month + "-" + year;
                dateofCompletionCheckbox.setText(eventTime);
                dateofCompletionCheckbox.setChecked(true);
            }

        };



    }




    private void startPosting() {


        final String title_val =  mPostTitle.getText().toString();
        final String desc_val = mPostDesc.getText().toString();

        if (!TextUtils.isEmpty(title_val) && !TextUtils.isEmpty(desc_val) && mImageUri != null
        &&placeLat!=null&&placeName!=null&&placeLong!=null&&eventTime!=null){
            mProgress.setMessage("Posting to Blog...");
            mProgress.show();
            newPostProgress.setVisibility(View.VISIBLE);
            final String randomName = UUID.randomUUID().toString();
            final StorageReference filepath = storageReference.child("event_images").child(randomName + ".jpg");
            filepath.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        File newImageFile = new File(mImageUri.getPath());
                        try {
                            compressedImageFile = new Compressor(PostActivity.this)
                                    .setMaxWidth(360)
                                    .setMaxHeight(240)
                                    .setQuality(90)
                                    .compressToBitmap(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG,100,baos);
                        byte[] thumbData = baos.toByteArray();

                        final UploadTask thumbFilePath = storageReference.child("event_images/thumbs").child(randomName + ".jpg").putBytes(thumbData);
                        thumbFilePath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                StorageReference thumbFilePath = storageReference.child("event_images/thumbs").child(randomName + ".jpg");
                                thumbFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        final String downloadThumbUri = uri.toString();
                                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String downloadUrl = uri.toString();

                                        String album_id=firebaseFirestore.collection("Posts")
                                                .document().getId();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUrl);
                                        postMap.put("title", title_val);
                                        postMap.put("desc", desc_val);
                                        postMap.put("thumb",downloadThumbUri);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        postMap.put("album_id",album_id);
                                        postMap.put("event_date",eventTime);
                                        postMap.put("location_name",placeName);
                                        postMap.put("location_lat",placeLat);
                                        postMap.put("location_long",placeLong);
                                        postMap.put("participated","no");

                                        firebaseFirestore.collection("Posts")
                                                .document(album_id)
                                                .set(postMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){


                                                            Toast.makeText(PostActivity.this, "Post was added",Toast.LENGTH_LONG).show();
                                                            Intent mainintent = new Intent(PostActivity.this, EventActivity.class);
                                                            startActivity(mainintent);
                                                            finish();

                                                        }else{
                                                            Log.i(POSTING,task.getException().getMessage());
                                                        }
                                                        mProgress.dismiss();

                                                    }
                                                });
                                    }
                                });
                            }
                        });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
                    } else {
                        newPostProgress.setVisibility(View.INVISIBLE);
                    }
                }
            });

        }else{
            Toast.makeText(getApplicationContext(),"Please fill up all forms",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mSelectImage.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
