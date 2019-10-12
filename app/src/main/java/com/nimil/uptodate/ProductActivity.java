package com.nimil.uptodate;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.nimil.uptodate.UptoDateProviderContract.Orders;
import com.nimil.uptodate.UptoDateProviderContract.Products;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;

public class ProductActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, CustomDeleteDialog.DeleteDialogListener {
    private static final int LOADER_PRODUCT = 0;
    public static String PRODUCT_ID="com.nimil.uptodate.ProductActivity.PRODUCT_ID";
    public static final int DEFAULT_VALUE=-1;
    private boolean mIsNewProduct;
    private TextView mTextProductTitle;
    private DatePicker mDpPurchaseDate;
    private TextView mTextActualPrice;
    private TextView mTextSellingPrice;
    private TextView mTextQuantity;
    private Cursor mProductCursor;
    private Uri mProductUri;
    private int mProductTitlePos;
    private int mPurchaseDatePos;
    private int mActualPricePos;
    private int mSellingPricePos;
    private int mQuantityPos;
    private boolean mIsDeleteConfirmed;
    private String mPreviousProductName;

private int mProduct_id;
    private String mCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);
        readDisplayStateValues();
        initialActions();
    }
    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mProduct_id = intent.getIntExtra(PRODUCT_ID, DEFAULT_VALUE);
        mIsNewProduct = (mProduct_id == DEFAULT_VALUE);

    }

    private void updateCurrency() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        String countryCode = pref.getString("currency","INR");
        mCurrency = Currency.getInstance(countryCode).getSymbol();
    }

    private void initialActions() {
        updateCurrency();
        mTextProductTitle=findViewById(R.id.text_product_title);
        mDpPurchaseDate=findViewById(R.id.dp_purchased_date);
        TextView textActualPriceLabel=findViewById(R.id.text_actualprice_label);
        textActualPriceLabel.setText("Actual Price in "+mCurrency);
        mTextActualPrice=findViewById(R.id.text_actual_price);
        mTextActualPrice.setHint("Actual Price in "+mCurrency);
        TextView textSellingPriceLabel=findViewById(R.id.text_sellingprice_label);
        textSellingPriceLabel.setText("Selling Price in "+mCurrency+ " *");
        mTextSellingPrice=findViewById(R.id.text_selling_price);
        mTextSellingPrice.setHint("Selling Price in "+mCurrency);
        mTextQuantity=findViewById(R.id.text_quantity);
        Button button_add_update=findViewById(R.id.button_add_update);
        if(mIsNewProduct)
            button_add_update.setText("Add Product");
        else
            button_add_update.setText("Update Product");
        button_add_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mIsNewProduct){
                    //insert
                    if(validateUserInput())
                        createNewProduct();
                    else{
                        CustomMessageBox messageBox=new CustomMessageBox("Please Fill all the Mandatory(*) Fields");
                        messageBox.show(getSupportFragmentManager(),"Input Validation");
                    }
                }
                else{
                    //Update
                    if(validateUserInput()) {
                        updateProduct();
                        String currentProductName=mTextProductTitle.getText().toString();
                        if(!mPreviousProductName.equals(currentProductName)){
                            updateOrdersProductName();
                        }
                    }
                    else{
                        CustomMessageBox messageBox=new CustomMessageBox("Please Fill all the Mandatory(*) Fields");
                        messageBox.show(getSupportFragmentManager(),"Input Validation");
                    }
                }
            }
        });
        if(!mIsNewProduct)
            getSupportLoaderManager().initLoader(LOADER_PRODUCT,null,this).forceLoad();
    }

    private void updateOrdersProductName() {
        AsyncTask<ContentValues,Void,Void> task= new AsyncTask<ContentValues,Void,Void>() {

            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                String selection = Orders.COLUMN_PRODUCT_ID + " = ?";
                String[] selectionArgs = {Integer.toString(mProduct_id)};
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
        cv.put(Orders.COLUMN_ORDER_PRODUCT_NAME,mTextProductTitle.getText().toString());
        task.execute(cv);
    }

    private boolean validateUserInput() {
        if(mTextProductTitle.getText().toString().equals(""))
            return false;
        if(mTextSellingPrice.getText().toString().equals("")){
            return false;
        }
        if(mTextQuantity.getText().toString().equals(""))
            return false;
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mProduct_id!=DEFAULT_VALUE) {
            getMenuInflater().inflate(R.menu.comman_menu, menu);
        }
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_order_edit) {
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_product);
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
        CustomDeleteDialog customDeleteDialog=new CustomDeleteDialog("Order will be deleted forever");
        customDeleteDialog.show(getSupportFragmentManager(),"Delete Confirmation");
    }
    @Override
    public void applyDeleteReply(boolean isConfirmed) {
        mIsDeleteConfirmed=isConfirmed;
        if(mIsDeleteConfirmed)
            deleteProduct();
    }

    private void deleteProduct() {
        AsyncTask<Void,Void,Void> task= new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voidValues) {

                String selection = UptoDateProviderContract.Products._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mProduct_id)};
                getContentResolver().delete(UptoDateProviderContract.Products.CONTENT_URI,selection,selectionArgs);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        task.execute();
    }

    private void updateProduct() {
        AsyncTask<ContentValues,Void,Void> task= new AsyncTask<ContentValues,Void,Void>() {

            @Override
            protected Void doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                String selection = Products._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mProduct_id)};
                getContentResolver().update(Products.CONTENT_URI,rowValues,selection,selectionArgs);
                //return noteUri;
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        ContentValues cv=new ContentValues();
        cv.put(Products.COLUMN_PRODUCT_NAME,mTextProductTitle.getText().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String purchaseDate=sdf.format(getDateFromDatePicker(mDpPurchaseDate));
        cv.put(Products.COLUMN_PURCHASE_DATE,purchaseDate);

        float actutal_price=mTextActualPrice.getText().toString().equals("")?
                0:
                Float.parseFloat(mTextActualPrice.getText().toString());
        float selling_price=mTextSellingPrice.getText().toString().equals("")?
                0:
                Float.parseFloat(mTextSellingPrice.getText().toString());
        int quantity=mTextQuantity.getText().toString().equals("")?
                0:
                Integer.parseInt(mTextQuantity.getText().toString());
        cv.put(Products.COLUMN_ACTUAL_PRICE,actutal_price);
        cv.put(Products.COLUMN_SELLING_PRICE,selling_price);
        cv.put(Products.COLUMN_QUANTITY,quantity);
        task.execute(cv);
    }

    private void createNewProduct() {
        AsyncTask<ContentValues,Void,Uri> task= new AsyncTask<ContentValues,Void,Uri>() {

            @Override
            protected Uri doInBackground(ContentValues... contentValues) {
                ContentValues rowValues=contentValues[0];
                Uri noteUri=getContentResolver().insert(Products.CONTENT_URI,rowValues);
                return noteUri;
            }
            @Override
            protected void onPostExecute(Uri uri) {
                mProductUri=uri;
                if(mProductUri.getLastPathSegment().equals("-1"))
                {
                    CustomMessageBox messageBox=new CustomMessageBox("Product Name Already Exist");
                    messageBox.show(getSupportFragmentManager(),"Input Validation");

                }
                else{
                finish();}
                //Snackbar.make(m,R.string.product_added,Snackbar.LENGTH_SHORT).show();
            }
        };
        ContentValues cv=new ContentValues();
        cv.put(Products.COLUMN_PRODUCT_NAME,mTextProductTitle.getText().toString());

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String entryDate = sdf.format(new Date());
        String purchaseDate=sdf.format(getDateFromDatePicker(mDpPurchaseDate));

        cv.put(Products.COLUMN_PURCHASE_DATE,purchaseDate);
        cv.put(Products.COLUMN_ENTRY_DATE,entryDate);

        float actutal_price=mTextActualPrice.getText().toString().equals("")?
                0:
                Float.parseFloat(mTextActualPrice.getText().toString());
        float selling_price=mTextSellingPrice.getText().toString().equals("")?
                0:
                Float.parseFloat(mTextSellingPrice.getText().toString());
        int quantity=mTextQuantity.getText().toString().equals("")?
                    0:
                    Integer.parseInt(mTextQuantity.getText().toString());
        cv.put(Products.COLUMN_ACTUAL_PRICE,actutal_price);
        cv.put(Products.COLUMN_SELLING_PRICE,selling_price);
        cv.put(Products.COLUMN_QUANTITY,quantity);
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
        String[] productColumns={
                Products.COLUMN_PRODUCT_NAME,
                Products.COLUMN_PURCHASE_DATE,
                Products.COLUMN_QUANTITY,
                Products.COLUMN_ACTUAL_PRICE,
                Products.COLUMN_SELLING_PRICE
        };
        mProductUri= ContentUris.withAppendedId(Products.CONTENT_URI,mProduct_id);
        return new CursorLoader(this,mProductUri,productColumns,null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId()==LOADER_PRODUCT) {
            mProductCursor = data;
            mProductTitlePos = mProductCursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME);
            mPurchaseDatePos = mProductCursor.getColumnIndex(Products.COLUMN_PURCHASE_DATE);
            mActualPricePos=mProductCursor.getColumnIndex(Products.COLUMN_ACTUAL_PRICE);
            mSellingPricePos=mProductCursor.getColumnIndex(Products.COLUMN_SELLING_PRICE);
            mQuantityPos=mProductCursor.getColumnIndex(Products.COLUMN_QUANTITY);
            mProductCursor.moveToNext();
            displayProductDetails();
        }
    }
    private void displayProductDetails() {
        mPreviousProductName=mProductCursor.getString(mProductTitlePos);
        mTextProductTitle.setText(mProductCursor.getString(mProductTitlePos));
        //mDpPurchaseDate.set
        String purchaseDate=mProductCursor.getString(mPurchaseDatePos);
        int pDay=0,pMonth=0,pYear=0;
        Date date_purchase;
        try {
            date_purchase =new SimpleDateFormat("dd/mm/yyyy").parse(purchaseDate);
            pDay=date_purchase.getDay();
            pMonth=date_purchase.getMonth();
            pYear=date_purchase.getYear();
        } catch (ParseException e) {
            date_purchase=null;
        }
        if(date_purchase!=null)
            mDpPurchaseDate.updateDate(pYear,pMonth,pDay);
        mTextActualPrice.setText(mProductCursor.getString(mActualPricePos));
        mTextSellingPrice.setText(mProductCursor.getString(mSellingPricePos));
        mTextQuantity.setText(mProductCursor.getString(mQuantityPos));
            ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.layout_product);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(false);
            }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId()==LOADER_PRODUCT) {
            if (mProductCursor != null)
                mProductCursor.close();
        }
    }
}
