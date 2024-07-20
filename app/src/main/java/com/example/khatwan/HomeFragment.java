package com.example.khatwan;

import static android.app.Activity.RESULT_OK;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import com.abedelazizshe.lightcompressorlibrary.CompressionListener;
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor;
import com.abedelazizshe.lightcompressorlibrary.VideoQuality;
import com.abedelazizshe.lightcompressorlibrary.config.AppSpecificStorageConfiguration;
import com.abedelazizshe.lightcompressorlibrary.config.Configuration;
import com.abedelazizshe.lightcompressorlibrary.config.SaveLocation;
import com.abedelazizshe.lightcompressorlibrary.config.SharedStorageConfiguration;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    List<Uri> videoUri;
    MaterialButton btnUploadPost;
    MaterialButton btnPickVideo;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView tvResult;
    VideoView vidView;
    public HomeFragment() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);
        initializer(view);
        btnPickVideo.setOnClickListener(view12 -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(Intent.createChooser(intent,"Pick Up Video"),2);
        });
        btnUploadPost.setOnClickListener(view1 -> {
            if (videoUri!=null) {
                Log.e(TAG, "Uploading Post Btn Clicked : Video URI : " + videoUri.get(0).toString() );
                List<String> videoNames = new ArrayList<>();
                videoNames.add("firstVideo");
                Log.e(TAG, "SIZES of Lists : " + videoNames.size() + " : " + videoUri.size() );
                try {
                    VideoCompressor.start(requireContext(),videoUri,false,
                            new SharedStorageConfiguration(
                                    SaveLocation.movies, // default is movies
                                    "khatwan-compressed-videos" // optional subFolderName
                            ),
                            new AppSpecificStorageConfiguration(
                                    "khatwan-compressed-videos" // optional subFolderName
                            ),
                            new Configuration(VideoQuality.MEDIUM,true,2,false,false,360.0,480.0 ,videoNames)
                            ,new CompressionListener() {
                                @Override
                                public void onProgress(int index, float percent) {}
                                @Override
                                public void onStart(int index) {}
                                @Override
                                public void onSuccess(int index, long size, String path) {
                                    Log.e(TAG, "onSuccess: Compression of Video Success!" );
                                }
                                @Override
                                public void onFailure(int index, @NonNull String failureMessage) {
                                    Log.e(TAG, "onFailure: " + failureMessage );
                                }
                                @Override
                                public void onCancelled(int index) {
                                    Log.e(TAG, "onFailure: at index : " + index );
                                }
                            }
                    );
                }
                catch (Exception e) {
                    tvResult.setText(tvResult.getText() + "\n Error : " + e.getMessage());
                }
            }
        });
        return view;
    }

    private void initializer(View view) {
        //firebse
        reference= FirebaseDatabase.getInstance().getReference().child("VideosPosts");
        storageReference= FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        videoUri = new ArrayList<>();

        //ids initialization
        vidView=view.findViewById(R.id.vidView);
        tvResult=view.findViewById(R.id.tvResult);
        btnPickVideo=view.findViewById(R.id.post_video_layout);
        btnUploadPost=view.findViewById(R.id.upload_video_post_btn);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2 && resultCode==RESULT_OK && data!=null) {
            videoUri.add(data.getData());
            tvResult.setText("Uri Value : " + data.getDataString());
            vidView.setVideoURI(data.getData());
            vidView.start();
        }
    }

    private void uploadVideoWithoutText(Uri viduri) {
        StorageReference fileRef= storageReference.child("Videos/"+System.currentTimeMillis()+".mp4");
        fileRef.putFile(viduri).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uid=user.getUid();
                    DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String Uname="";
                            String profileImageUrl="";
                            String imgUrl= uri.toString();
                            String post_desc= "This text will also be posted with the Video!";
                            Date date= new Date();
                            String dateFormat= String.valueOf(date.getTime());
                            if (snapshot.exists()) {
                                Uname = snapshot.child("username").getValue(String.class);
                                profileImageUrl = snapshot.child("userprofileimage").getValue(String.class);
                            }
                            VideoPostData videoPostData= new VideoPostData(post_desc,imgUrl,dateFormat,uid,Uname,profileImageUrl);
                            //upload data in realtime database
                            reference.push().setValue(videoPostData).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.e("TAG", "onComplete: uploadVideoWithoutText.... Successfully Video and text Uploaded to Database!");
                                }
                            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+ e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).addOnProgressListener(snapshot -> {}).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
