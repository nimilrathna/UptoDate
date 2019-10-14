package com.nimil.uptodate;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class CustomerNameCursorAdapter extends CursorAdapter {
    Context mContext;
    public CustomerNameCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
        this.mContext=context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String keyword = cursor.getString(cursor.getColumnIndex(UptoDateProviderContract.Customers.COLUMN_CUSTOMER_NAME));
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        tv.setText(keyword);
    }
    @Override
    public CharSequence convertToString(Cursor cursor) {
        String value = "";
        value = cursor.getString(cursor.getColumnIndex(UptoDateProviderContract.Customers.COLUMN_CUSTOMER_NAME));
        return value;
    }
}
