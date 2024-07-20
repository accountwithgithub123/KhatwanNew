package com.example.khatwan;

public class VideoPostData {
    private String postDesc, compressedUrl,dateFormat, uid, uname,profileImageUrl;

    public VideoPostData(String postDesc, String compressedUrl, String dateFormat, String uid, String uname, String profileImageUrl) {
        this.postDesc = postDesc;
        this.compressedUrl = compressedUrl;
        this.dateFormat = dateFormat;
        this.uid = uid;
        this.uname = uname;
        this.profileImageUrl = profileImageUrl;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public String getCompressedUrl() {
        return compressedUrl;
    }

    public void setCompressedUrl(String compressedUrl) {
        this.compressedUrl = compressedUrl;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
