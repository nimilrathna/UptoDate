package com.nimil.uptodate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.nimil.uptodate.UptoDateProviderContract.Orders;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Currency;

public class OrderViewActivity extends AppCompatActivity
implements CustomDeleteDialog.DeleteDialogListener {
    public static final String ORDER_ID = "com.nimil.uptodate.orderview.ORDER_ID";
    public static final int DEFAULT_VALUE = -1;
    private Cursor mOrderCursor;
    private int mOrderId;
    private TextView mTextOrderDate;;
    private TextView mTextCustomerName;
    private TextView mTextProductName;
    private TextView mTextQuantity;
    private TextView mTextCashPayable;
    private TextView mTextCashPaid;
    private TextView mTextStatus;
    private TextView mTextOrderProfit;
    private TextView mTextLabelCustomerName;
    private TextView mTextLabelProductName;
    private int mCustomerNamePos;
    private int mProductNamePos;
    private int mOrderDatePos;
    private int mQuantityPos;
    private int mCashPaidPos;
    private int mOrderStatusPos;
    private int mSellingPricePos;
    private int mActualPricePos;
    String mCurrency;
    private int mCustomerMobilePos;
    private TextView mTextCustomerMobile;
    private int mCustomerEmailPos;
    private TextView mTextEmailId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        readDisplayStateValues();
        initialActions();
    }

    @Override
    protected void onResume() {
        if(mOrderId!=DEFAULT_VALUE){
            loadOrderDetails();
        }
        super.onResume();
    }

    private void initialActions() {
        updateCurrency();
        mTextOrderDate=findViewById(R.id.text_orderdate);
        mTextCustomerName=findViewById(R.id.text_customername);
        mTextCustomerMobile = findViewById(R.id.text_customerphone);
        mTextEmailId = findViewById(R.id.text_customeremail);
        mTextProductName=findViewById(R.id.text_productname);
        mTextQuantity=findViewById(R.id.text_quantity);
        mTextCashPayable=findViewById(R.id.text_cashpayable);
        mTextCashPaid=findViewById(R.id.text_cashpaid);
        mTextStatus=findViewById(R.id.text_orderstatus);
        mTextOrderProfit=findViewById(R.id.text_profit);
        mTextLabelCustomerName=findViewById(R.id.text_label_customername);
        mTextLabelProductName=findViewById(R.id.text_label_productname);

    }
    private void updateCurrency() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        String countryCode = pref.getString("currency","INR");
        mCurrency= Currency.getInstance(countryCode).getSymbol();

    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mOrderId = intent.getIntExtra(ORDER_ID, DEFAULT_VALUE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(mOrderId!=DEFAULT_VALUE) {
            getMenuInflater().inflate(R.menu.order_menu, menu);
        }
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_edit) {
            Intent intent = new Intent(this, OrderActivity.class);
            intent.putExtra(OrderActivity.ORDER_ID, mOrderId);
            this.startActivity(intent);
        }
        else if (id == R.id.action_delete) {
            showDeleteDialog();

        }
        else if(id==R.id.action_whatsapp){
            sendWhatsappMessage();
        }
        else if(id==R.id.action_email){
            sendEmail();
        }
        return super.onOptionsItemSelected(item);
    }


    private void sendEmail() {
        try {
            String msg = createMessage();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String organisation_name = pref.getString("signature", "");
            String subject = organisation_name + " - Order Details";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("message/plain");
            String email=mTextEmailId.getText().toString();
            email=email.equals("N/A")?"":email;
            String mailto = "mailto:"+email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(Html.fromHtml(msg).toString());

            intent.setData(Uri.parse(mailto));
            /*intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT,Html.fromHtml(msg));*/
            startActivity(intent);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            //Do Nothing
        }

    }


    private String createMessage() {
        String message= "<p>"+String.format("%30s %2s %68s","Order Date ",":",mTextOrderDate.getText().toString())+"</p>"
                +"<p>"+String.format("%30s %2s %68s%n","CustomerName ",":", mTextCustomerName.getText().toString())+"</p>"
                +"<p>"+String.format("%30s %2s %68s%n","Product Name ",":", mTextProductName.getText().toString())+"</p>"
                +"<p>"+String.format("%30s %2s %68s%n","Units ",":",mTextQuantity.getText().toString())+"</p>"
                +"<p>"+String.format("%30s %2s %68s%n","Price ",":",mOrderCursor.getString(mSellingPricePos))+"</p>"
                +"<p>"+String.format("%30s %2s %68s%n","Total Amount",":",mTextCashPayable.getText().toString())+"</p>";

        return message;
    }

    private void sendWhatsappMessage() {
        try {
            String msg=createMessage();
            SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
            String organisation_name=pref.getString("signature","");
            String sendMessage="<h2>"+organisation_name+"</h2>" +msg;

            String toNumber = mTextCustomerMobile.getText().toString();
            //if(toNumber.charAt(0)=='+'||toNumber.charAt(0)=='0')
              //  toNumber=toNumber.substring(1,toNumber.length()-1);// Replace with mobile phone number without +Sign or leading zeros, but with country code
            //Suppose your country is India and your phone number is “xxxxxxxxxx”, then you need to send “91xxxxxxxxxx”.
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+Html.fromHtml(sendMessage)));
            startActivity(intent);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void showDeleteDialog() {
        CustomDeleteDialog customDeleteDialog=new CustomDeleteDialog("Order will be deleted forever");
        customDeleteDialog.show(getSupportFragmentManager(),"Delete Confirmation");
    }
    @Override
    public void applyDeleteReply(boolean isConfirmed) {
        if(isConfirmed)
            deleteOrder();
    }

    private void deleteOrder() {
        AsyncTask<Integer,Void,Void> task= new AsyncTask<Integer,Void,Void>() {

            @Override
            protected Void doInBackground(Integer... values) {
                int orderId=values[0];
                String selection = Orders._ID + " = ?";
                String[] selectionArgs = {Integer.toString(orderId)};
                getContentResolver().delete(Orders.CONTENT_URI,selection,selectionArgs);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                finish();
            }

        };
        task.execute(mOrderId);
    }
    private void loadOrderDetails() {

        AsyncTask<Integer, Void, Cursor> task = new AsyncTask<Integer, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(Integer... ints) {
                int orderId = ints[0];
                String[] orderColumns = {
                        Orders.COLUMN_CUSTOMER_ID,
                        Orders.COLUMN_PRODUCT_ID,
                        Orders.COLUMN_ORDER_CUSTOMER_NAME,
                        Orders.COLUMN_CONTACT_NUMBER,
                        Orders.COLUMN_EMAIL_ID,
                        Orders.COLUMN_ORDER_PRODUCT_NAME,
                        Orders.COLUMN_ORDER_DATE,
                        Orders.COLUMN_ORDER_QUANTITY,
                        Orders.COLUMN_ACTUAL_PRICE,
                        Orders.COLUMN_SELLING_PRICE,
                        Orders.COLUMN_CASH_PAYED,
                        Orders.COLUMN_ORDER_STATUS

                };
                Uri orderUri = ContentUris.withAppendedId(Orders.CONTENT_EXTENDED_URI, orderId);
                Cursor cursor=getContentResolver().query(orderUri,
                        orderColumns,null,null,null);
                return cursor;
            }

            @Override
            protected void onPostExecute(Cursor cursor) {
                mOrderCursor = cursor;
                mCustomerNamePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_CUSTOMER_NAME);
                mCustomerMobilePos = mOrderCursor.getColumnIndex(Orders.COLUMN_CONTACT_NUMBER);
                mCustomerEmailPos = mOrderCursor.getColumnIndex(Orders.COLUMN_EMAIL_ID);
                mProductNamePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_PRODUCT_NAME);
                mOrderDatePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_DATE);
                mQuantityPos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_QUANTITY);
                mActualPricePos=mOrderCursor.getColumnIndex(Orders.COLUMN_ACTUAL_PRICE);
                mSellingPricePos=mOrderCursor.getColumnIndex(Orders.COLUMN_SELLING_PRICE);
                mCashPaidPos=mOrderCursor.getColumnIndex(Orders.COLUMN_CASH_PAYED);
                mOrderStatusPos=mOrderCursor.getColumnIndex(Orders.COLUMN_ORDER_STATUS);
                mOrderCursor.moveToNext();
                displayOrderDetails();
            }

        };
        task.execute(mOrderId);
    }

    private void displayOrderDetails() {
        mTextOrderDate.setText(mOrderCursor.getString(mOrderDatePos));
        mTextCustomerName.setText(mOrderCursor.getString(mCustomerNamePos));
        mTextCustomerMobile.setText(mOrderCursor.getString(mCustomerMobilePos));
        String emailId=mOrderCursor.getString(mCustomerEmailPos);
        if(emailId==null||emailId.equals(""))
            emailId="N/A";
        mTextEmailId.setText(emailId);
        mTextProductName.setText(mOrderCursor.getString(mProductNamePos));
        mTextQuantity.setText(mOrderCursor.getString(mQuantityPos));
        mTextStatus.setText(mOrderCursor.getString(mOrderStatusPos));
        String cashPaid=mOrderCursor.getString(mCashPaidPos);
        if(cashPaid==null || cashPaid.equals(""))
            cashPaid="N/A";
        mTextCashPaid.setText(cashPaid);
        DecimalFormat formatDecimal = new DecimalFormat("#.##");
        formatDecimal.setRoundingMode(RoundingMode.UP);
        int orderQuantity = Integer.parseInt(mOrderCursor.getString(mQuantityPos));
        String cashPayablestr = mOrderCursor.getString(mSellingPricePos);
        double cashPayable=0;
        if (cashPayablestr == null || cashPayablestr.equals("")) {
            mTextCashPayable.setText("N/A");
        } else {
            cashPayable = orderQuantity * Double.parseDouble(cashPayablestr);
            mTextCashPayable.setText(mCurrency + formatDecimal.format(cashPayable));
        }

        String actualPrice=mOrderCursor.getString(mActualPricePos);
        if(actualPrice==null || actualPrice.equals("") || actualPrice.equals("0") || cashPayable==0){
            mTextOrderProfit.setText("N/A");
        }
        else{
            double dActualPrice=Double.parseDouble(actualPrice);
            double profit=cashPayable-(orderQuantity*dActualPrice);
            mTextOrderProfit.setText(mCurrency+formatDecimal.format(profit));
        }
    }
}
