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
    MaterialButton upload_post_btn;
    MaterialButton btnPickVideo;
    EditText video_description;
    ImageView post_video_img;
    StorageReference storageReference;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressBar progressBar;
    TextView tvResult;
    VideoView vidView;
    public HomeFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_home, container, false);
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
        upload_post_btn=view.findViewById(R.id.upload_video_post_btn);
        video_description=view.findViewById(R.id.post_video_description);
        post_video_img= view.findViewById(R.id.post_video_img);
        progressBar=view.findViewById(R.id.progress_bar);

        //pick images from Gallery
        btnPickVideo.setOnClickListener(view12 -> {
            Intent intent=new Intent();
            intent.setAction(Intent.ACTION_PICK);
            intent.setType("video/*");
            startActivityForResult(Intent.createChooser(intent,"Pick Up Video"),2);
        });
//Can you put video uri to some textview? To check what is the value of it??n yes./ Okay
        // to upload post
        upload_post_btn.setOnClickListener(view1 -> {
            //startActivity(new Intent(requireContext(),GeekForGeek.class));
            if (videoUri!=null) {
                Log.e(TAG, "Uploading Post Btn Clicked : Video URI : " + videoUri );
                //File file= new File(requireContext().getCacheDir().getAbsolutePath());
                //String inputFilePath = getRealPathFromURI(videoUri);
                //String inputFilePath = videoUri.toString();
                File file = new File(String.valueOf(Objects.requireNonNull(videoUri.get(0))));//create path from uri
                final String[] split = file.getPath().split(":");//split the path.
//                String inputFilePath  = split[1];
//                String inputFilePath  = videoUri.getPath();
                String inputFilePath  = "/storage/emulated/0/AzRecorderFree/video.mp4";

                String outputDirPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/CompressedVideos/";
                File outputDir = new File(outputDirPath);
                if (!outputDir.exists()) {
                    outputDir.mkdirs();
                }
               // List<VideoQuality> videoNames = null;
                List<String> videoNames = new ArrayList<>();
                videoNames.add("FirstVideo");
                Log.e(TAG, "SIZES: " + videoNames.size() + " : " + videoUri.size() );

                try {
                    VideoCompressor.start(
                            requireContext(), // context is required
                            videoUri, // List<Uri> Source can be provided as content uris        -- let's say it's one uri in the list, that is the source pf video.
                            false,
                            // THIS STORAGE
                            new SharedStorageConfiguration(
                                    SaveLocation.movies, // default is movies
                                    "khatwan-compressed-videos" // optional subFolderName
                            ),
                            // OR AND NOT BOTH
                            new AppSpecificStorageConfiguration(
                                    "khatwan-compressed-videos" // optional subFolderName
                            ),
                            new Configuration(VideoQuality.MEDIUM,true,2,false,false,360.0,480.0 ,videoNames)
                            ,new CompressionListener() {
                                @Override
                                public void onProgress(int index, float percent) {

                                }

                                @Override
                                public void onStart(int index) {
                                    // Compression start
                                }

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
                } catch (Exception e) {
                    // ut the error to your tecctview.
                    tvResult.setText(tvResult.getText() + "\n Error : " + e.getMessage());
                }
                // If you agree !!! Give me some time and i'll clean this code and then send the zip file to you .When you're are free anytime today, tomorrow or some other day try to fix this. Please!
//                 Ah.?? Let me check. I'm free now. I may not be tomorrow. Will not be the day after tomorrow. Do you agree? Okay then let me send you the clean version of this code and try to fix it on your system. As we may be wasting time in this scenario(using anydesk)........................................................................................................................................................... put to git. I'll get it fro there.
                //new VideoCompression(requireContext(), inputFilePath, outputDirPath).execute();
                //new VideoCompression(requireContext()).execute(videoUri.toString(),null,file.getPath());
            }
           /*
            else {
                uploadPostWithoutVideo();
            }

            */
        });
        return view;

    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==2 && resultCode==RESULT_OK && data!=null) {

            videoUri.add(data.getData());
            Glide.with(requireContext()).load(videoUri.get(0)).into(post_video_img);
            tvResult.setText("Uri Value : " + data.getDataString());
            vidView.setVideoURI(data.getData());
            vidView.start();
//            Done or needed something else?? Man are you here!!! yes. tak me where is the compression code
// GUISCRCPY is not good one. Yes it do the same thing, but    we require scrcy for a reason. oky? Okay from where?
            //Done!  Have you installed scrcpy? No. Install it and show me your real device and pick a file and etc etc..
            //String videoPath = getRealPathFromURI(requireContext(), videoUri);
            String videoPath = "MyNull";
                File file = new File(String.valueOf(Objects.requireNonNull(videoUri.get(0))));//create path from uri
                final String[] split = file.getPath().split(":");//split the path.
                videoPath = split[1];
                Log.e(TAG, "Correct Video inPutPath: " + videoPath);
            // post_image.setImageURI(imageUri);








//             We'll check the issue, and try the common solutions.
//             Lets check if someone else encounter same error.
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
                                    //FragmentManager manager= requireActivity().getSupportFragmentManager();
                                    //FragmentTransaction transaction=manager.beginTransaction();
                                    //transaction.replace(R.id.framelayout,new VideosFragment());
                                    //transaction.commit();
                                }
                            }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {}
                    });
                }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+ e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).addOnProgressListener(snapshot -> progressBar.setVisibility(View.VISIBLE)).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(requireContext(), "Error "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                Log.e("VideoCompression", "Input file does not exist: (Returning Null) - " + inputPath);
                return null;
            }
           // inputPath = "/storage/emulated/0/AzRecorderFree/video.mp4";
            try {
//                filePath = SiliCompressor.with(context).compressVideo(videoUri.get(0), outputPath);
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
/*


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
 */