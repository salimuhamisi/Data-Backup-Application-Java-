package com.example.autobackup;


public class Upload {
    private String Name;
    private String FileUrl;

    public Upload(){
        //Empty class
    }
    public Upload(String name, String fileUrl){
        if (name.trim().equals("")){
            name="No Name";
        }
        Name=name;
        FileUrl=fileUrl;
    }
    public String getName(){

        return Name;
    }

    public void setName(String name) {

        Name = name;
    }

    public String getFileUrl() {

        return FileUrl;
    }

    public void setFileUrl(String fileUrl) {

        FileUrl = fileUrl;
    }
}
