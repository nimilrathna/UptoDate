package com.nimil.uptodate;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.provider.BaseColumns;

import com.nimil.uptodate.UptoDateDatabaseContract.*;
import com.nimil.uptodate.UptoDateProviderContract.*;

public class UptoDateProvider extends ContentProvider {
    UptoDateOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher=new UriMatcher(UriMatcher.NO_MATCH);

    public static final int PRODUCTS = 0;
    public static final int CUSTOMERS = 1;
    public static final int ORDERS = 2;
    public static final int ORDERS_EXTENDED = 3;

    public static final int ORDER_EXTENDED_ROW=7;
    public static final int ORDER_ROW = 4;
    public static final int PRODUCT_ROW = 5;
    public static final int CUSTOMER_ROW = 6;
    static{
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, Products.PATH, PRODUCTS);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Customers.PATH, CUSTOMERS);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Orders.PATH, ORDERS);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY,UptoDateProviderContract.Orders.EXTENDED_PATH, ORDERS_EXTENDED);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Orders.EXTENDED_PATH+"/#", ORDER_EXTENDED_ROW);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Orders.PATH+"/#", ORDER_ROW);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Customers.PATH+"/#", CUSTOMER_ROW);
        sUriMatcher.addURI(UptoDateProviderContract.AUTHORITY, UptoDateProviderContract.Products.PATH+"/#", PRODUCT_ROW);
    }
    public UptoDateProvider() {
    }
    @Override
    public boolean onCreate() {
        mDbOpenHelper=new UptoDateOpenHelper(getContext());
        return true;
    }
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db =mDbOpenHelper.getWritableDatabase();
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
        int uriMatch=sUriMatcher.match(uri);
        int rowsAffected=0;
        try{
            switch(uriMatch){
                case PRODUCTS:
                    rowsAffected=db.delete(ProductInfoEntry.TABLE_NAME,selection,selectionArgs);
                    break;
                case CUSTOMERS:
                    rowsAffected=db.delete(CustomerInfoEntry.TABLE_NAME,selection,selectionArgs);
                    break;
                case ORDERS:
                    rowsAffected=db.delete(OrderInfoEntry.TABLE_NAME,selection,selectionArgs);
                    break;
            }
        }
        catch (SQLiteException ex) {
            return -1;
        }
        return rowsAffected;
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db =mDbOpenHelper.getWritableDatabase();
        long rowId=-1;
        Uri rowUri=null;
        int uriMatch=sUriMatcher.match(uri);
        switch(uriMatch){
            case PRODUCTS:
                rowId=db.insert(ProductInfoEntry.TABLE_NAME,null,values);
                rowUri= ContentUris.withAppendedId(Products.CONTENT_URI,rowId);
                break;
            case CUSTOMERS:
                rowId=db.insert(CustomerInfoEntry.TABLE_NAME,null,values);
                rowUri= ContentUris.withAppendedId(Customers.CONTENT_URI,rowId);
                break;
            case ORDERS:
                rowId=db.insert(OrderInfoEntry.TABLE_NAME,null,values);
                rowUri=ContentUris.withAppendedId(Orders.CONTENT_URI,rowId);
                break;
            case ORDERS_EXTENDED:
                break;
        }
        return rowUri;
    }



    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor=null;
        SQLiteDatabase db=mDbOpenHelper.getReadableDatabase();
        long rowId;
        String rowSelection;
        String[]  rowSelectionArgs;
        int uriMatch=sUriMatcher.match(uri);
        switch(uriMatch){
            case PRODUCTS:
                cursor=db.query(ProductInfoEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case CUSTOMERS:
                cursor=db.query(CustomerInfoEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ORDERS:
                cursor=db.query(OrderInfoEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case ORDERS_EXTENDED:
                cursor=ordersExpandedQuery(db,projection,selection,selectionArgs,sortOrder);
                break;
            case ORDER_EXTENDED_ROW:
                rowId=ContentUris.parseId(uri);
                rowSelection=OrderInfoEntry.getQName(OrderInfoEntry._ID) + " =?";
                rowSelectionArgs=new String[]{Long.toString(rowId)};
                cursor=ordersExpandedQuery(db,projection,rowSelection,rowSelectionArgs,null);
                break;
            case ORDER_ROW:
                rowId=ContentUris.parseId(uri);
                rowSelection=OrderInfoEntry._ID + " =?";
                rowSelectionArgs=new String[]{Long.toString(rowId)};
                cursor=db.query(OrderInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,
                        null,null,null);
                break;
            case PRODUCT_ROW:
                rowId=ContentUris.parseId(uri);
                rowSelection=ProductInfoEntry._ID + " =?";
                rowSelectionArgs=new String[]{Long.toString(rowId)};
                cursor=db.query(ProductInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,
                        null,null,null);
                break;
            case CUSTOMER_ROW:
                rowId=ContentUris.parseId(uri);
                rowSelection=CustomerInfoEntry._ID + " =?";
                rowSelectionArgs=new String[]{Long.toString(rowId)};
                cursor=db.query(CustomerInfoEntry.TABLE_NAME,projection,rowSelection,rowSelectionArgs,
                        null,null,null);
                break;
        }
        return cursor;
    }

    private Cursor ordersExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String[] columns=new String[projection.length];
        /*for(int i=0;i<projection.length;i++) {
            columns[i]=projection[i].equals(BaseColumns._ID) || projection[i].equals(CourseIDColumns.COLUMN_COURSE_ID)?
                    NoteInfoEntry.getQName(projection[i]):projection[i];
        }*/
        if(sortOrder!=null && sortOrder.equals(Orders.COLUMN_ENTRY_DATE)){
            sortOrder=OrderInfoEntry.getQName(sortOrder);
        }
        String tablesWithJoin= OrderInfoEntry.TABLE_NAME +
                " LEFT JOIN "+ CustomerInfoEntry.TABLE_NAME+" ON "+
                OrderInfoEntry.COLUMN_CUSTOMER_ID +"="+ CustomerInfoEntry.getQName(CustomerInfoEntry._ID) +
                " LEFT JOIN "+ ProductInfoEntry.TABLE_NAME + " ON "+
                OrderInfoEntry.COLUMN_PRODUCT_ID + "="+ProductInfoEntry.getQName(ProductInfoEntry._ID);

        return db.query(tablesWithJoin,projection,selection,selectionArgs,null,null,sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db =mDbOpenHelper.getWritableDatabase();
        int uriMatch=sUriMatcher.match(uri);
        int rowsAffected=0;
        switch(uriMatch){
            case PRODUCTS:
                rowsAffected=db.update(ProductInfoEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case CUSTOMERS:
                rowsAffected=db.update(CustomerInfoEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case ORDERS:
                rowsAffected=db.update(OrderInfoEntry.TABLE_NAME,values,selection,selectionArgs);
                break;
            case ORDERS_EXTENDED:
                break;
        }
        return rowsAffected;
    }
}
