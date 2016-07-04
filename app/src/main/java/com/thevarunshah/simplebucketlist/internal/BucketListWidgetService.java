package com.thevarunshah.simplebucketlist.internal;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.thevarunshah.classes.Item;
import com.thevarunshah.simplebucketlist.R;

import java.util.ArrayList;

public class BucketListWidgetService extends RemoteViewsService {

    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {

        return new ListViewRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListViewRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private Context mContext;
    private ArrayList<Item> records;
    private int mAppWidgetId;

    public ListViewRemoteViewsFactory(Context context, Intent intent) {

        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    //initialize the data set
    public void onCreate() {

        records = Utility.getBucketList();
        if(records.size() == 0){
            Utility.readData(mContext);
        }
    }

    @Override
    public void onDataSetChanged() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return records.size();
    }

    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.widget_row);

        //get item and set the appropriate checkbox
        Item item = records.get(position);
        String data = item.getItemText();
        rv.setTextViewText(R.id.widgetrow_text, data);
        if(item.isDone()){
            rv.setImageViewResource(R.id.widgetrow_check, R.drawable.ic_check_box_black_24dp);
            rv.setInt(R.id.widgetrow_text, "setPaintFlags", Paint.ANTI_ALIAS_FLAG | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else{
            rv.setImageViewResource(R.id.widgetrow_check, R.drawable.ic_check_box_outline_blank_black_24dp);
            rv.setInt(R.id.widgetrow_text, "setPaintFlags", Paint.ANTI_ALIAS_FLAG & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        //setup onclick action for individual items
        final Intent onClickIntent = new Intent(mContext, BucketListWidgetProvider.class);
        onClickIntent.setAction(BucketListWidgetProvider.CLICK_ACTION);
        onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
        rv.setOnClickFillInIntent(R.id.widgetrow_text, onClickIntent);
        rv.setOnClickFillInIntent(R.id.widgetrow_check, onClickIntent);

        return rv;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
