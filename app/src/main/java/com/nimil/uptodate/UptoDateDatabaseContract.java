package com.nimil.uptodate;

import android.provider.BaseColumns;

public final class UptoDateDatabaseContract {
    public UptoDateDatabaseContract(){}
    public static final class ProductInfoEntry implements BaseColumns{
        public static final String TABLE_NAME="products_info";
        public static final String COLUMN_PRODUCT_NAME="product_name";
        public static final String COLUMN_PURCHASE_DATE="purchase_date";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final String COLUMN_QUANTITY="quantity";
        public static final String COLUMN_ACTUAL_PRICE="actual_price";
        public static final String COLUMN_SELLING_PRICE="selling_price";
        public static final String INDEX1=TABLE_NAME+"_INDEX1";
        public static final String SQL_CREATE_INDEX1="CREATE INDEX "+INDEX1+" ON "+TABLE_NAME+"("+COLUMN_PRODUCT_NAME+")";

        public static String getQName(String COLUMN_NAME){
            return TABLE_NAME +"."+COLUMN_NAME;
        }

        public static final String SQL_CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"("+
                _ID+" INTEGER PRIMARY KEY, "+
                COLUMN_PRODUCT_NAME+" TEXT UNIQUE NOT NULL, "+
                COLUMN_PURCHASE_DATE+" TEXT NOT NULL, "+
                COLUMN_ENTRY_DATE+" TEXT NOT NULL, "+
                COLUMN_QUANTITY+" INTEGER NOT NULL, "+
                COLUMN_ACTUAL_PRICE+" REAL NOT NULL, "+
                COLUMN_SELLING_PRICE+" REAL NOT NULL)";
    }
    public static final class CustomerInfoEntry implements BaseColumns{
        public static final String TABLE_NAME="customers_info";
        public static final String COLUMN_CUSTOMER_NAME="customer_name";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final String COLUMN_CONTACT_NUMBER="contact_number";
        public static final String COLUMN_ALTERNATIVE_NUMBER="alternative_number";
        public static final String COLUMN_ADDRESS="address";
        public static final String COLUMN_EMAIL_ID ="email_id";
        public static final String INDEX1=TABLE_NAME+"_INDEX1";
        public static final String SQL_CREATE_INDEX1="CREATE INDEX "+INDEX1+" ON "+TABLE_NAME+"("+COLUMN_CUSTOMER_NAME+")";

        public static String getQName(String COLUMN_NAME){
            return TABLE_NAME +"."+COLUMN_NAME;
        }

        public static final String SQL_CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"("+
                _ID+" INTEGER PRIMARY KEY, "+
                COLUMN_CUSTOMER_NAME+" TEXT NOT NULL, "+
                COLUMN_ENTRY_DATE+" TEXT NOT NULL, "+
                COLUMN_CONTACT_NUMBER+" TEXT NOT NULL, "+
                COLUMN_ALTERNATIVE_NUMBER+" TEXT, "+
                COLUMN_ADDRESS+" TEXT, "+
                COLUMN_EMAIL_ID +" TEXT)";
    }
    public static final class OrderInfoEntry implements BaseColumns{
        public static final String TABLE_NAME="orders_info";
        public static final String COLUMN_CUSTOMER_ID="customer_id";
        public static final String COLUMN_ORDER_CUSTOMER_NAME="order_customer_name";
        public static final String COLUMN_PRODUCT_ID="product_id";
        public static final String COLUMN_ORDER_PRODUCT_NAME="order_product_name";
        public static final String COLUMN_ORDER_DATE="order_date";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final String COLUMN_ORDER_QUANTITY="order_quantity";
        public static final String COLUMN_CASH_PAYED="cash_payed";
        public static final String COLUMN_ORDER_STATUS="order_status";
        public static final String INDEX1=TABLE_NAME+"_INDEX1";
        public static final String SQL_CREATE_INDEX1="CREATE INDEX "+INDEX1+" ON "+TABLE_NAME+"("+COLUMN_ORDER_DATE+")";

        public static String getQName(String COLUMN_NAME){
            return TABLE_NAME +"."+COLUMN_NAME;
        }

        public static final String SQL_CREATE_TABLE="CREATE TABLE "+TABLE_NAME+"("+
                _ID+" INTEGER PRIMARY KEY, "+
                COLUMN_CUSTOMER_ID+" INTEGER, "+
                COLUMN_ORDER_CUSTOMER_NAME+" TEXT NOT NULL, "+
                COLUMN_PRODUCT_ID+" INTEGER, "+
                COLUMN_ORDER_PRODUCT_NAME+" TEXT NOT NULL, "+
                COLUMN_ORDER_DATE+" TEXT NOT NULL, "+
                COLUMN_ENTRY_DATE+" TEXT NOT NULL, "+
                COLUMN_ORDER_QUANTITY+" INTEGER NOT NULL, "+
                COLUMN_CASH_PAYED+" REAL, "+
                COLUMN_ORDER_STATUS+" TEXT NOT NULL,"+
                "FOREIGN KEY("+COLUMN_CUSTOMER_ID+") REFERENCES "+CustomerInfoEntry.TABLE_NAME+"("+CustomerInfoEntry._ID+")" +
                "ON UPDATE SET NULL ON DELETE SET NULL," +
                "FOREIGN KEY("+COLUMN_PRODUCT_ID+") REFERENCES "+ProductInfoEntry.TABLE_NAME+"("+ProductInfoEntry._ID+
                ")ON UPDATE SET NULL ON DELETE SET NULL)";
    }
}
