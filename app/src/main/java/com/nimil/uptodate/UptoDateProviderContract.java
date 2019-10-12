package com.nimil.uptodate;

import android.net.Uri;
import android.provider.BaseColumns;

public final class UptoDateProviderContract {
    private UptoDateProviderContract(){}
    public static final String AUTHORITY="com.nimil.uptodate.provider";
    public static final Uri AUTHORITY_URI=Uri.parse("content://"+AUTHORITY);

    protected interface ProductColumns{
        public static final String COLUMN_PRODUCT_NAME="product_name";
        public static final String COLUMN_PURCHASE_DATE="purchase_date";
        public static final String COLUMN_QUANTITY="quantity";
        public static final String COLUMN_ACTUAL_PRICE="actual_price";
        public static final String COLUMN_SELLING_PRICE="selling_price";
    }
    protected interface CustomerColumns{
        public static final String COLUMN_CUSTOMER_NAME="customer_name";
        public static final String COLUMN_CONTACT_NUMBER="contact_number";
        public static final String COLUMN_ALTERNATIVE_NUMBER="alternative_number";
        public static final String COLUMN_ADDRESS="address";
        public static final String COLUMN_EMAIL_ID ="email_id";
    }
    protected interface OrderColumns{
        public static final String COLUMN_ORDER_DATE="order_date";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final String COLUMN_ORDER_QUANTITY="order_quantity";
        public static final String COLUMN_CASH_PAYED="cash_payed";
        public static final String COLUMN_ORDER_STATUS="order_status";
        public static final String COLUMN_CUSTOMER_ID="customer_id";
        public static final String COLUMN_ORDER_CUSTOMER_NAME="order_customer_name";
        public static final String COLUMN_PRODUCT_ID="product_id";
        public static final String COLUMN_ORDER_PRODUCT_NAME="order_product_name";
    }
    public static final class Products implements BaseColumns,ProductColumns {
        public static final String PATH="products";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final Uri CONTENT_URI=Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }
    public static final class Customers implements BaseColumns,CustomerColumns {
        public static final String PATH="customers";
        public static final String COLUMN_ENTRY_DATE="entry_date";
        public static final Uri CONTENT_URI=Uri.withAppendedPath(AUTHORITY_URI,PATH);
    }
    public static final class Orders implements BaseColumns,OrderColumns,ProductColumns,CustomerColumns{
        public static final String PATH="orders";
        public static final Uri CONTENT_URI=Uri.withAppendedPath(AUTHORITY_URI,PATH);
        public static final String EXTENDED_PATH="orders_extended";
        public static final Uri CONTENT_EXTENDED_URI=Uri.withAppendedPath(AUTHORITY_URI,EXTENDED_PATH);
    }
}
