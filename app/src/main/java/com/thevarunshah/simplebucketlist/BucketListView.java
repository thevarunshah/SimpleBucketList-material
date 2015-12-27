package com.thevarunshah.simplebucketlist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;

import com.thevarunshah.classes.BucketAdapter;
import com.thevarunshah.classes.BucketItem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;


public class BucketListView extends AppCompatActivity implements OnClickListener, Serializable{

	private static final long serialVersionUID = 1L; //for serializing data

	private static final String TAG = "BucketListView"; //for debugging purposes

	private final ArrayList<BucketItem> bucketList = new ArrayList<BucketItem>(); //list of goals

	public static boolean completeItemsHidden = false;

	private ListView listView = null; //main view of goals
	private BucketAdapter listAdapter = null; //adapter for goals display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bucket_list_view);

		Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
		myToolbar.setContentInsetsRelative(72, 72);
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
		alertDialog.setNegativeButton("CANCEL", null);

		AlertDialog alert = alertDialog.create();
		alert.show();

		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		if(this.completeItemsHidden){
			menu.findItem(R.id.hide_completed).setTitle(R.string.show_completed_text);
		}
		else{
			menu.findItem(R.id.hide_completed).setTitle(R.string.hide_completed_text);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.hide_completed:
				if(!this.completeItemsHidden){
					item.setTitle(R.string.show_completed_text);
				}
				else{
					item.setTitle(R.string.hide_completed_text);
				}
				this.completeItemsHidden = !this.completeItemsHidden;
				return true;
			case R.id.about:
				Log.i(TAG, "Made by Varun Shah");
				Snackbar snackbar = Snackbar.make(findViewById(R.id.coordLayout), "Made by Varun Shah", Snackbar.LENGTH_SHORT);
				snackbar.show();

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause(){
		
		super.onPause();
		
		//backup data
		Log.i(TAG, "writing to file");
		writeData();
	}
	
	@Override
	protected void onResume(){
		
		super.onResume();
		
		//read data from backup
		if(bucketList.isEmpty()){
			Log.i(TAG, "reading from file");
			readData();
		}
	}

	public ArrayList<BucketItem> getUncompletedList(){

		ArrayList<BucketItem> uncompleted = new ArrayList<BucketItem>();
		for(BucketItem bi : this.bucketList){
			if(!bi.isDone()){
				uncompleted.add(bi);
			}
		}
		return uncompleted;
	}
	
	/**
	 * creates a new file in internal memory and writes to it
	 */
	private void writeData(){

		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = this.getApplicationContext().openFileOutput("bucket_list.ser", Context.MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(this.bucketList);
			oos.writeBoolean(this.completeItemsHidden);
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
	private void readData(){

		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = this.getApplicationContext().openFileInput("bucket_list.ser");
			ois = new ObjectInputStream(fis);
			ArrayList<BucketItem> list = (ArrayList<BucketItem>) ois.readObject();
			if(list != null){
				this.bucketList.clear();
				this.bucketList.addAll(list);
			}
			this.completeItemsHidden = ois.readBoolean();
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
}
