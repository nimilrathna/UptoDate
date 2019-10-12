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
    //Cursor mCursor;
    public CustomerNameCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
        this.mContext=context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return view;
        /*LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.keyword_autocomplete, null);
        return v;*/
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        String keyword = cursor.getString(cursor.getColumnIndex(UptoDateProviderContract.Customers.COLUMN_CUSTOMER_NAME));
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        tv.setText(keyword);
    }
    @Override
    public CharSequence convertToString(Cursor cursor) {
        //return super.convertToString(cursor);
        String value = "";
        /*switch (type) {
            case Keywords:
                value = cursor.getString(DatabaseHelper.KEYWORD_COLUMN);
                break;
            case Cities:
                value = cursor.getString(DatabaseHelper.CITY_COLUMN);
                break;
        }*/
        value = cursor.getString(cursor.getColumnIndex(UptoDateProviderContract.Customers.COLUMN_CUSTOMER_NAME));
        return value;
    }
}
