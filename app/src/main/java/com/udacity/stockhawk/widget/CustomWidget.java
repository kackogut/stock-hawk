package com.udacity.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.DetailStockActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Implementation of App Widget functionality.
 */
public class CustomWidget extends AppWidgetProvider {


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.custom_widget);

            Intent intent = new Intent(context,MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
            remoteViews.setOnClickPendingIntent(R.id.widget_main_layout, pendingIntent);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                setRemoteAdapter(context, remoteViews);
            }else
                setRemoteAdapterV11(context, remoteViews);

            Intent clickIntent = new Intent(context, MainActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                                        .addNextIntentWithParentStack(clickIntent)
                                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate);
            remoteViews.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views){
        views.setRemoteAdapter(R.id.widget_list_view,
                new Intent(context, StockWidgetService.class));
    }
    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views){
        views.setRemoteAdapter(0,R.id.widget_list_view,
                new Intent(context, StockWidgetService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent){
        super.onReceive(context,intent);
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetsIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            onUpdate(context, appWidgetManager, appWidgetsIds);
        }
    }
}

