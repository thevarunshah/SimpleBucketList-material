package com.thevarunshah.simplebucketlist.internal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketItemListAdapter extends RecyclerView.Adapter<BucketItemListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final static String TAG = "BucketItemListAdapter"; //for debugging purposes

    private ArrayList<Item> bucketList; //the list the adapter manages
    private final Context context; //context attached to adapter
    private boolean tablet;
    private final OnStartDragListener dragStartListener;

    private boolean isLongClick = false;

    public BucketItemListAdapter(Context context, ArrayList<Item> bucketList, boolean tablet, OnStartDragListener dragStartListener) {
        this.context = context;
        this.bucketList = bucketList;
        this.tablet = tablet;
        this.dragStartListener = dragStartListener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false), tablet);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder holder, final int position) {

        if(!tablet) {
            //attach an on-tap listener to the item for checking/unchecking
            holder.item.setOnClickListener(view -> {
                Item item = bucketList.get(position);
                CheckBox cb = holder.itemView.findViewById(R.id.row_check);
                cb.setChecked(!item.isDone());
            });

            //attach a long-tap listener to the item
            holder.item.setOnLongClickListener(view -> {

                isLongClick = true;

                final Item item = bucketList.get(position); //get clicked item

                //inflate layout with customized alert dialog view
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                final View dialog = layoutInflater.inflate(R.layout.context_menu_dialog, null, false);
                final AlertDialog.Builder itemOptionsDialogBuilder = new AlertDialog.Builder(context);

                //customize alert dialog and set its view
                itemOptionsDialogBuilder.setTitle(R.string.item_options);
                itemOptionsDialogBuilder.setView(dialog);

                //set up actions for dialog buttons
                itemOptionsDialogBuilder.setNegativeButton("CANCEL", null);

                //create the dialog
                final AlertDialog itemOptionsDialog = itemOptionsDialogBuilder.create();

                /*
                 *fetch buttons and attach the appropriate on-tap listeners
                 */

                //edit button on-tap listener
                Button editButton = dialog.findViewById(R.id.context_edit);
                editButton.setOnClickListener(v -> {

                    //inflate layout with customized alert dialog view
                    LayoutInflater layoutInflater1 = LayoutInflater.from(context);
                    final View dialog13 = layoutInflater1.inflate(R.layout.input_dialog, null, false);
                    final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(context);

                    //customize alert dialog and set its view
                    editItemDialogBuilder.setTitle("Edit Item");
                    editItemDialogBuilder.setIcon(R.drawable.ic_edit_black_24px);
                    editItemDialogBuilder.setView(dialog13);

                    //fetch and set up edittext
                    final EditText input = dialog13.findViewById(R.id.input_dialog_text);
                    input.setText(item.getItemText());
                    input.setFocusableInTouchMode(true);
                    input.requestFocus();

                    //set up actions for dialog buttons
                    editItemDialogBuilder.setPositiveButton("SAVE", (dialogInterface, whichButton) -> {

                        //update text of item and the view
                        item.setItemText(input.getText().toString());
                        notifyDataSetChanged();

                        Utility.writeData(context); //backup data
                        itemOptionsDialog.dismiss();
                    });
                    editItemDialogBuilder.setNegativeButton("CANCEL", (dialog12, which) -> itemOptionsDialog.dismiss());

                    //create and show the dialog
                    AlertDialog editItemDialog = editItemDialogBuilder.create();
                    editItemDialog.show();

                    if (editItemDialog.getWindow() != null) {
                        //show keyboard
                        editItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                });
                //archive button on-tap listener
                Button archiveButton = dialog.findViewById(R.id.context_archive);
                archiveButton.setOnClickListener(v -> {

                    //move item to the archive list and update the view
                    Utility.moveToArchive(position);
                    notifyDataSetChanged();
                    Utility.writeData(context); //backup data
                    itemOptionsDialog.dismiss();

                    //display success message
                    Snackbar infoBar = Snackbar.make(view, R.string.item_archived, Snackbar.LENGTH_SHORT);
                    infoBar.show();
                });
                //delete button on-tap listener
                Button deleteButton = dialog.findViewById(R.id.context_delete);
                deleteButton.setOnClickListener(v -> {

                    //remove item from adapter and update view
                    bucketList.remove(position);
                    notifyDataSetChanged();
                    Utility.writeData(context); //backup data
                    itemOptionsDialog.dismiss();

                    //display success message and give option to undo
                    Snackbar infoBar = Snackbar.make(view, R.string.item_deleted, Snackbar.LENGTH_LONG);
                    infoBar.setAction("UNDO", v1 -> {
                        //undo deleting
                        bucketList.add(position, item);
                        notifyDataSetChanged();
                        Utility.writeData(context); //backup data
                    });
                    infoBar.setActionTextColor(Color.WHITE);
                    infoBar.show();
                });

                //show the dialog
                itemOptionsDialog.show();
                return true;
            });

            holder.item.setOnTouchListener((v, event) -> {
                int action = event.getActionMasked();
                if(action == MotionEvent.ACTION_UP && isLongClick){
                    isLongClick = false;
                    return true;
                }
                if(action == MotionEvent.ACTION_DOWN){
                    isLongClick = false;
                }
                return v.onTouchEvent(event);
            });
        }

        final Item item = bucketList.get(position);

        //attach a check listener to the checkbox
        holder.done.setOnCheckedChangeListener((buttonView, isChecked) -> {

            //get item and set as done/undone
            item.setDone(isChecked);
            //apply or get rid of strikethrough effect
            holder.item.setPaintFlags(isChecked ? (holder.item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG) : (holder.item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG));

            Utility.writeData(context); //backup data
        });

        //tablet view listeners
        if(tablet) {

            //on click for the item text
            holder.item.setOnClickListener(view -> holder.done.setChecked(!item.isDone()));

            //on click for edit
            holder.edit.setOnClickListener(view -> {

                //inflate layout with customized alert dialog view
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                final View dialog = layoutInflater.inflate(R.layout.input_dialog, null, false);
                final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(context);

                //customize alert dialog and set its view
                editItemDialogBuilder.setTitle(R.string.edit_item);
                editItemDialogBuilder.setIcon(R.drawable.ic_edit_black_24px);
                editItemDialogBuilder.setView(dialog);

                //fetch and set up edittext
                final EditText input = dialog.findViewById(R.id.input_dialog_text);
                input.setText(item.getItemText());
                input.setFocusableInTouchMode(true);
                input.requestFocus();

                //set up actions for dialog buttons
                editItemDialogBuilder.setPositiveButton("SAVE", (dialogInterface, whichButton) -> {
                    //update text of item and the view
                    item.setItemText(input.getText().toString());
                    Utility.writeData(context); //backup data
                });
                editItemDialogBuilder.setNegativeButton("CANCEL", (dialog1, which) -> {
                });

                //create and show the dialog
                AlertDialog editItemDialog = editItemDialogBuilder.create();
                editItemDialog.show();

                if (editItemDialog.getWindow() != null) {
                    //show keyboard
                    editItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });

            //on click for item archive
            holder.archive.setOnClickListener(view -> {

                //move item to the archive list and update the view
                Utility.moveToArchive(position);
                notifyDataSetChanged();
                Utility.writeData(context); //backup data

                //display success message
                Snackbar infoBar = Snackbar.make(view, R.string.item_archived, Snackbar.LENGTH_SHORT);
                infoBar.show();
            });

            //on click for item delete
            holder.delete.setOnClickListener(view -> {

                //remove item from adapter and update view
                bucketList.remove(position);
                notifyDataSetChanged();
                Utility.writeData(context); //backup data

                //display success message and give option to undo
                Snackbar infoBar = Snackbar.make(view, R.string.item_deleted, Snackbar.LENGTH_LONG);
                infoBar.setAction("UNDO", v -> {
                    //undo deleting
                    bucketList.add(position, item);
                    notifyDataSetChanged();
                    Utility.writeData(context); //backup data
                });
                infoBar.setActionTextColor(Color.WHITE);
                infoBar.show();
            });
        }

        holder.handle.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                dragStartListener.onStartDrag(holder);
            }
            return false;
        });

        //get item and link references to holder
        holder.item.setText(item.getItemText());
        holder.done.setChecked(item.isDone());
        holder.done.setTag(item);
    }

    @Override
    public void onItemDismiss(int position) {
        bucketList.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Item prev = bucketList.remove(fromPosition);
        bucketList.add(toPosition, prev);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public int getItemCount() {
        return bucketList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        View itemView;
        CheckBox done;
        TextView item;
        ImageButton edit;
        ImageButton archive;
        ImageButton delete;
        ImageView handle;

        ItemViewHolder(View itemView, boolean tablet) {
            super(itemView);
            this.itemView = itemView;
            item = itemView.findViewById(R.id.row_text);
            done = itemView.findViewById(R.id.row_check);
            if(tablet){
                edit = itemView.findViewById(R.id.context_edit);
                archive = itemView.findViewById(R.id.context_archive);
                delete = itemView.findViewById(R.id.context_delete);
            }
            handle = itemView.findViewById(R.id.handle);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}
