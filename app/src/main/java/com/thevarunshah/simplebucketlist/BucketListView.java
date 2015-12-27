package com.thevarunshah.simplebucketlist;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.thevarunshah.classes.Backend;
import com.thevarunshah.classes.BucketAdapter;
import com.thevarunshah.classes.BucketItem;


public class BucketListView extends AppCompatActivity implements OnClickListener{

	private static final String TAG = "BucketListView"; //for debugging purposes

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
        listAdapter = new BucketAdapter(this, R.layout.row, Backend.getBucketList());
        
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
				Backend.getBucketList().add(goal);
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
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.archive_completed:
				//transfer completed items to archive
				Backend.transferCompletedToArchive();
				listAdapter.notifyDataSetChanged();
				Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "All completed items archived!", Snackbar.LENGTH_SHORT);
				infoBar.show();
				return true;
			case R.id.display_archived:
				Intent i = new Intent(BucketListView.this, ArchiveListView.class);
				startActivity(i);
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
		Backend.writeData(this.getApplicationContext());
	}
	
	@Override
	protected void onResume(){
		
		super.onResume();
		
		//read data from backup
		if(Backend.getBucketList().isEmpty()){
			Log.i(TAG, "reading from file");
			Backend.readData(this.getApplicationContext());
		}
	}
}
