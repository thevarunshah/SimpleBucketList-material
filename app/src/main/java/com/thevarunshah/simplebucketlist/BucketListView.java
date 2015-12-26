package com.thevarunshah.simplebucketlist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import com.thevarunshah.classes.BucketAdapter;
import com.thevarunshah.classes.BucketItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class BucketListView extends AppCompatActivity implements OnClickListener, Serializable{

	private static final long serialVersionUID = 1L; //for serializing data

	private static final String TAG = "BucketListView"; //for debugging purposes

	private final ArrayList<BucketItem> bucketList = new ArrayList<BucketItem>(); //list of goals

	private ListView listView = null; //main view of goals
	private BucketAdapter listAdapter = null; //adapter for goals display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucket_list_view);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(myToolbar);
        
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

		LayoutInflater layoutInflater = LayoutInflater.from(BucketListView.this);
		final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
		final AlertDialog.Builder alertDialog = new AlertDialog.Builder(BucketListView.this, R.style.AppCompatAlertDialogStyle);
		alertDialog.setTitle("New Item");
		alertDialog.setIcon(R.drawable.ic_launcher);

		alertDialog.setView(dialog);

		final EditText input = (EditText) dialog.findViewById(R.id.input_dialog_text);
		input.setFocusableInTouchMode(true);
		input.requestFocus();
		input.setHint("Enter Details");

		alertDialog.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int whichButton) {

				Log.i(TAG, "adding new goal");

				String itemText = input.getText().toString();
				BucketItem goal = new BucketItem(itemText); //create new goal

				//add goal to main list and update view
				bucketList.add(goal);
				listAdapter.notifyDataSetChanged();
			}
		});
		alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int whichButton) {

				input.clearFocus();
			}
		});

		AlertDialog alert = alertDialog.create();
		alert.show();

		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
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
