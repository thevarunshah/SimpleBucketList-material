package com.thevarunshah.classes;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class Backend {

    private static final ArrayList<Item> bucketList = new ArrayList<Item>(); //list of all items
    private static final ArrayList<Item> archiveList = new ArrayList<Item>(); //list of archived items

    private static final String TAG = "Backend"; //for debugging purposes

    /**
     * transfers all completed items in the bucket list to the archive list
     */
    public static void transferCompletedToArchive(){

        //add items to the archive list and store their indices for deletion
        ArrayList<Integer> removeIndices = new ArrayList<Integer>();
        for(int i = 0; i < bucketList.size(); i++){

            Item bi = bucketList.get(i);
            if(bi.isDone()){
                archiveList.add(bi);
                removeIndices.add(i);
            }
        }

        //remove items from the bucket list
        int numRemoved = 0; //index offset
        for(Integer i : removeIndices){

            bucketList.remove(i-numRemoved);
            numRemoved++;
        }
    }

    /**
     * moves a single item at index position from the bucket list to the archive list
     *
     * @param position the index of the item to be moved
     */
    public static void moveToArchive(int position){

        Item bi = bucketList.get(position);
        archiveList.add(bi);
        bucketList.remove(position);
    }

    /**
     * creates a new file in internal memory and writes to it
     *
     * @param context the application context
     */
    public static void writeData(Context context){

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            //open file and and write the bucket and archive lists to it
            fos = context.openFileOutput("bucket_list.ser", Context.MODE_PRIVATE);
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
                Log.i(TAG, "could not close the file");
                e.printStackTrace();
            }
        }
    }

    /**
     * reads from serialized file in internal memory
     *
     * @param context the application context
     */
    public static void readData(Context context){

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            //open file and read the bucket and archive lists from it
            fis = context.openFileInput("bucket_list.ser");
            ois = new ObjectInputStream(fis);

            ArrayList<Item> bucketItems = (ArrayList<Item>) ois.readObject();
            ArrayList<Item> archiveItems = (ArrayList<Item>) ois.readObject();
            if(bucketItems != null){
                bucketList.clear();
                bucketList.addAll(bucketItems);
            }
            if(archiveItems != null){
                archiveList.clear();
                archiveList.addAll(archiveItems);
            }
        } catch (Exception e) {
            Log.i(TAG, "could not read from file");
            e.printStackTrace();
        } finally{
            try{
                if(ois != null) ois.close();
                if(fis != null) fis.close();
            } catch(Exception e){
                Log.i(TAG, "could not close the file");
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<Item> getBucketList() {
        return bucketList;
    }

    public static ArrayList<Item> getArchiveList() {
        return archiveList;
    }
}
