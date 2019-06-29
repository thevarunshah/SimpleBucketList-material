package com.thevarunshah.simplebucketlist.internal;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.thevarunshah.classes.Item;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class Utility {

    private static BackupManager backupManager;

    private static final ArrayList<Item> bucketList = new ArrayList<>(); //list of all items
    private static final ArrayList<Item> archiveList = new ArrayList<>(); //list of archived items

    private static final String PREFERENCE_FILE_KEY = "BucketListPreferences";
    private static final String ADD_TO_TOP_PREFERENCE_KEY = "AddToTop";

    private static final String TAG = "Utility"; //for debugging purposes

    /**
     * transfers all completed items in the bucket list to the archive list
     *
     * @return indices of the items moved to archive
     */
    public static ArrayList<Integer> transferCompletedToArchive(){

        //add items to the archive list and store their indices for deletion
        ArrayList<Integer> removeIndices = new ArrayList<>();
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

        return removeIndices;
    }

    /**
     * undoes the transferring of completed items to the archive list
     */
    public static void undoTransferToArchive(ArrayList<Integer> removedIndices){

        //for each item in the archive list, move it back to it's original position
        int numMoved = 0;
        for(int i = archiveList.size()-removedIndices.size(); i < archiveList.size()+numMoved; i++){
            bucketList.add(removedIndices.get(numMoved), archiveList.get(i-numMoved));
            archiveList.remove(i-numMoved);
            numMoved++;

            if(numMoved == removedIndices.size()){
                break;
            }
        }
    }

    /**
     * moves a single item at index position from the bucket list to the archive list
     *
     * @param position the index of the item to be moved
     */
    public static void moveToArchive(int position){
        archiveList.add(bucketList.get(position));
        bucketList.remove(position);
    }

    /**
     * fetches the user's preference of whether to add items to top or bottom.
     *
     * @param context the application context
     * @return the user's preference to add items to top or bottom; false by default
     */
    public static boolean getAddToTopPreference(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(ADD_TO_TOP_PREFERENCE_KEY, false);
    }

    /**
     * updates the user's preference of whether to add items to top or bottom.
     *
     * @param context the application context
     * @param addToTop true if user wants to add items to the top; false otherwise
     */
    public static void updateAddToTopPreference(Context context, boolean addToTop) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ADD_TO_TOP_PREFERENCE_KEY, addToTop);
        editor.apply();
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
            Log.e(TAG, "could not write to file");
            e.printStackTrace();
        } finally{
            try{
                if (oos != null) oos.close();
                if (fos != null) fos.close();
            } catch (Exception e){
                Log.e(TAG, "could not close the file");
                e.printStackTrace();
            }
        }

        backupManager = new BackupManager(context);
        backupManager.dataChanged();

        updateWidget(context);
    }

    private static void updateWidget(Context context){
        Intent widgetIntent = new Intent(context, BucketListWidgetProvider.class);
        widgetIntent.setAction(BucketListWidgetProvider.UPDATE_ACTION);
        context.sendBroadcast(widgetIntent);
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

            updateWidget(context);
        } catch (Exception e) {
            Log.e(TAG, "could not read from file");
            e.printStackTrace();
        } finally{
            try{
                if (ois != null) ois.close();
                if (fis != null) fis.close();
            } catch(Exception e){
                Log.e(TAG, "could not close the file");
                e.printStackTrace();
            }
        }
    }

    public static void restoreData(Context context, RestoreObserver restoreObserver) {
        backupManager = new BackupManager(context);
        backupManager.requestRestore(restoreObserver);
    }

    public static ArrayList<Item> getBucketList() {
        return bucketList;
    }

    public static ArrayList<Item> getArchiveList() {
        return archiveList;
    }
}
