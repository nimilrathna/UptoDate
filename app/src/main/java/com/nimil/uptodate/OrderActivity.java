package com.nimil.uptodate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.nimil.uptodate.UptoDateProviderContract.Customers;
import com.nimil.uptodate.UptoDateProviderContract.Orders;
import com.nimil.uptodate.UptoDateProviderContract.Products;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;


public class OrderActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, CustomDeleteDialog.DeleteDialogListener {
    public static final String ORDER_ID = "com.nimil.uptodate.ORDER_ID";
    private static final int LOADER_ORDER = 0;
    private static final int LOADER_CUSTUMER_NAMES = 1;
    private static final int LOADER_PRODUCT_NAMES = 2;
    public static final int DEFAULT_VALUE = -1;
    private boolean mIsNewOrder;
    private DatePicker mDpOrderDate;
    private TextView mTextQuantity;
    private TextView mTextCashPayed;
    //private CheckBox mCheckPaymentCompleted;
    //private CheckBox mCheckDelivered;

    private Cursor mOrderCursor;

    private Cursor mCustomerNamesCursor;
    private Cursor mProductNameCursor;
    private Uri mOrderUri;
    private int mCustomerIdPos;
    private int mProductIdPos;
    private int mCustomerNamePos;
    private int mProductNamePos;
    private int mOrderDatePos;
    private int mQunatityPos;
    private int mCashPayedPos;
    private int mPaymentCompletedPos;
    private int mDeliveredPos;
    private int mOrderId;

    private SimpleCursorAdapter mAdapterCustomerNames;
    private SimpleCursorAdapter mAdapterProductNames;
    private boolean mCustomerNamesloadFinished = false;
    private boolean mProductNamesloadFinished = false;
    private boolean mOrderloadFinished = false;
    private boolean mIsDeleteConfirmed;
    private static final int ID_NEW = -1;

    private AutoCompleteTextView mAutoTextCustomerName;
    private AutoCompleteTextView mAutoTextProductName;
    private AutoCompletionTextCursorAdapter mCustomerNameAutoTextCursorAdapter;
    private AutoCompletionTextCursorAdapter mProductNameAutoTextCursorAdapter;

