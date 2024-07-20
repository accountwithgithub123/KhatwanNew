package com.example.khatwan;

import static android.app.Activity.RESULT_OK;
import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
//import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class UploadVideo extends AppCompatActivity {
    private static final String TAG = "HomeFragment";
    Uri videoUri;
    MaterialButton upload_post_btn;
    MaterialButton btnPickVideo;
    EditText video_description;
    ImageView post_video_img;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressBar progressBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        
        reference= FirebaseDatabase.getInstance().getReference().child("VideosPosts");
        storageReference= FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();





        //ids initialization
        btnPickVideo=findViewById(R.id.post_video_layout);
        upload_post_btn=findViewById(R.id.upload_video_post_btn);
        video_description=findViewById(R.id.post_video_description);
        post_video_img= findViewById(R.id.post_video_img);
        progressBar=findViewById(R.id.progress_bar);

        //pick images from Gallery
        btnPickVideo.setOnClickListener(view12 -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(Intent.createChooser(intent,"Pick Up Video"),2);
        });

        // to upload post
        upload_post_btn.setOnClickListener(view1 -> {
            if (videoUri!=null) {
                Log.e(TAG, "Uploading Post Btn Clicked : Video URI : " + videoUri );

                String outputDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CompressedVideos/";
                File outputDir = new File(outputDirPath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }

                //new VideoCompression(UploadVideo.this, videoUri.toString(), outputDirPath).execute();
                //new VideoCompression(UploadVideo.this).execute(videoUri.toString(),null,file.getPath());
            }
            else {
                uploadPostWithoutVideo();
            }
        });

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2 && resultCode==RESULT_OK && data!=null)
        {
            videoUri= data.getData();
            Glide.with(UploadVideo.this).load(videoUri).into(post_video_img);
        }
    }

    private void uploadVideoWithoutText(Uri viduri)
    {
        progressBar.setVisibility(View.VISIBLE);
        StorageReference fileRef= storageReference.child("Videos/"+System.currentTimeMillis()+".mp4");
        fileRef.putFile(viduri).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String uid=user.getUid();
                    //to get UserName from Database
                    DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);
                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String Uname="";
                            String profileImageUrl="";
                            String imgUrl= uri.toString();
                            String post_desc= video_description.getText().toString();
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
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("TAG", "onComplete: uploadVideoWithoutText.... Successfully Video and text Uploaded to Database!");

                                }
                            }).addOnFailureListener(e -> Toast.makeText(UploadVideo.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }).addOnFailureListener(e -> Toast.makeText(UploadVideo.this, "Error "+ e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).addOnProgressListener(snapshot -> progressBar.setVisibility(View.VISIBLE)).addOnFailureListener(e -> Toast.makeText(UploadVideo.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void uploadPostWithoutVideo()
    {
        String uid=user.getUid();
        //to get UserName from Database
        DatabaseReference userRef= FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String Uname="";
                String profileImageUrl="";
                String post_desc= video_description.getText().toString();
                Date date= new Date();
                String dateFormat= String.valueOf(date.getTime());
                if (snapshot.exists()) {
                    Uname = snapshot.child("username").getValue(String.class);
                }
                VideoPostData videoPostData= new VideoPostData(post_desc,"",uid,dateFormat,Uname,profileImageUrl);
                //upload data in realtime database
                reference.push().setValue(videoPostData).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Log.e("TAG", "onComplete: Posted No Video Only Text" );
                        //FragmentManager manager= getActivity().getSupportFragmentManager();
                        //FragmentTransaction transaction=manager.beginTransaction();
                        //transaction.replace(R.id.framelayout,new DisplayPostFragment());
                        //transaction.commit();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "Error In UploadPostWithoutVideo : Error : " + e.getMessage() );
                        Toast.makeText(UploadVideo.this, "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Video.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = 0;
            if (cursor != null) {
                column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Failed to get real path from URI", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return "NULL";
    }



    public class VideoCompression extends AsyncTask<Void ,Void ,String> {
        Context context;
        String inputPath;
        String outputPath;
        public VideoCompression(Context context, String inputPath, String outputPath) {
            this.context = context;
            this.inputPath = inputPath;
            this.outputPath = outputPath;
        }
        @Override
        protected String doInBackground(Void... voids) {
            Log.d("VideoCompression", "Input path: " + inputPath);
            Log.d("VideoCompression", "Output path: " + outputPath);
            String filePath = null;
            // inputPath = "/storage/emulated/0/AzRecorderFree/video.mp4";
            try {
                Log.e(TAG, "doInBackground: Just Before Sili Compressor");
//                filePath = SiliCompressor.with(UploadVideo.this).compressVideo(videoUri, outputPath);
            } catch (IllegalArgumentException e) {
                Log.e("VideoCompression", "Invalid argument: " + e.getMessage());
            } catch (Exception e) {
                Log.e("VideoCompression", "Compression error: " + e.getMessage());
            }
            return filePath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s!=null && !s.isEmpty())
            {
                File f= new File(s);
                Uri uri= Uri.fromFile(f);
                uploadVideoWithoutText(uri);
            }
        }
    }
}
