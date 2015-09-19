package com.thevarunshah.simplebucketlist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.thevarunshah.classes.BucketAdapter;
import com.thevarunshah.classes.BucketItem;


public class BucketListView extends Activity implements OnClickListener, Serializable{
	
	private static final long serialVersionUID = 1L; //for serializing data

	private static final String TAG = "BucketListView"; //for debugging purposes
	
	private final ArrayList<BucketItem> bucketList = new ArrayList<BucketItem>(); //list of goals
	
	private ListView listView = null; //main view of goals
	private BucketAdapter listAdapter = null; //adapter for goals display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucket_list_view);
        
        //obtain list view and create new bucket list custom adapter
        listView = (ListView) findViewById(R.id.listview);
        listAdapter = new BucketAdapter(this, R.layout.row, bucketList);
        
        //attach adapter to list view
        listView.setAdapter(listAdapter);
        
        //obtain add button and attach press listener to it
        FloatingActionButton addItem = (FloatingActionButton) findViewById(R.id.add_item);
        addItem.setOnClickListener(this);
    }
    
    @Override
	public void onClick(View v) {

    	Log.i(TAG, "pressed add button");
    	
    	//start add item activity and wait for result (in onActivityResult(...))
    	Intent i = new Intent(BucketListView.this, AddItem.class);
    	startActivityForResult(i, 0);
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode == RESULT_OK && requestCode == 0){

			Log.i(TAG, "adding new goal");
			
			BucketItem goal = new BucketItem(data.getStringExtra("text")); //create new goal
			
			//add goal to main list and update view
			bucketList.add(goal);
			listAdapter.notifyDataSetChanged();
		}
	}
	
	@Override
	protected void onPause(){
		
		super.onPause();
		
		//backup data
		try {
			Log.i(TAG, "writing to file");
			writeData(this.bucketList);
		} catch (IOException e) {
			Log.i(TAG, "could not write to file");
			Log.i(TAG, "Exception: " + e);
		}
	}
	
	@Override
	protected void onResume(){
		
		super.onResume();
		
		//read data from backup
		try {
			if(bucketList.isEmpty()){
				Log.i(TAG, "reading from file");
				ArrayList<BucketItem> backup = readData();
				this.bucketList.clear();
				this.bucketList.addAll(backup);
			}
		} catch (Exception e) {
			Log.i(TAG, "could not read from file");
			Log.i(TAG, "Exception: " + e);
		}
	}
	
	/**
	 * creates a new file in SD card and writes to it
	 * 
	 * @param bucketList object which is written to file
	 * @throws IOException
	 */
	private static void writeData(ArrayList<BucketItem> bucketList) throws IOException {
		
		//obtain file and create if not there
		File file = new File(android.os.Environment.getExternalStorageDirectory() + "/bucket_list.ser");
		file.createNewFile();
		
		//write to file
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
		oos.writeObject(bucketList);
		oos.close();
	}
	
	/**
	 * reads from file in SD card
	 * 
	 * @return object which holds the backup data
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private static ArrayList<BucketItem> readData() throws IOException, ClassNotFoundException {
		
		//obtain file
		File file = new File(android.os.Environment.getExternalStorageDirectory() + "/bucket_list.ser");
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
		
		@SuppressWarnings("unchecked")
		//read list from file
		ArrayList<BucketItem> list = (ArrayList<BucketItem>) ois.readObject();
		ois.close();
		return list;
	}
}
