package com.thevarunshah.simplebucketlist;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.internal.BucketListWidgetProvider;
import com.thevarunshah.simplebucketlist.internal.Utility;
import com.thevarunshah.simplebucketlist.internal.BucketItemAdapter;

import java.util.ArrayList;


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
		listAdapter = new BucketItemAdapter(this, Utility.getBucketList());
		listView.setAdapter(listAdapter); //attach adapter to list view

		attachListenersToListView();

		//obtain add button and attach a on-tap listener to it
		final FloatingActionButton addButton = (FloatingActionButton) findViewById(R.id.add_item);
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
						Utility.getBucketList().add(item);
						listAdapter.notifyDataSetChanged();

						Utility.writeData(getApplicationContext()); //backup data

						//update widget
						Intent widgetIntent = new Intent(getApplicationContext(), BucketListWidgetProvider.class);
						widgetIntent.setAction(BucketListWidgetProvider.UPDATE_ACTION);
						getApplicationContext().sendBroadcast(widgetIntent);
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

		//moving fab out of the way when scrolling listview
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {

			int lastPosition = -1;

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem, int i1, int i2) {

				if(lastPosition == firstVisibleItem){
					return;
				}

				if(firstVisibleItem > lastPosition){
					addButton.animate().translationY(addButton.getHeight()*2); //scrolling down
				}
				else{
					addButton.animate().translationY(0); //scrolling up
				}

				lastPosition = firstVisibleItem;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}
		});
	}

	private void attachListenersToListView(){

		//attach an on-tap listener to the item for checking/unchecking
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				Item item = listAdapter.getItem(i);
				CheckBox cb = (CheckBox) view.findViewById(R.id.row_check);
				cb.setChecked(!item.isDone());
			}
		});

		//attach a long-tap listener to the item
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int i, long l) {

				final Item item = listAdapter.getItem(i); //get clicked item

				//inflate layout with customized alert dialog view
				LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
				final View dialog = layoutInflater.inflate(R.layout.context_menu_dialog, null);
				final AlertDialog.Builder itemOptionsDialogBuilder = new AlertDialog.Builder(BucketItemListView.this,
						R.style.AppCompatAlertDialogStyle);

				//customize alert dialog and set its view
				itemOptionsDialogBuilder.setTitle("Item Options");
				itemOptionsDialogBuilder.setView(dialog);

				//set up actions for dialog buttons
				itemOptionsDialogBuilder.setNegativeButton("CANCEL", null);

				//create the dialog
				final AlertDialog itemOptionsDialog = itemOptionsDialogBuilder.create();

				/*
				 *fetch buttons and attach the appropriate on-tap listeners
				 */

				//edit button on-tap listener
				Button editButton = (Button) dialog.findViewById(R.id.context_edit);
				editButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						//inflate layout with customized alert dialog view
						LayoutInflater layoutInflater = LayoutInflater.from(BucketItemListView.this);
						final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
						final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(BucketItemListView.this,
								R.style.AppCompatAlertDialogStyle);

						//customize alert dialog and set its view
						editItemDialogBuilder.setTitle("Edit Item");
						editItemDialogBuilder.setIcon(R.drawable.ic_edit_black_24dp);
						editItemDialogBuilder.setView(dialog);

						//fetch and set up edittext
						final EditText input = (EditText) dialog.findViewById(R.id.input_dialog_text);
						input.setText(item.getItemText());
						input.setFocusableInTouchMode(true);
						input.requestFocus();

						//set up actions for dialog buttons
						editItemDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int whichButton) {

								//update text of item and the view
								item.setItemText(input.getText().toString());
								listAdapter.notifyDataSetChanged();

								Utility.writeData(BucketItemListView.this); //backup data
								itemOptionsDialog.dismiss();
							}
						});
						editItemDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								itemOptionsDialog.dismiss();
							}
						});

						//create and show the dialog
						AlertDialog editItemDialog = editItemDialogBuilder.create();
						editItemDialog.show();

						//show keyboard
						editItemDialog.getWindow()
								.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
					}
				});
				//archive button on-tap listener
				Button archiveButton = (Button) dialog.findViewById(R.id.context_archive);
				archiveButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						//move item to the archive list and update the view
						Utility.moveToArchive(i);
						listAdapter.notifyDataSetChanged();
						Utility.writeData(BucketItemListView.this); //backup data
						itemOptionsDialog.dismiss();

						//display success message
						Snackbar infoBar = Snackbar.make(view, "Item archived.", Snackbar.LENGTH_SHORT);
						infoBar.show();
					}
				});
				//delete button on-tap listener
				Button deleteButton = (Button) dialog.findViewById(R.id.context_delete);
				deleteButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {

						//remove item from adapter and update view
						listAdapter.getBucketList().remove(i);
						listAdapter.notifyDataSetChanged();
						Utility.writeData(BucketItemListView.this); //backup data
						itemOptionsDialog.dismiss();

						//display success message and give option to undo
						Snackbar infoBar = Snackbar.make(view, "Item deleted.", Snackbar.LENGTH_LONG);
						infoBar.setAction("UNDO", new OnClickListener() {
							@Override
							public void onClick(View v) {

								//undo deleting
								listAdapter.getBucketList().add(i, item);
								listAdapter.notifyDataSetChanged();
								Utility.writeData(BucketItemListView.this); //backup data
							}
						});
						infoBar.setActionTextColor(Color.WHITE);
						infoBar.show();
					}
				});

				//show the dialog
				itemOptionsDialog.show();

				return true;
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
				final ArrayList<Integer> removedIndices = Utility.transferCompletedToArchive();
				if(removedIndices.size() == 0){
					Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "No items to archive.",
							Snackbar.LENGTH_SHORT);
					infoBar.show();
					return true;
				}
				listAdapter.notifyDataSetChanged();
				Utility.writeData(this.getApplicationContext()); //backup data

				//friendly success message and give option to undo
				Snackbar infoBar = Snackbar.make(findViewById(R.id.coordLayout), "All completed items archived.",
						Snackbar.LENGTH_LONG);
				infoBar.setAction("UNDO", new OnClickListener() {
					@Override
					public void onClick(View v) {

						//undo deleting
						Utility.undoTransferToArchive(removedIndices);
						listAdapter.notifyDataSetChanged();
						Utility.writeData(getApplicationContext()); //backup data
					}
				});
				infoBar.setActionTextColor(Color.WHITE);
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
		if(Utility.getBucketList().isEmpty()){
			Utility.readData(this.getApplicationContext()); //read data from backup
		}
		else{
			this.listAdapter.notifyDataSetChanged(); //refresh
		}
	}
}
