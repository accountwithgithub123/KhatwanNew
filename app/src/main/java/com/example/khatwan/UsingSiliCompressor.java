package com.example.khatwan;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


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

public class UsingSiliCompressor extends Fragment {
    private static final String TAG = "UsingSiliCompressor";
    Uri videoUri;
    MaterialButton btnUploadPost;
    MaterialButton btnPickVideo;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;
    TextView tvResult;
    VideoView vidView;
    public UsingSiliCompressor() {}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_using_sili_compressor, container, false);
        initializer(view);
        btnPickVideo.setOnClickListener(view12 -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(Intent.createChooser(intent,"Pick Up Video"),2);
        });
        btnUploadPost.setOnClickListener(view1 -> {
            if (videoUri!=null) {
                Log.e(TAG, "Uploading Post Btn Clicked : Video URI : " + videoUri );
                String inputFilePath = videoUri.toString();
                //String inputFilePath  = "/storage/emulated/0/AzRecorderFree/video.mp4";
                String outputDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CompressedVideos/";
                File outputDir = new File(outputDirPath);
                if (!outputDir.exists()) outputDir.mkdirs();
                try {
                    new VideoCompression(requireContext(),inputFilePath,outputDirPath).execute(null,null,null);
                }
                catch (Exception e){
                    tvResult.setText(tvResult.getText() + "\n Error : " + e.getMessage());
                }
            }
        });
        return view;

    }

    private void initializer(View view) {
        reference= FirebaseDatabase.getInstance().getReference().child("VideosPosts");
        storageReference= FirebaseStorage.getInstance().getReference();
        auth=FirebaseAuth.getInstance();
        user=auth.getCurrentUser();
        videoUri = null;

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

            videoUri = (data.getData());
            tvResult.setText("Uri Value : " + data.getDataString());
            vidView.setVideoURI(data.getData());
            vidView.start();
            Log.e(TAG, "Picked Video inPutPath: " + data.getDataString());
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
                            String post_desc= "Some Text for the Post!";
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

    @SuppressLint("StaticFieldLeak")
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
            try {
                //TODO Change here...
//               filePath = SiliCompressor.with(context).compressVideo(inputPath, outputPath);
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