package com.nimil.uptodate;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.nimil.uptodate.UptoDateDatabaseContract.*;

public class UptoDateOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME="UptoDate.db";
    public static final int DATABASE_VERSION=13;
    public UptoDateOpenHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ProductInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(CustomerInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(OrderInfoEntry.SQL_CREATE_TABLE);
        db.execSQL(ProductInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(CustomerInfoEntry.SQL_CREATE_INDEX1);
        db.execSQL(OrderInfoEntry.SQL_CREATE_INDEX1);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        //db.execSQL("DROP TABLE IF EXISTS products_info");
        //db.execSQL("DROP TABLE IF EXISTS customers_info");
        //db.execSQL("DROP TABLE IF EXISTS orders_info");
        //onCreate(db);
    }
}
