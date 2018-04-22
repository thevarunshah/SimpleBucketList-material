package com.thevarunshah.simplebucketlist.internal;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.thevarunshah.simplebucketlist.BucketItemListView;
import com.thevarunshah.simplebucketlist.R;

public class BucketListWidgetProvider extends AppWidgetProvider {

    public static String CLICK_ACTION = "com.thevarunshah.simplebucketlist.internal.CLICK";
    public static String UPDATE_ACTION = "com.thevarunshah.simplebucketlist.internal.UPDATE";

    @Override
    public void onReceive(Context context, Intent intent){

        final String action = intent.getAction();
        if (action != null && action.equals(CLICK_ACTION)) {
            //start main activity
            Intent i = new Intent(context, BucketItemListView.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
        else if (action != null && action.equals(UPDATE_ACTION)) {
            //update the widget data
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, BucketListWidgetProvider.class);
            manager.notifyAppWidgetViewDataChanged(manager.getAppWidgetIds(componentName), R.id.widget_listview);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        //update each of the app widgets with the remote adapter
        for (int appWidgetId : appWidgetIds) {

            //setup the intent such that it calls the service to populate the widget views
            Intent intent = new Intent(context, BucketListWidgetService.class);

            //add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            //instantiate the RemoteViews object for the app widget layout
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.bucketlist_widget);
            //setup the adapter such that it populates the data
            rv.setRemoteAdapter(R.id.widget_listview, intent);
            //if there are no items in the bucket list, display this empty view
            rv.setEmptyView(R.id.widget_listview, R.id.empty_view);

            //setup onclick actions for the listview and empty view
            final Intent onClickIntent = new Intent(context, BucketListWidgetProvider.class);
            onClickIntent.setAction(BucketListWidgetProvider.CLICK_ACTION);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.empty_view, onClickPendingIntent);
            rv.setPendingIntentTemplate(R.id.widget_listview, onClickPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
