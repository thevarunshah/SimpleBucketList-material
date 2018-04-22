package com.thevarunshah.simplebucketlist.internal;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.BucketItemListView;
import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketItemListAdapter extends RecyclerView.Adapter<BucketItemListAdapter.ItemViewHolder> implements ItemTouchHelperAdapter {

    private final static String TAG = "BucketItemListAdapter"; //for debugging purposes

    private ArrayList<Item> bucketList; //the list the adapter manages
    private final Context context; //context attached to adapter
    private static boolean tablet;
    private final OnStartDragListener dragStartListener;

    public BucketItemListAdapter(Context context, ArrayList<Item> bucketList, boolean tablet, OnStartDragListener dragStartListener) {
        this.context = context;
        this.bucketList = bucketList;
        BucketItemListAdapter.tablet = tablet;
        this.dragStartListener = dragStartListener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row, parent, false));
    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, final int position) {

        if(!tablet) {
            //attach an on-tap listener to the item for checking/unchecking
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Item item = getItem(position);
                    CheckBox cb = view.findViewById(R.id.row_check);
                    cb.setChecked(!item.isDone());
                }
            });

            //attach a long-tap listener to the item
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View view) {

                    final Item item = getItem(position); //get clicked item

                    //inflate layout with customized alert dialog view
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    final View dialog = layoutInflater.inflate(R.layout.context_menu_dialog, null);
                    final AlertDialog.Builder itemOptionsDialogBuilder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

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
                    editButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //inflate layout with customized alert dialog view
                            LayoutInflater layoutInflater = LayoutInflater.from(context);
                            final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
                            final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

                            //customize alert dialog and set its view
                            editItemDialogBuilder.setTitle("Edit Item");
                            editItemDialogBuilder.setIcon(R.drawable.ic_edit_black_24px);
                            editItemDialogBuilder.setView(dialog);

                            //fetch and set up edittext
                            final EditText input = dialog.findViewById(R.id.input_dialog_text);
                            input.setText(item.getItemText());
                            input.setFocusableInTouchMode(true);
                            input.requestFocus();

                            //set up actions for dialog buttons
                            editItemDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int whichButton) {

                                    //update text of item and the view
                                    item.setItemText(input.getText().toString());
                                    notifyDataSetChanged();

                                    Utility.writeData(context); //backup data
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
                            editItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                        }
                    });
                    //archive button on-tap listener
                    Button archiveButton = dialog.findViewById(R.id.context_archive);
                    archiveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //move item to the archive list and update the view
                            Utility.moveToArchive(position);
                            notifyDataSetChanged();
                            Utility.writeData(context); //backup data
                            itemOptionsDialog.dismiss();

                            //display success message
                            Snackbar infoBar = Snackbar.make(view, R.string.item_archived, Snackbar.LENGTH_SHORT);
                            infoBar.show();
                        }
                    });
                    //delete button on-tap listener
                    Button deleteButton = dialog.findViewById(R.id.context_delete);
                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //remove item from adapter and update view
                            getBucketList().remove(position);
                            notifyDataSetChanged();
                            Utility.writeData(context); //backup data
                            itemOptionsDialog.dismiss();

                            //display success message and give option to undo
                            Snackbar infoBar = Snackbar.make(view, R.string.item_deleted, Snackbar.LENGTH_LONG);
                            infoBar.setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //undo deleting
                                    getBucketList().add(position, item);
                                    notifyDataSetChanged();
                                    Utility.writeData(context); //backup data
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

        final Item item = bucketList.get(position);

        //attach a check listener to the checkbox
        holder.done.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                //get item and set as done/undone
                item.setDone(isChecked);
                //apply or get rid of strikethrough effect
                holder.item.setPaintFlags(isChecked ? (holder.item.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG) : (holder.item.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG));

                Utility.writeData(context); //backup data
            }
        });

        //tablet view listeners
        if(tablet) {

            //on click for the item text
            holder.item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.done.setChecked(!item.isDone());
                }
            });

            //on click for edit
            holder.edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //inflate layout with customized alert dialog view
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    final View dialog = layoutInflater.inflate(R.layout.input_dialog, null);
                    final AlertDialog.Builder editItemDialogBuilder = new AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle);

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
                    editItemDialogBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int whichButton) {
                            //update text of item and the view
                            item.setItemText(input.getText().toString());
                            Utility.writeData(context); //backup data
                        }
                    });
                    editItemDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) { }
                    });

                    //create and show the dialog
                    AlertDialog editItemDialog = editItemDialogBuilder.create();
                    editItemDialog.show();

                    //show keyboard
                    editItemDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            });

            //on click for item archive
            holder.archive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //move item to the archive list and update the view
                    Utility.moveToArchive(position);
                    notifyDataSetChanged();
                    Utility.writeData(context); //backup data

                    //display success message
                    Snackbar infoBar = Snackbar.make(view, R.string.item_archived, Snackbar.LENGTH_SHORT);
                    infoBar.show();
                }
            });

            //on click for item delete
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //remove item from adapter and update view
                    bucketList.remove(position);
                    notifyDataSetChanged();
                    Utility.writeData(context); //backup data

                    //display success message and give option to undo
                    Snackbar infoBar = Snackbar.make(view, R.string.item_deleted, Snackbar.LENGTH_LONG);
                    infoBar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //undo deleting
                            bucketList.add(position, item);
                            notifyDataSetChanged();
                            Utility.writeData(context); //backup data
                        }
                    });
                    infoBar.setActionTextColor(Color.WHITE);
                    infoBar.show();
                }
            });
        }

        holder.handle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = MotionEventCompat.getActionMasked(event);
                if (action == MotionEvent.ACTION_DOWN) {
                    dragStartListener.onStartDrag(holder);
                    BucketItemListView.itemsMoved = true;
                }
                return false;
            }
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

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            item = itemView.findViewById(R.id.row_text);
            done = itemView.findViewById(R.id.row_check);
            if(BucketItemListAdapter.tablet){
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

    public Item getItem(int position){
        return this.bucketList.get(position);
    }

    public ArrayList<Item> getBucketList(){
        return this.bucketList;
    }

    public void setBucketList(ArrayList<Item> bucketList) {
        this.bucketList = bucketList;
    }
}
