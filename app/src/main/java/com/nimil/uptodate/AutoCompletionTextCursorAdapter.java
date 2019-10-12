package com.nimil.uptodate;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.nimil.uptodate.UptoDateProviderContract.Customers;
import com.nimil.uptodate.UptoDateProviderContract.Products;

public class AutoCompletionTextCursorAdapter extends CursorAdapter {

    Context mContext;
    public static final int CUSTOMER_NAMES=0;
    public static final int PRODUCT_NAMES=1;
    private final int mCursorData;
    private TextView mTextView;
    private String mIdSearchConstraint;
    AutoCompletionTextCursorAdapter(Context mContext,Cursor c,int mCursorData){
        super(mContext,c);
        this.mContext=mContext;
        this.mCursorData=mCursorData;
        mIdSearchConstraint=null;
    }


    public void setIdSearchConstarint(String idSearchConstraint) {
        this.mIdSearchConstraint = idSearchConstraint;
    }
    public String getIdSearchConstarint(){return mIdSearchConstraint;}

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        mTextView=view.findViewById(android.R.id.text1);
        switch(mCursorData){
            case CUSTOMER_NAMES:
                mTextView.setText(cursor.getString(cursor.getColumnIndex(Customers.COLUMN_CUSTOMER_NAME)));
                //mId=cursor.getString(cursor.getColumnIndex(Customers._ID));
                break;
            case PRODUCT_NAMES:
                mTextView.setText(cursor.getString(cursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME)));
                //mId=cursor.getString(cursor.getColumnIndex(Products._ID));
                //mProductQuantity=Integer.parseInt(cursor.getString(cursor.getColumnIndex(Products.COLUMN_QUANTITY)));
                break;
        }

    }
    @Override
    public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
        if (getFilterQueryProvider() != null) {
            return getFilterQueryProvider().runQuery(constraint);
        }
        Cursor cursor=null;
        String selections = null;
        String orderBy;
        String selectionArgs[] = null;
        switch(mCursorData) {
            case CUSTOMER_NAMES:
                String[] customerColumns = {
                    Customers._ID,
                    Customers.COLUMN_CUSTOMER_NAME,
                        Customers.COLUMN_CONTACT_NUMBER
                };
                orderBy = Customers.COLUMN_CUSTOMER_NAME;
                if (constraint != null) {
                    if(mIdSearchConstraint!=null) {
                        selections = Customers.COLUMN_CUSTOMER_NAME + " LIKE ? AND "+
                        Customers._ID +" LIKE ? ";
                        selectionArgs = new String[]{
                                "%" + constraint + "%",
                                mIdSearchConstraint};
                        mIdSearchConstraint=null;
                    }
                    else{
                        selections = Customers.COLUMN_CUSTOMER_NAME + " LIKE ? ";
                        selectionArgs = new String[]{"%" + constraint + "%"};
                    }
                }
                    cursor = mContext.getContentResolver().query(Customers.CONTENT_URI, customerColumns, selections, selectionArgs, orderBy);
                break;
            case PRODUCT_NAMES:
                String[] productColumns={
                        Products._ID,
                        Products.COLUMN_PRODUCT_NAME,
                        Products.COLUMN_QUANTITY
                };
                orderBy = Products.COLUMN_PRODUCT_NAME;
                if(constraint!=null){
                    selections=Products.COLUMN_PRODUCT_NAME +" LIKE ?";
                    selectionArgs=new String[]{"%"+constraint+"%"};
                }
                cursor = mContext.getContentResolver().query(Products.CONTENT_URI, productColumns, selections, selectionArgs, orderBy);
                break;
        }

        return cursor;
    }
    @Override
    public String convertToString(Cursor cursor) {
        String result=null;
        switch(mCursorData){
            case CUSTOMER_NAMES:
                result=cursor.getString(cursor.getColumnIndex(Customers.COLUMN_CUSTOMER_NAME));
                break;
            case PRODUCT_NAMES:
                result=cursor.getString(cursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME));
        }
        return result;
    }

}
