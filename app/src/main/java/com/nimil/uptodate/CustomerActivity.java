package com.nimil.uptodate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nimil.uptodate.UptoDateProviderContract.Customers;
import com.nimil.uptodate.UptoDateProviderContract.Orders;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomerActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>, CustomDeleteDialog.DeleteDialogListener {
    public static String CUSTOMER_ID="com.nimil.uptodate.CustomerActivity.CUSTOMER_ID";
    private static final int LOADER_CUSTOMER = 0;
    public static final int DEFAULT_VALUE=-1;
    private boolean mIsNewCustomer;
    private TextView mTextCustomerName;
    private TextView mTextMobileNo;
    private TextView mTextAlternativeNo;
    private TextView mTextEmailId;

    private Cursor mCustomerCursor;
    private Uri mCustomerUri;
    private int mCustomerNamePos;
    private int mMobileNoPos;
    private int mAlternativeNoPos;
    private int mEmailIdPos;
    private int mCustomerId;
    private boolean mIsDeleteConfirmed;
    private String mPreviousCustomerName;
    private int mAddressPos;
    private TextView mTextAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        readDisplayStateValues();
        initialActions();
    }
    private void initialActions() {
        mTextCustomerName=findViewById(R.id.text_customer_name);
        mTextMobileNo=findViewById(R.id.text_mobile_no);
        mTextAlternativeNo=findViewById(R.id.text_alternative_no);
        mTextAddress = findViewById(R.id.text_address);
        mTextEmailId=findViewById(R.id.text_emailid);

        Button button_add_update=findViewById(R.id.button_add_update_customer);
        if(mIsNewCustomer)
            button_add_update.setText("Add Customer");
        else
            button_add_update.setText("Update Customer");
        button_add_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsNewCustomer){
                    //insert
                    if(validateUserInput())
                        createNewCustomer();
                    else{
                        CustomMessageBox messageBox=new CustomMessageBox("Please Fill all the Mandatory(*) Fields");
                        messageBox.show(getSupportFragmentManager(),"Input Validation");
                    }
                }
                else{
                    //Update
                    if(validateUserInput()) {
                        updateCustomerDetails();
                        String currentCustomerName=mTextCustomerName.getText().toString();
                        if(!mPreviousCustomerName.equals(currentCustomerName)){
                            updateOrderCustomerName();
                        }
                    }
                    else{
                        CustomMessageBox messageBox=new CustomMessageBox("Please Fill all the Mandatory(*) Fields");
                        messageBox.show(getSupportFragmentManager(),"Input Validation");
                    }
                }
            }
        });
        if(!mIsNewCustomer)
            getSupportLoaderManager().initLoader(LOADER_CUSTOMER,null,this).forceLoad();
    }

    private void updateOrderCustomerName() {
        AsyncTask<ContentValues,Void,Void> task= new AsyncTask<ContentValues,Void,Void>() {

            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                String selection = Orders.COLUMN_CUSTOMER_ID + " = ?";
                String[] selectionArgs = {Integer.toString(mCustomerId)};
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
        cv.put(Orders.COLUMN_ORDER_CUSTOMER_NAME,mTextCustomerName.getText().toString());
        task.execute(cv);
    }

    private boolean validateUserInput() {
        if(mTextCustomerName.getText().toString().equals(""))
            return false;
        if(mTextMobileNo.getText().toString().equals(""))
            return false;
        return true;
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mCustomerId = intent.getIntExtra(CUSTOMER_ID, DEFAULT_VALUE);
        mIsNewCustomer = (mCustomerId == DEFAULT_VALUE);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mCustomerId!=DEFAULT_VALUE){
        getMenuInflater().inflate(R.menu.comman_menu, menu);
        }
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_order_edit) {
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_customer);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(true);
            }
        }
        else if (id == R.id.action_order_delete) {
            showDeleteDialog();

        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteDialog() {
        CustomDeleteDialog customDeleteDialog=new CustomDeleteDialog("Customer will be deleted forever");
        customDeleteDialog.show(getSupportFragmentManager(),"Delete Confirmation");
    }
    @Override
    public void applyDeleteReply(boolean isConfirmed) {
        mIsDeleteConfirmed=isConfirmed;
        if(mIsDeleteConfirmed)
            deleteCustomer();
    }

    private void deleteCustomer() {
        AsyncTask<Void,Void,Void> task= new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voidValues) {

                String selection = UptoDateProviderContract.Customers._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mCustomerId)};
                int noRowsAffected=getContentResolver().delete(UptoDateProviderContract.Customers.CONTENT_URI,selection,selectionArgs);
                if(noRowsAffected==-1){
                    CustomMessageBox customMessageBox=new CustomMessageBox("Delete Failed! \n Product is refered by Orders.");
                    customMessageBox.show(getSupportFragmentManager(),"Message");
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        task.execute();
    }

    private void updateCustomerDetails() {
        AsyncTask<ContentValues,Void,Void> task= new AsyncTask<ContentValues,Void,Void>() {

            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                String selection = Customers._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mCustomerId)};
                getContentResolver().update(Customers.CONTENT_URI,rowValues,selection,selectionArgs);
                //return noteUri;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        ContentValues cv=new ContentValues();
        cv.put(Customers.COLUMN_CUSTOMER_NAME,mTextCustomerName.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String entryDate = sdf.format(new Date());
        cv.put(Customers.COLUMN_ENTRY_DATE,entryDate);
        cv.put(Customers.COLUMN_CONTACT_NUMBER,mTextMobileNo.getText().toString());
        cv.put(Customers.COLUMN_ALTERNATIVE_NUMBER,mTextAlternativeNo.getText().toString());
        cv.put(Customers.COLUMN_ADDRESS,mTextAddress.getText().toString());
        cv.put(Customers.COLUMN_EMAIL_ID,mTextEmailId.getText().toString());

        task.execute(cv);
    }

    private void createNewCustomer() {
        AsyncTask<ContentValues,Void,Uri> task= new AsyncTask<ContentValues,Void,Uri>() {

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                Uri customerUri=getContentResolver().insert(Customers.CONTENT_URI,rowValues);
                return customerUri;
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mCustomerUri=uri;
                finish();
            }

        };
        ContentValues cv=new ContentValues();
        cv.put(Customers.COLUMN_CUSTOMER_NAME,mTextCustomerName.getText().toString());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String entryDate = sdf.format(new Date());
        cv.put(Customers.COLUMN_ENTRY_DATE,entryDate);
        cv.put(Customers.COLUMN_CONTACT_NUMBER,mTextMobileNo.getText().toString());
        cv.put(Customers.COLUMN_ALTERNATIVE_NUMBER,mTextAlternativeNo.getText().toString());
        cv.put(Customers.COLUMN_ADDRESS,mTextAddress.getText().toString());
        cv.put(Customers.COLUMN_EMAIL_ID,mTextEmailId.getText().toString());
        task.execute(cv);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] productColumns={
                Customers.COLUMN_CUSTOMER_NAME,
                Customers.COLUMN_CONTACT_NUMBER,
                Customers.COLUMN_ALTERNATIVE_NUMBER,
                Customers.COLUMN_ADDRESS,
                Customers.COLUMN_EMAIL_ID
        };
        mCustomerUri= ContentUris.withAppendedId(Customers.CONTENT_URI,mCustomerId);
        return new CursorLoader(this,mCustomerUri,productColumns,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId()==LOADER_CUSTOMER) {
            mCustomerCursor = data;
            mCustomerNamePos=mCustomerCursor.getColumnIndex(Customers.COLUMN_CUSTOMER_NAME);
            mMobileNoPos=mCustomerCursor.getColumnIndex(Customers.COLUMN_CONTACT_NUMBER);
            mAlternativeNoPos=mCustomerCursor.getColumnIndex(Customers.COLUMN_ALTERNATIVE_NUMBER);
            mAddressPos = mCustomerCursor.getColumnIndex(Customers.COLUMN_ADDRESS);
            mEmailIdPos=mCustomerCursor.getColumnIndex(Customers.COLUMN_EMAIL_ID);
            mCustomerCursor.moveToNext();
            displayCustomerDetails();
        }
    }

    private void displayCustomerDetails() {
        mPreviousCustomerName=mCustomerCursor.getString(mCustomerNamePos);
        mTextCustomerName.setText(mCustomerCursor.getString(mCustomerNamePos));
        mTextMobileNo.setText(mCustomerCursor.getString(mMobileNoPos));
        mTextAlternativeNo.setText(mCustomerCursor.getString(mAlternativeNoPos));
        mTextAddress.setText(mCustomerCursor.getString(mAddressPos));
        mTextEmailId.setText(mCustomerCursor.getString(mEmailIdPos));

        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_customer);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            child.setEnabled(false);
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId()==LOADER_CUSTOMER) {
            if (mCustomerCursor != null)
                mCustomerCursor.close();
        }
    }
}
