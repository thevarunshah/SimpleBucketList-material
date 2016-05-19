package com.thevarunshah.simplebucketlist;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.thevarunshah.classes.ArchivedItemAdapter;
import com.thevarunshah.classes.Backend;

public class ArchivedItemListView extends AppCompatActivity {

    private static final String TAG = "ArchivedItemListView"; //for debugging purposes

    private ListView listView = null; //main view of items
    private ArchivedItemAdapter listAdapter = null; //adapter for items display

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.archived_item_listview);

        //fetch toolbar and set it as the action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.archived_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back button

        //obtain list view and create new archive list custom adapter
        listView = (ListView) findViewById(R.id.archived_listview);
        listAdapter = new ArchivedItemAdapter(this, Backend.getArchiveList());
        listView.setAdapter(listAdapter); //attach adapter to list view
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //fetch and set actionbar menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.archive_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_archived:
                //inflate layout with customized alert dialog view
                LayoutInflater layoutInflater = LayoutInflater.from(ArchivedItemListView.this);
                final View dialog = layoutInflater.inflate(R.layout.info_dialog, null);
                final AlertDialog.Builder deleteItemDialogBuilder = new AlertDialog.Builder(ArchivedItemListView.this,
                    R.style.AppCompatAlertDialogStyle);

                //customize alert dialog and set its view
                deleteItemDialogBuilder.setTitle("Delete All");
                deleteItemDialogBuilder.setIcon(R.drawable.ic_warning_black_24dp);
                deleteItemDialogBuilder.setView(dialog);

                //fetch textview and set its text
                final TextView message = (TextView) dialog.findViewById(R.id.info_dialog);
                message.setText("Are you sure you want to permanently delete all archived items?");

                deleteItemDialogBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int whichButton) {

                        //remove all archived items from archive list and update view
                        Backend.getArchiveList().clear();
                        listAdapter.notifyDataSetChanged();

                        Backend.writeData(getApplicationContext()); //backup data
                    }
                });
                deleteItemDialogBuilder.setNegativeButton("CANCEL", null);

                //create and show the dialog
                AlertDialog deleteItemDialog = deleteItemDialogBuilder.create();
                deleteItemDialog.show();

                return true;
            case android.R.id.home:
                //back button tapped, finish activity
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){

        super.onResume();
        if(Backend.getArchiveList().isEmpty()){
            Backend.readData(this.getApplicationContext()); //read data from backup
        }
    }
}
