package com.nimil.uptodate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nimil.uptodate.UptoDateProviderContract.Products;

import java.util.ArrayList;
import java.util.Currency;

public class ProductRecyclerAdapter extends RecyclerView.Adapter<ProductRecyclerAdapter.ViewHolder>{
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mProductIdPos;
    private int mProductTitlePos;
    private int mPurchaseDatePos;
    private int mQuantityPos;
    private int mActualPricePos;
    private int mSellingPricePos;
    private boolean mIsSelectionMode;
    private ArrayList<Integer> mSelectedIds;
    private String mCurrency;

    public ProductRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater=LayoutInflater.from(mContext);
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
        mProductIdPos=mCursor.getColumnIndex(Products._ID);
        mProductTitlePos=mCursor.getColumnIndex(Products.COLUMN_PRODUCT_NAME);
        mPurchaseDatePos=mCursor.getColumnIndex(Products.COLUMN_PURCHASE_DATE);
        mQuantityPos=mCursor.getColumnIndex(Products.COLUMN_QUANTITY);
        mActualPricePos=mCursor.getColumnIndex(Products.COLUMN_ACTUAL_PRICE);
        mSellingPricePos=mCursor.getColumnIndex(Products.COLUMN_SELLING_PRICE);
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
        View itemView=mLayoutInflater.inflate(R.layout.content_product_item,parent,false);
        updateCurrency();
        return new ViewHolder(itemView);
    }
    private void updateCurrency() {
        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(mContext);
        String countryCode = pref.getString("currency","INR");
        //Locale locale=new Locale(mCurrency);
        mCurrency= Currency.getInstance(countryCode).getSymbol();

    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mId=mCursor.getInt(mProductIdPos);
        holder.mText_product_title.setText(mCursor.getString(mProductTitlePos));
        holder.mText_purchase_date.setText(mCursor.getString(mPurchaseDatePos));
        holder.mText_quantity.setText(mCursor.getString(mQuantityPos));
        holder.mText_selling_price.setText(mCurrency+mCursor.getString(mSellingPricePos));
        float actual_price=Float.valueOf(mCursor.getString(mActualPricePos));
        float selling_price=Float.valueOf(mCursor.getString(mSellingPricePos));
        float profit=selling_price-actual_price;
        if(actual_price<=0) {
            holder.mText_actual_price.setText("N/A");
            holder.mText_profit.setText("N/A");
        }
        else{
            holder.mText_actual_price.setText(mCurrency+mCursor.getString(mActualPricePos));
            holder.mText_profit.setText(mCurrency+String.valueOf(profit));
        }
        if(mIsSelectionMode)
            holder.mCheck_Selection.setVisibility(View.VISIBLE);
        else
            holder.mCheck_Selection.setVisibility(View.GONE);

    }

    @Override
    public int getItemCount() {
        return  mCursor==null ? 0 : mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public final TextView mText_product_title;
        public final TextView mText_purchase_date;
        public final TextView mText_quantity;
        public final TextView mText_actual_price;
        public final TextView mText_selling_price;
        public final TextView mText_profit;
        public final CheckBox mCheck_Selection;
        private boolean isAddedToSelectionList;
        public int mId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mText_product_title=itemView.findViewById(R.id.text_product_title);
            mText_purchase_date=itemView.findViewById(R.id.text_purchase_date);
            mText_quantity=itemView.findViewById(R.id.text_quantity);
            mText_actual_price=itemView.findViewById(R.id.text_actual_price);
            mText_selling_price=itemView.findViewById(R.id.text_selling_price);
            mText_profit=itemView.findViewById(R.id.text_profit);
            mCheck_Selection=itemView.findViewById(R.id.check_selection);
            mCheck_Selection.setChecked(false);
            isAddedToSelectionList=false;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext,ProductActivity.class);
                    intent.putExtra(ProductActivity.PRODUCT_ID,mId);
                    mContext.startActivity(intent);
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
                            mSelectedIds.remove(new Integer(mId));
                            isAddedToSelectionList=false;
                        }
                    }
                }
            });
        }
    }
}