    private int mPreviousQuantity;
    private String mCustomerId;
    private String mProductId;
    private int mProductTotalQuantity;
    private String mCustomerPhoneNo;
    private int mProductQuantityPos;
    private int mProductTotalQuantityPos;
    private String mSelectedCustomerName;
    private String mSelectedProductName;
    private Spinner mSpinnerOrderStatus;
    private ArrayAdapter<CharSequence> mStatusArrayAdapter;
    private int mOrderStatusPos;
    private String mCurrency;
    private String mPreviousProductId;
    private int mPreviousProductTotalQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        readDisplayStateValues();
        intialActions();
    }

    @Override
    protected void onResume() {
        //getSupportLoaderManager().restartLoader(LOADER_CUSTUMER_NAMES,null,this).forceLoad();
        //getSupportLoaderManager().restartLoader(LOADER_PRODUCT_NAMES,null,this).forceLoad();
//        mSpinnerProductName.setSelection(-1);
        //      mSpinnerCustomerName.setSelection(-1);
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mOrderId = intent.getIntExtra(ORDER_ID, DEFAULT_VALUE);
        mIsNewOrder = (mOrderId == DEFAULT_VALUE);
    }

    private void updateCurrency() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String countryCode = pref.getString("currency", "");
        mCurrency = Currency.getInstance(countryCode).getSymbol();

    }

    private void intialActions() {
       /* mSpinnerCustomerName=findViewById(R.id.spinner_customer_name);
        mSpinnerProductName=findViewById(R.id.spinner_product_name);*/
        updateCurrency();
        mDpOrderDate = findViewById(R.id.dp_order_date);
        mTextQuantity = findViewById(R.id.text_quantity);
        mTextCashPayed = findViewById(R.id.text_profit);
        TextView textCashPayedLabel = findViewById(R.id.text_cash_payed_label);
        textCashPayedLabel.setText("Cash Payed in " + mCurrency);
        //mCheckPaymentCompleted=findViewById(R.id.check_payment_completed);
        //mCheckDelivered=findViewById(R.id.layout_constrain_order);
        mSpinnerOrderStatus = findViewById(R.id.spinner_order_status);

        mStatusArrayAdapter = ArrayAdapter.createFromResource(this,
                R.array.order_status, android.R.layout.simple_spinner_item);
        mStatusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerOrderStatus.setAdapter(mStatusArrayAdapter);
      /*  mAdapterCustomerNames = new SimpleCursorAdapter(this,R.layout.content_spinner_item,null,
                new String[] {Customers.COLUMN_CUSTOMER_NAME},
                new int[] {R.id.text_item},0);
        mAdapterCustomerNames.setDropDownViewResource(R.layout.content_spinner_item);
        mSpinnerCustomerName.setAdapter(mAdapterCustomerNames);

        mAdapterProductNames = new SimpleCursorAdapter(this,R.layout.content_spinner_item,null,
                new String[] {Products.COLUMN_PRODUCT_NAME},
                new int[] {R.id.text_item},0);
        mAdapterProductNames.setDropDownViewResource(R.layout.content_spinner_item);
        mSpinnerProductName.setAdapter(mAdapterProductNames);
        mSpinnerProductName.setSelection(-1);*/

        mAutoTextCustomerName = findViewById(R.id.autotext_customer_name);
        mAutoTextProductName = findViewById(R.id.autotext_product_name);

        mCustomerNameAutoTextCursorAdapter = new AutoCompletionTextCursorAdapter(this, null, AutoCompletionTextCursorAdapter.CUSTOMER_NAMES);
        mAutoTextCustomerName.setAdapter(mCustomerNameAutoTextCursorAdapter);

        mAutoTextCustomerName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currentText = mAutoTextCustomerName.getText().toString();
                if (!b && mSelectedCustomerName != null && !currentText.equals(mSelectedCustomerName)) {
                    mCustomerId = null;
                }
            }
        });
        mAutoTextCustomerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mCustomerId = getSelectedCustomerId(i);
                mSelectedCustomerName = mAutoTextCustomerName.getText().toString();
                mCustomerPhoneNo = getSelectedCustomerPhoneNo(i);
            }
        });

        /*setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                mCustomerId=mCustomerNameAutoTextCursorAdapter.getId();
            }
        });*/
        mProductNameAutoTextCursorAdapter = new AutoCompletionTextCursorAdapter(this, null, AutoCompletionTextCursorAdapter.PRODUCT_NAMES);
        mAutoTextProductName.setAdapter(mProductNameAutoTextCursorAdapter);
        mAutoTextProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                String currentText = mAutoTextProductName.getText().toString();
                if (!b && mSelectedProductName != null && !currentText.equals(mSelectedProductName)) {
                    mProductId = null;
                }
            }
        });
        mAutoTextProductName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mProductId = getSelectedProductId(i);
                mSelectedProductName = mAutoTextProductName.getText().toString();
                mProductTotalQuantity = getSelectedProductQuantity(i);
            }
        });
        Button button_add_update = findViewById(R.id.button_add_update_order);
        if (mIsNewOrder)
            button_add_update.setText("Add Order");
        else {
            String previousQuantity = mTextQuantity.getText().toString();
            if (previousQuantity.equals(""))
                mPreviousQuantity = 0;
            else
                mPreviousQuantity = Integer.parseInt(previousQuantity);
            button_add_update.setText("Update Order");
        }
        button_add_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTextQuantity.requestFocus();
                String currQuantity = mTextQuantity.getText().toString();
                int currentQuantity = Integer.parseInt(currQuantity.equals("") ? "0" : currQuantity);
                CustomMessageBox messageBox;
                if (!validateUserInput()) {
                    messageBox = new CustomMessageBox("Please Fill all the Mandatory(*) Fields");
                    messageBox.show(getSupportFragmentManager(), "Input Validation");
                    return;
                } else if (mCustomerId == null || mCustomerId.equals("")) {
                    messageBox = new CustomMessageBox("Customer Does not Exist");
                    messageBox.show(getSupportFragmentManager(), "Input Validation");
                    return;
                } else if (mProductId == null) {
                    messageBox = new CustomMessageBox("Product Does not Exist");
                    messageBox.show(getSupportFragmentManager(), "Input Validation");
                    return;
                }
                else if(currentQuantity<0){
                    messageBox = new CustomMessageBox("Quantity is in negative.");
                    messageBox.show(getSupportFragmentManager(), "Input Validation");
                    return;
                }
                if (mIsNewOrder) {
                    //insert
                    if (currentQuantity > mProductTotalQuantity) {
                        messageBox = new CustomMessageBox("Insufficient Product Quantity");
                        messageBox.show(getSupportFragmentManager(), "Input Validation");
                        return;
                    }
                    createNewOrder();
                    int quantity = mProductTotalQuantity - currentQuantity;
                    updateProductQuantity(mProductId,quantity);

                    sendSMStoCustomer();
                } else {
                    if(mPreviousProductId!=null && mPreviousProductId.equals(mProductId)) {
                        int quantityToReduce = currentQuantity - mPreviousQuantity;
                        if (quantityToReduce > 0 && quantityToReduce > mProductTotalQuantity) {
                            messageBox = new CustomMessageBox("Insufficient Product Quantity");
                            messageBox.show(getSupportFragmentManager(), "Input Validation");
                            return;
                        }
                        int quantity = mProductTotalQuantity - (quantityToReduce);
                        updateProductQuantity(mProductId,quantity);
                    }
                    else {
                        if(currentQuantity>mProductTotalQuantity){
                            messageBox = new CustomMessageBox("Insufficient Product Quantity");
                            messageBox.show(getSupportFragmentManager(), "Input Validation");
                            return;
                        }
                        if(mPreviousProductId!=null && !mPreviousProductId.equals("")) {
                            int quantity = mPreviousProductTotalQuantity + mPreviousQuantity;
                            updateProductQuantity(mPreviousProductId, quantity);
                        }
                        int quantity1 = mProductTotalQuantity - currentQuantity;
                        updateProductQuantity(mProductId,quantity1);
                    }
                    updateOrderDetails();

                }

            }
        });
        if (!mIsNewOrder)
            getSupportLoaderManager().restartLoader(LOADER_ORDER, null, this).forceLoad();

    }

    private void sendSMStoCustomer() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean sendMessage = pref.getBoolean("sms", false);
        if (sendMessage) {
            String message = pref.getString("message", "");
            try {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.SEND_SMS)
                        == PackageManager.PERMISSION_GRANTED) {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(mCustomerPhoneNo, null, message, null, null);
                }
                } catch(Exception ex){
                   //Do Nothing
                }
        }
    }


    private String getSelectedProductId(int i) {
        Cursor cursor=mProductNameAutoTextCursorAdapter.getCursor();
        if(cursor!=null){
            cursor.moveToPosition(i);
            return cursor.getString(cursor.getColumnIndex(Products._ID));
        }
        return null;
    }
    private int getSelectedProductQuantity(int i) {
        Cursor cursor=mProductNameAutoTextCursorAdapter.getCursor();
        if(cursor!=null){
            cursor.moveToPosition(i);
            return Integer.parseInt(cursor.getString(cursor.getColumnIndex(Products.COLUMN_QUANTITY)));
        }
        return -1;
    }

    private String getSelectedCustomerId(int i) {
        Cursor cursor=mCustomerNameAutoTextCursorAdapter.getCursor();
        if(cursor!=null){
            cursor.moveToPosition(i);
            return cursor.getString(cursor.getColumnIndex(Customers._ID));
        }
        return null;
    }
    private String getSelectedCustomerPhoneNo(int i) {
        Cursor cursor=mCustomerNameAutoTextCursorAdapter.getCursor();
        if(cursor!=null){
            cursor.moveToPosition(i);
            return cursor.getString(cursor.getColumnIndex(Customers.COLUMN_CONTACT_NUMBER));
        }
        return null;
    }

    private void updateProductQuantity(String productId, int quantity) {
        AsyncTask<String,Void,Void> task=new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                String quantity=strings[0];
                String productId=strings[1];
                ContentValues cv=new ContentValues();
                cv.put(Products.COLUMN_QUANTITY,quantity);
                String selection = Products._ID + " = ?";
                String[] selectionArgs = {productId};
                getContentResolver().update(Products.CONTENT_URI,cv,selection,selectionArgs);
                return null;
            }
        };
        ContentValues cv=new ContentValues();
        cv.put(Products.COLUMN_QUANTITY,quantity);
        task.execute(Integer.toString(quantity),productId);
    }

    private boolean validateUserInput() {
        if(mAutoTextCustomerName.getText().equals(""))
            return false;
        if(mAutoTextProductName.getText().equals(""))
            return false;
        if(mTextQuantity.getText().toString().equals(""))
            return false;

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.edit_order_menu, menu);

        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_cancel) {
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        CustomDeleteDialog customDeleteDialog=new CustomDeleteDialog("Order will be deleted forever");
        customDeleteDialog.show(getSupportFragmentManager(),"Delete Confirmation");
    }
    @Override
    public void applyDeleteReply(boolean isConfirmed) {
        mIsDeleteConfirmed=isConfirmed;
        if(mIsDeleteConfirmed)
            deleteOrder();
    }

    private void deleteOrder() {
        AsyncTask<Void,Void,Void> task= new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voidValues) {

                String selection = Orders._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mOrderId)};
                getContentResolver().delete(Orders.CONTENT_URI,selection,selectionArgs);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        task.execute();
    }


    private void updateOrderDetails() {
        AsyncTask<ContentValues,Void,Void> task= new AsyncTask<ContentValues,Void,Void>() {

            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                String selection = Orders._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mOrderId)};
                getContentResolver().update(Orders.CONTENT_URI,rowValues,selection,selectionArgs);
                //return noteUri;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        ContentValues cv=new ContentValues();
        cv.put(Orders.COLUMN_CUSTOMER_ID,mCustomerId);
        cv.put(Orders.COLUMN_ORDER_CUSTOMER_NAME,mAutoTextCustomerName.getText().toString());
        cv.put(Orders.COLUMN_PRODUCT_ID,mProductId);
        cv.put(Orders.COLUMN_ORDER_PRODUCT_NAME,mAutoTextProductName.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String entryDate = sdf.format(new Date());
        String orderDate=sdf.format(getDateFromDatePicker(mDpOrderDate));
        cv.put(Orders.COLUMN_ORDER_DATE,orderDate);
        cv.put(Orders.COLUMN_CASH_PAYED,mTextCashPayed.getText().toString());
        String txtQuantity=mTextQuantity.getText().toString();
        int quantity=txtQuantity.equals("")?0:Integer.parseInt(txtQuantity);
        cv.put(Orders.COLUMN_ORDER_QUANTITY,quantity);
        cv.put(Orders.COLUMN_ORDER_STATUS,mSpinnerOrderStatus.getSelectedItem().toString());
        String paymentCompleted="";
        /*(if(mCheckPaymentCompleted.isChecked())
            paymentCompleted="Y";
        else
            paymentCompleted="N";
        cv.put(Orders.COLUMN_PAYMENT_COMPLETED,paymentCompleted);
        String isDelivered;
        if(mCheckDelivered.isChecked())
            isDelivered="Y";
        else
            isDelivered="N";
        cv.put(Orders.COLUMN_PRODUCT_DELIVERED,isDelivered);*/
        task.execute(cv);
    }

    private void createNewOrder() {
        AsyncTask<ContentValues,Void,Uri> task= new AsyncTask<ContentValues,Void,Uri>() {

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                Uri noteUri=getContentResolver().insert(Orders.CONTENT_URI,rowValues);
                return noteUri;
            }
            @Override
            protected void onPostExecute(Uri uri) {
                mOrderUri=uri;
                finish();
                //Snackbar.make(m,R.string.product_added,Snackbar.LENGTH_SHORT).show();
            }
        };
        ContentValues cv=new ContentValues();

        cv.put(Orders.COLUMN_CUSTOMER_ID,mCustomerId);
        cv.put(Orders.COLUMN_ORDER_CUSTOMER_NAME,mAutoTextCustomerName.getText().toString());
        cv.put(Orders.COLUMN_PRODUCT_ID,mProductId);
        cv.put(Orders.COLUMN_ORDER_PRODUCT_NAME,mAutoTextProductName.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String entryDate = sdf.format(new Date());
        String orderDate=sdf.format(getDateFromDatePicker(mDpOrderDate));
        cv.put(Orders.COLUMN_ENTRY_DATE,entryDate);
        cv.put(Orders.COLUMN_ORDER_DATE,orderDate);
        cv.put(Orders.COLUMN_CASH_PAYED,mTextCashPayed.getText().toString());
        String txtQuantity=mTextQuantity.getText().toString();
        int quantity=txtQuantity.equals("")?0:Integer.parseInt(txtQuantity);
        cv.put(Orders.COLUMN_ORDER_QUANTITY,quantity);
        cv.put(Orders.COLUMN_ORDER_STATUS,mSpinnerOrderStatus.getSelectedItem().toString());
        /*String paymentCompleted="";
        if(mCheckPaymentCompleted.isChecked())
            paymentCompleted="Y";
        else
            paymentCompleted="N";
        cv.put(Orders.COLUMN_PAYMENT_COMPLETED,paymentCompleted);

        String isDelivered;
        if(mCheckDelivered.isChecked())
            isDelivered="Y";
        else
            isDelivered="N";
        cv.put(Orders.COLUMN_PRODUCT_DELIVERED,isDelivered);*/
        task.execute(cv);
    }
    public static java.util.Date getDateFromDatePicker(DatePicker datePicker){
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        return calendar.getTime();
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader=null;
        if(id==LOADER_ORDER) {
            String[] orderColumns = {
                    Orders.COLUMN_CUSTOMER_ID,
                    Orders.COLUMN_PRODUCT_ID,
                    Orders.COLUMN_ORDER_CUSTOMER_NAME,
                    Orders.COLUMN_ORDER_PRODUCT_NAME,
                    Orders.COLUMN_ORDER_DATE,
                    Orders.COLUMN_ORDER_QUANTITY,
                    Orders.COLUMN_QUANTITY,
                    Orders.COLUMN_CASH_PAYED,
                    Orders.COLUMN_ORDER_STATUS

            };
            mOrderUri = ContentUris.withAppendedId(Orders.CONTENT_EXTENDED_URI, mOrderId);
            loader=new CursorLoader(this,mOrderUri,orderColumns,null,null,null);
        }
        else if(id==LOADER_CUSTUMER_NAMES){
            String[] customerNamesColumn={
                    Customers._ID,
                    Customers.COLUMN_CUSTOMER_NAME
            };
            loader=new CursorLoader(this,Customers.CONTENT_URI,customerNamesColumn,null,null,Customers.COLUMN_CUSTOMER_NAME);
        }

        else if(id==LOADER_PRODUCT_NAMES){
            String[] productNamesColumn={
                    Products._ID,
                    Products.COLUMN_PRODUCT_NAME,
                    Products.COLUMN_QUANTITY
            };
            loader=new CursorLoader(this,Products.CONTENT_URI,productNamesColumn,null,null,Products.COLUMN_PRODUCT_NAME);
        }
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId()==LOADER_ORDER) {
            mOrderloadFinished = true;
            mOrderCursor = data;
            mCustomerIdPos=mOrderCursor.getColumnIndex(Orders.COLUMN_CUSTOMER_ID);
            mProductIdPos=mOrderCursor.getColumnIndex(Orders.COLUMN_PRODUCT_ID);
            mCustomerNamePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_CUSTOMER_NAME);
            mProductNamePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_PRODUCT_NAME);
            mProductQuantityPos = mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_QUANTITY);
            mProductTotalQuantityPos = mOrderCursor.getColumnIndex(Orders.COLUMN_QUANTITY);
            mOrderDatePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_DATE);
            mQunatityPos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_QUANTITY);
            //mPaymentCompletedPos=mOrderCursor.getColumnIndex(Orders.COLUMN_PAYMENT_COMPLETED);
            mCashPayedPos=mOrderCursor.getColumnIndex(Orders.COLUMN_CASH_PAYED);
            //mDeliveredPos=mOrderCursor.getColumnIndex(Orders.COLUMN_PRODUCT_DELIVERED);
            mOrderStatusPos = mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_STATUS);
            mOrderCursor.moveToNext();
            displayOrderDetails();

        }
        /*else if(loader.getId()==LOADER_CUSTUMER_NAMES){
            mCustomerNamesloadFinished = true;
            mCustomerNamesCursor=data;
            MatrixCursor matrixCursor = new MatrixCursor(new String[] { Customers._ID, Customers.COLUMN_CUSTOMER_NAME });
            matrixCursor.addRow(new Object[] { "-1","Create New Customer"  });
            MergeCursor mergeCursor = new MergeCursor(new Cursor[] { mCustomerNamesCursor,matrixCursor });
            mAdapterCustomerNames.changeCursor(mergeCursor);
            if(mOrderloadFinished&&mProductNamesloadFinished){
                displayOrderDetails();
                mCustomerNamesloadFinished=false;
            }
        }
        else if(loader.getId()==LOADER_PRODUCT_NAMES){
            mProductNamesloadFinished = true;
            mProductNameCursor=data;
            MatrixCursor matrixCursor = new MatrixCursor(new String[] { Products._ID, Products.COLUMN_PRODUCT_NAME});
            matrixCursor.addRow(new Object[] { "-1","Create New Product"  });
            MergeCursor mergeCursor = new MergeCursor(new Cursor[] {mProductNameCursor, matrixCursor });
            mAdapterProductNames.changeCursor(mergeCursor);
            if(mOrderloadFinished && mCustomerNamesloadFinished){
                displayOrderDetails();
                mProductNamesloadFinished=false;
            }
        }*/
    }
    private int getCustomerIdPosition(String customerId){
        while(mCustomerNamesCursor.moveToNext()){
            if(mCustomerNamesCursor.getString(mCustomerNamesCursor.getColumnIndex(Customers._ID)).equals(customerId)){
                return mCustomerNamesCursor.getPosition();
            }
        }
        return -1;
    }
    private int getProductIdPosition(String productId){
        while(mProductNameCursor.moveToNext()){
            if(mProductNameCursor.getString(mProductNameCursor.getColumnIndex(Products._ID)).equals(productId)){
                return mProductNameCursor.getPosition();
            }
        }
        return -1;
    }
    private void displayOrderDetails() {
        mCustomerNameAutoTextCursorAdapter.setIdSearchConstarint(mOrderCursor.getString(mCustomerIdPos));
        mSelectedCustomerName=mOrderCursor.getString(mCustomerNamePos);
        mAutoTextCustomerName.setText(mOrderCursor.getString(mCustomerNamePos));
        mCustomerId=mOrderCursor.getString(mCustomerIdPos);
        mAutoTextProductName.setText(mOrderCursor.getString(mProductNamePos));
        mSelectedProductName=mOrderCursor.getString(mProductNamePos);
        mProductId=mOrderCursor.getString(mProductIdPos);
        mPreviousProductId = mOrderCursor.getString(mProductIdPos);
        mPreviousQuantity=Integer.parseInt(mOrderCursor.getString(mProductQuantityPos));
        if(mProductId!=null) {
            mProductTotalQuantity = Integer.parseInt(mOrderCursor.getString(mProductTotalQuantityPos));
            mPreviousProductTotalQuantity = Integer.parseInt(mOrderCursor.getString(mProductTotalQuantityPos));
        }
        else
            mProductTotalQuantity=0;
        String orderDate=mOrderCursor.getString(mOrderDatePos);
        int pDay=0,pMonth=0,pYear=0;
        Calendar cal_order;

        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date date = formatter.parse(orderDate);//convert to date
            cal_order = Calendar.getInstance();// get calendar instance
            cal_order.setTime(date);//set the calendar date to your date
            pYear = cal_order.get(Calendar.YEAR);
            pMonth = cal_order.get(Calendar.MONTH)+1;
            pDay=cal_order.get(Calendar.DAY_OF_MONTH);
        } catch (ParseException e) {
            cal_order=null;
        }
        if(cal_order!=null)
            mDpOrderDate.init(pYear,pMonth,pDay,null);
        mTextQuantity.setText(mOrderCursor.getString(mQunatityPos));
        mTextCashPayed.setText(mOrderCursor.getString(mCashPayedPos));
        String paymentCompleted=mOrderCursor.getString(mPaymentCompletedPos);
       /* if(paymentCompleted.equals("Y")){
            mCheckPaymentCompleted.setChecked(true);
        }
        else
            mCheckPaymentCompleted.setChecked(false);
        String delivered=mOrderCursor.getString(mDeliveredPos);
        if(delivered.equals("Y"))
            mCheckDelivered.setChecked(true);
        else
            mCheckDelivered.setChecked(false);*/
       int pos=mStatusArrayAdapter.getPosition(mOrderCursor.getString(mOrderStatusPos));
       mSpinnerOrderStatus.setSelection(pos);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId()==LOADER_ORDER) {
            if (mOrderCursor != null)
                mOrderCursor.close();
        }

    }


}
