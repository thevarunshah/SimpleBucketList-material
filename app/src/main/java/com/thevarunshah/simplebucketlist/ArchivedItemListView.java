package com.thevarunshah.simplebucketlist;

import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.thevarunshah.simplebucketlist.internal.ArchivedItemAdapter;
import com.thevarunshah.simplebucketlist.internal.Utility;

public class ArchivedItemListView extends AppCompatActivity {

    private static final String TAG = "ArchivedItemListView"; //for debugging purposes

    private ListView listView = null; //main view of items
    private ArchivedItemAdapter listAdapter = null; //adapter for items display
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.archived_item_listview);

        //fetch toolbar and set it as the action bar
        Toolbar toolbar = findViewById(R.id.archived_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //back button
        }

        //obtain list view and create new archive list custom adapter
        listView = findViewById(R.id.archived_listview);
        listAdapter = new ArchivedItemAdapter(this, Utility.getArchiveList());
        listView.setAdapter(listAdapter); //attach adapter to list view

        emptyStateTextView = findViewById(R.id.list_empty_textview);
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
            case R.id.unarchive_archived:
                //inflate layout with customized alert dialog view
                LayoutInflater unarchiveLayoutInflater = LayoutInflater.from(ArchivedItemListView.this);
                final View unarchiveDialog = unarchiveLayoutInflater.inflate(R.layout.info_dialog, null, false);
                final AlertDialog.Builder unarchiveItemDialogBuilder = new AlertDialog.Builder(ArchivedItemListView.this);

                //customize alert dialog and set its view
                unarchiveItemDialogBuilder.setTitle("Unarchive All");
                unarchiveItemDialogBuilder.setIcon(R.drawable.ic_unarchive_black_24px);
                unarchiveItemDialogBuilder.setView(unarchiveDialog);

                //fetch textview and set its text
                final TextView unarchiveMessage = unarchiveDialog.findViewById(R.id.info_dialog);
                unarchiveMessage.setText(R.string.unarchive_confirm_text);

                unarchiveItemDialogBuilder.setPositiveButton("UNARCHIVE", (dialogInterface, whichButton) -> {
                    //move all archived items from archive list to bucket list and update view
                    Utility.getBucketList().addAll(Utility.getArchiveList());
                    Utility.getArchiveList().clear();
                    listAdapter.notifyDataSetChanged();

                    Utility.writeData(getApplicationContext()); //backup data
                });
                unarchiveItemDialogBuilder.setNegativeButton("CANCEL", null);

                //create and show the dialog
                AlertDialog unarchiveItemDialog = unarchiveItemDialogBuilder.create();
                unarchiveItemDialog.show();

                return true;
            case R.id.delete_archived:
                //inflate layout with customized alert dialog view
                LayoutInflater deleteLayoutInflater = LayoutInflater.from(ArchivedItemListView.this);
                final View deleteDialog = deleteLayoutInflater.inflate(R.layout.info_dialog, null, false);
                final AlertDialog.Builder deleteItemDialogBuilder = new AlertDialog.Builder(ArchivedItemListView.this);

                //customize alert dialog and set its view
                deleteItemDialogBuilder.setTitle("Delete All");
                deleteItemDialogBuilder.setIcon(R.drawable.ic_warning_black_24px);
                deleteItemDialogBuilder.setView(deleteDialog);

                //fetch textview and set its text
                final TextView deleteMessage = deleteDialog.findViewById(R.id.info_dialog);
                deleteMessage.setText(R.string.delete_confirm_text);

                deleteItemDialogBuilder.setPositiveButton("DELETE", (dialogInterface, whichButton) -> {
                    //remove all archived items from archive list and update view
                    Utility.getArchiveList().clear();
                    listAdapter.notifyDataSetChanged();

                    Utility.writeData(getApplicationContext()); //backup data
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
        if (Utility.getArchiveList().isEmpty()) {
            Utility.readData(this.getApplicationContext()); //read data from backup
        }

        listView.setVisibility(Utility.getArchiveList().isEmpty() ? View.GONE : View.VISIBLE);
        emptyStateTextView.setVisibility(Utility.getArchiveList().isEmpty() ? View.VISIBLE : View.GONE);
    }
}
