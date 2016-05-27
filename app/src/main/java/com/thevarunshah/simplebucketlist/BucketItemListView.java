package com.thevarunshah.simplebucketlist;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thevarunshah.classes.Backend;
import com.thevarunshah.classes.BucketItemAdapter;
import com.thevarunshah.classes.Item;


public class BucketItemListView extends AppCompatActivity {

	private static final String TAG = "BucketItemListView"; //for debugging purposes

	private ListView listView = null; //main view of items
	private BucketItemAdapter listAdapter = null; //adapter for items display

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.bucket_item_listview);

		//fetch toolbar and set it as the action bar
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setContentInsetsRelative(72, 72);
		setSupportActionBar(toolbar);

		//obtain list view and create new bucket list custom adapter
		listView = (ListView) findViewById(R.id.listview);
		listAdapter = new BucketItemAdapter(this, Backend.getBucketList());
		listView.setAdapter(listAdapter); //attach adapter to list view

		//obtain add button and attach a on-tap listener to it
		FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_item);
		addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
				final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
				final AlertDialog.Builder newItemDialogBuilder = new AlertDialog.Builder(BucketItemListView.this,
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				newItemDialogBuilder.setTitle("New Item");
				newItemDialogBuilder.setIcon(R.drawable.ic_launcher);
				newItemDialogBuilder.setView(dialog);

				//fetch and set up edittext
				final EditText input = (EditText) dialog.findViewById(R.id.input_dialog_text);
				input.setHint("Enter Details");
				input.setFocusableInTouchMode(true);
				input.requestFocus();

				//set up actions for dialog buttons
				newItemDialogBuilder.setPositiveButton("ADD", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialogInterface, int whichButton) {

						//create new item
						String itemText = input.getText().toString();
						Item item = new Item(itemText);

						//add item to main list and update view
						Backend.getBucketList().add(item);
						listAdapter.notifyDataSetChanged();

						Backend.writeData(getApplicationContext()); //backup data
					}
				});
				newItemDialogBuilder.setNegativeButton("CANCEL", null);

				//create and show the dialog
				AlertDialog newItemDialog = newItemDialogBuilder.create();
				newItemDialog.show();

				//show keyboard
				newItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		//fetch and set actionbar menu
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
				Backend.writeData(this.getApplicationContext()); //backup data
				//friendly success message
				Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "All completed items archived!",
						Snackbar.LENGTH_SHORT);
				infoBar.show();
				return true;
			case R.id.display_archived:
				//switch to archive view
				Intent i = new Intent(BucketItemListView.this, ArchivedItemListView.class);
				startActivity(i);
				return true;
			case R.id.about:
				displayAboutDialog();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * display app information and prompt them to rate it.
	 */
	private void displayAboutDialog(){

		//inflate layout with customized alert dialog view
		LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
		final View dialog = layoutInflater.inflate(R.layout.info_dialog, null);
		final AlertDialog.Builder infoDialogBuilder = new AlertDialog.Builder(BucketItemListView.this,
				R.style.AppCompatAlertDialogStyle);

		//customize alert dialog and set its view
		infoDialogBuilder.setTitle("About");
		infoDialogBuilder.setIcon(R.drawable.ic_info_black_24dp);
		infoDialogBuilder.setView(dialog);

		//fetch textview and set its text
		final TextView message = (TextView) dialog.findViewById(R.id.info_dialog);
		message.setText(R.string.about_message);

		//set up actions for dialog buttons
		infoDialogBuilder.setPositiveButton("RATE APP", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int whichButton) {

				String appPackageName = getApplicationContext().getPackageName();
				Intent i = new Intent(Intent.ACTION_VIEW);
				try{
					i.setData(Uri.parse("market://details?id=" + appPackageName));
					startActivity(i);
				} catch(ActivityNotFoundException e){
					try{
						i.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName));
						startActivity(i);
					} catch (ActivityNotFoundException e2){
						Snackbar errorBar = Snackbar.make(findViewById(R.id.coordLayout),
								"Could not launch the Google Play app.", Snackbar.LENGTH_SHORT);
						errorBar.show();
					}
				}
			}
		});
		infoDialogBuilder.setNegativeButton("DISMISS", null);

		//create and show the dialog
		AlertDialog infoDialog = infoDialogBuilder.create();
		infoDialog.show();
	}

	@Override
	protected void onResume(){

		super.onResume();
		if(Backend.getBucketList().isEmpty()){
			Backend.readData(this.getApplicationContext()); //read data from backup
		}
		else{
			this.listAdapter.notifyDataSetChanged(); //refresh
		}
	}
}
