package com.thevarunshah.classes;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Backend implements Serializable {

    private static final long serialVersionUID = 1L; //for serializing data

    private static final ArrayList<BucketItem> bucketList = new ArrayList<BucketItem>(); //list of all items
    private static final ArrayList<BucketItem> archiveList = new ArrayList<BucketItem>(); //list of archived items

    private static final String TAG = "Backend"; //for debugging purposes

    public static void transferCompletedToArchive(){

        ArrayList<Integer> removeIndices = new ArrayList<Integer>();
        for(int i = 0; i < bucketList.size(); i++){
            BucketItem bi = bucketList.get(i);
            if(bi.isDone()){
                archiveList.add(bi);
                removeIndices.add(i);
            }
        }

        int numRemoved = 0; //index offset since items are being removed while iterating
        for(Integer i : removeIndices){
            bucketList.remove(i-numRemoved);
            numRemoved++;
        }
    }

    /**
     * creates a new file in internal memory and writes to it
     */
    public static void writeData(Context c){

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = c.openFileOutput("bucket_list.ser", Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(bucketList);
            oos.writeObject(archiveList);
        } catch (Exception e) {
            Log.i(TAG, "could not write to file");
            e.printStackTrace();
        } finally{
            try{
                oos.close();
                fos.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * reads from file in internal memory
     */
    public static void readData(Context c){

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = c.openFileInput("bucket_list.ser");
            ois = new ObjectInputStream(fis);
            ArrayList<BucketItem> bList = (ArrayList<BucketItem>) ois.readObject();
            if(bList != null){
                bucketList.clear();
                bucketList.addAll(bList);
            }
            ArrayList<BucketItem> aList = (ArrayList<BucketItem>) ois.readObject();
            if(aList != null){
                archiveList.clear();
                archiveList.addAll(aList);
            }
        } catch (Exception e) {
            Log.i(TAG, "could not read from file");
            e.printStackTrace();
        } finally{
            try{
                if(ois != null) ois.close();
                if(fis != null) fis.close();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<BucketItem> getBucketList() {
        return bucketList;
    }

    public static ArrayList<BucketItem> getArchiveList() {
        return archiveList;
    }
}
