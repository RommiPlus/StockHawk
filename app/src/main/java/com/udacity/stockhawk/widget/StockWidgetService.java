package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static android.provider.BaseColumns._ID;
import static com.udacity.stockhawk.ui.MainActivity.STOCK_NAME;

/**
 * Created by 123 on 2017/1/4.
 */

public class StockWidgetService extends RemoteViewsService {

    private Cursor mCursor;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StockRemoteViewsFactory();
    }

    class StockRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private DecimalFormat dollarFormatWithPlus;
        private DecimalFormat dollarFormat;
        private DecimalFormat percentageFormat;

        @Override
        public void onCreate() {
            dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+$");
            percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(Locale.getDefault());
            percentageFormat.setMaximumFractionDigits(2);
            percentageFormat.setMinimumFractionDigits(2);
            percentageFormat.setPositivePrefix("+");
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                mCursor.close();
            }

            // This method is called by the app hosting the widget (e.g., the launcher)
            // However, our ContentProvider is not exported so it doesn't have access to the
            // data. Therefore we need to clear (and finally restore) the calling identity so
            // that calls use our process and permission
            final long identityToken = Binder.clearCallingIdentity();
            mCursor = getContentResolver().query(
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS,
                    null, null, Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            // Bind data with view
            if (position == AdapterView.INVALID_POSITION ||
                    mCursor == null || !mCursor.moveToPosition(position)) {
                return null;
            }

            RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
            views.setTextViewText(R.id.symbol, mCursor.getString(Contract.Quote.POSITION_SYMBOL));
            views.setTextViewText(R.id.price, dollarFormat.format(mCursor.getFloat(Contract.Quote.POSITION_PRICE)));

            float rawAbsoluteChange = mCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = mCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

            if (rawAbsoluteChange > 0) {
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            } else {
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }

            String change = dollarFormatWithPlus.format(rawAbsoluteChange);
            String percentage = percentageFormat.format(percentageChange / 100);

            if (PrefUtils.getDisplayMode(StockWidgetService.this)
                    .equals(StockWidgetService.this.getString(R.string.pref_display_mode_absolute_key))) {
                views.setTextViewText(R.id.change, change);
            } else {
                views.setTextViewText(R.id.change, percentage);
            }

            int symbolColumn = mCursor.getColumnIndex(Contract.Quote.COLUMN_SYMBOL);
            final Intent fillInIntent = new Intent();
            fillInIntent.putExtra(STOCK_NAME, mCursor.getString(symbolColumn));
            views.setOnClickFillInIntent(R.id.stock_today, fillInIntent);
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(getPackageName(), R.layout.list_item_quote);
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getInt(mCursor.getColumnIndexOrThrow(_ID));
            }
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
