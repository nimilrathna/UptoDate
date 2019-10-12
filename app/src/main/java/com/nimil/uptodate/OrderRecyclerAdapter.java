package com.nimil.uptodate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nimil.uptodate.UptoDateProviderContract.Orders;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Currency;

public class OrderRecyclerAdapter extends RecyclerView.Adapter<OrderRecyclerAdapter.ViewHolder>{
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mOrderIdPos;
    private int mCustomerNamePos;
    private int mProductTitlePos;
    private int mOderDatePos;
    private int mQuantityPos;
    private int mCashPayedPos;
    private int mDeliveredPos;
    private int mPaymentDonePos;
    private int mSellingPricePos;
    private int mCustomerContactNoPos;
    private boolean mIsSelectionMode;
    private ArrayList<Integer> mSelectedIds;
    private int mOrderStatusPos;
    private String mCurrency;
    private int mActualPricePos;

    public OrderRecyclerAdapter(Context context, Cursor cursor){
        mContext=context;
        mCursor=cursor;
        mLayoutInflater=LayoutInflater.from(context);
        mIsSelectionMode=false;
        mSelectedIds=new ArrayList();
        populateColumnPosition();
    }

    public boolean isSelectionMode() {
        return mIsSelectionMode;
    }
    public void setSelectionMode(boolean value){
        mIsSelectionMode=value;
    }
    public ArrayList<Integer> getSelectedIds(){
        return mSelectedIds;
    }
    private void populateColumnPosition() {
        if(mCursor==null)
            return;
        mOrderIdPos=mCursor.getColumnIndex(Orders._ID);
        mCustomerNamePos=mCursor.getColumnIndex(Orders.COLUMN_ORDER_CUSTOMER_NAME);
        mCustomerContactNoPos=mCursor.getColumnIndex(Orders.COLUMN_CONTACT_NUMBER);
        mProductTitlePos=mCursor.getColumnIndex(Orders.COLUMN_ORDER_PRODUCT_NAME);
        mOderDatePos=mCursor.getColumnIndex(Orders.COLUMN_ORDER_DATE);
        mQuantityPos=mCursor.getColumnIndex(Orders.COLUMN_ORDER_QUANTITY);
        mSellingPricePos=mCursor.getColumnIndex(Orders.COLUMN_SELLING_PRICE);
        mCashPayedPos=mCursor.getColumnIndex(Orders.COLUMN_CASH_PAYED);
        mActualPricePos = mCursor.getColumnIndex(Orders.COLUMN_ACTUAL_PRICE);
        //mDeliveredPos=mCursor.getColumnIndex(Orders.COLUMN_PRODUCT_DELIVERED);
        //mPaymentDonePos=mCursor.getColumnIndex(Orders.COLUMN_PAYMENT_COMPLETED);
        mOrderStatusPos = mCursor.getColumnIndex(Orders.COLUMN_ORDER_STATUS);
    }
    public void changeCursor(Cursor cursor){
        //if(mCursor!=null)
        //  mCursor.close();
        mCursor=cursor;
        populateColumnPosition();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=mLayoutInflater.inflate(R.layout.content_order_item,parent,false);
        updateCurrency();
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mId = mCursor.getInt(mOrderIdPos);
        String customerName=mCursor.getString(mCustomerNamePos);
        int titleLength=mContext.getResources().getInteger(R.integer.customer_name_length);
        if(customerName.length()> titleLength)
            customerName=customerName.substring(0,titleLength)+"..";
        holder.mText_CustomerName.setText(customerName);
        holder.mText_ContactNo.setText(mCursor.getString(mCustomerContactNoPos));
        holder.mText_ContactNo.setVisibility(View.GONE);
        String productTitle=mCursor.getString(mProductTitlePos);
        int productTitleLength=mContext.getResources().getInteger(R.integer.product_name_length);
        if(productTitle.length()>productTitleLength)
            productTitle=productTitle.substring(0,productTitleLength)+"..";
        holder.mText_ProductTitle.setText(productTitle);
        holder.mText_OrderDate.setText(mCursor.getString(mOderDatePos));
        holder.mText_Quantity.setText(mCursor.getString(mQuantityPos));
        String cashpaid=mCursor.getString(mCashPayedPos);

        /*if(cashpaid.equals(""))
            holder.mText_CashPayed.setText("N/A");
        else
            holder.mText_CashPayed.setText(mCurrency+ cashpaid);*/

        DecimalFormat formatDecimal = new DecimalFormat("#.##");
        formatDecimal.setRoundingMode(RoundingMode.UP);
        int orderQuantity = Integer.parseInt(mCursor.getString(mQuantityPos));
        String cashPayablestr = mCursor.getString(mSellingPricePos);
        double cashPayable=0;
        if (cashPayablestr == null || cashPayablestr.equals("")) {
            holder.mText_cash_payable.setText("N/A");
        } else {
            cashPayable = orderQuantity * Double.parseDouble(cashPayablestr);
            holder.mText_cash_payable.setText(mCurrency + formatDecimal.format(cashPayable));
        }

        String actualPrice=mCursor.getString(mActualPricePos);
        if(actualPrice==null || actualPrice.equals("") || actualPrice.equals("0") || cashPayable==0){
            holder.mText_profit.setText("N/A");
        }
        else{
            double dActualPrice=Double.parseDouble(actualPrice);
            double profit=cashPayable-(orderQuantity*dActualPrice);
            holder.mText_profit.setText(mCurrency+formatDecimal.format(profit));
        }
       /* if (mCursor.getString(mDeliveredPos).equals("Y")) {
            holder.mCheck_Delivered.setChecked(true);
        }
        else {
            holder.mCheck_Delivered.setChecked(false);
        }
        if (mCursor.getString(mPaymentDonePos).equals("Y")){
            holder.mCheck_Payment_done.setChecked(true);
        }
        else {
            holder.mCheck_Payment_done.setChecked(false);
        }*/
       String status=mCursor.getString(mOrderStatusPos);
       holder.mText_Status.setText(status);
       if(status.equals(mContext.getString(R.string.paid)) || status.equals(mContext.getString(R.string.delivered))){
           holder.mText_Status.setTextColor(Color.parseColor("#008000"));
       }
       else{
           holder.mText_Status.setTextColor(Color.parseColor("#FF0000"));
       }

        if(mIsSelectionMode)
            holder.mCheck_Selection.setVisibility(View.VISIBLE);
        else{
            holder.mCheck_Selection.setChecked(false);
            holder.mCheck_Selection.setVisibility(View.GONE);
        }
    }
    private void updateCurrency() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(mContext);
        String countryCode = pref.getString("currency","INR");
        //Locale locale=new Locale(mCurrency);
        mCurrency=Currency.getInstance(countryCode).getSymbol();

    }

    @Override
    public int getItemCount() {
        return  mCursor==null ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView mText_CustomerName;
        public final TextView mText_ContactNo;
        public final TextView mText_ProductTitle;
        public final TextView mText_OrderDate;
        public final TextView mText_Quantity;
        public final TextView mText_cash_payable;
        //public final TextView mText_CashPayed;
        //public final CheckBox mCheck_Delivered;
        //public final CheckBox mCheck_Payment_done;
        public final TextView mText_Status;
        public final CheckBox mCheck_Selection;
        private boolean isAddedToSelectionList;
        public int mId;
        private final TextView mText_profit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mText_CustomerName=itemView.findViewById(R.id.text_customer_name);
            mText_ContactNo=itemView.findViewById(R.id.text_contact_no);
            mText_ProductTitle=itemView.findViewById(R.id.text_product_name);
            mText_OrderDate=itemView.findViewById(R.id.text_order_date);
            mText_Quantity=itemView.findViewById(R.id.text_quantity);
            mText_cash_payable=itemView.findViewById(R.id.text_cashpayable);
            mText_profit = itemView.findViewById(R.id.text_profit);
            //mText_CashPayed=itemView.findViewById(R.id.text_profit);
            //mCheck_Delivered=itemView.findViewById(R.id.check_delivered);
            //mCheck_Payment_done=itemView.findViewById(R.id.check_payment_done);
            mText_Status=itemView.findViewById(R.id.text_order_status);
            mCheck_Selection=itemView.findViewById(R.id.check_selection);
            mCheck_Selection.setChecked(false);
            isAddedToSelectionList=false;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mIsSelectionMode){
                        if(!mCheck_Selection.isChecked()){
                            mCheck_Selection.setChecked(true);
                            mSelectedIds.add(new Integer(mId));
                            isAddedToSelectionList=true;
                        }
                        else {
                            mCheck_Selection.setChecked(false);
                            if(isAddedToSelectionList) {
                                mSelectedIds.remove(new Integer((mId)));
                                isAddedToSelectionList=false;
                            }
                        }
                    }else {
                        Intent intent = new Intent(mContext, OrderViewActivity.class);
                        intent.putExtra(OrderViewActivity.ORDER_ID, mId);
                        mContext.startActivity(intent);
                        isAddedToSelectionList = true;
                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if(!isSelectionMode()) {
                        mSelectedIds.clear();
                        mCheck_Selection.setVisibility(View.VISIBLE);
                        mCheck_Selection.setChecked(true);
                        mSelectedIds.add(new Integer(mId));
                        isAddedToSelectionList=true;
                    }

                    mIsSelectionMode = true;
                    notifyDataSetChanged();

                    return true;
                }
            });
            mCheck_Selection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mCheck_Selection.isChecked()){
                        mSelectedIds.add(new Integer(mId));
                        isAddedToSelectionList=true;
                    }
                        else {
                        if(isAddedToSelectionList) {
                            mSelectedIds.remove(new Integer((mId)));
                            isAddedToSelectionList=false;
                        }
                    }
                }
            });
        }
    }
}
