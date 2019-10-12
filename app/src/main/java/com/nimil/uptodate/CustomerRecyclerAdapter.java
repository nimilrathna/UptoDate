package com.nimil.uptodate;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nimil.uptodate.UptoDateProviderContract.Customers;

import java.util.ArrayList;

public class CustomerRecyclerAdapter extends RecyclerView.Adapter<CustomerRecyclerAdapter.ViewHolder> {
    private final Context mContext;
    private Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mCustomerIdPos;
    private int mEntryDatePos;
    private int mCustomerNamePos;
    private int mMobileNoPos;
    private int mAlternativeNoPos;
    private int mEmailIdPos;
    private boolean mIsSelectionMode;
    private ArrayList<Integer> mSelectedIds;

    public CustomerRecyclerAdapter(Context context, Cursor cursor){
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
        mCustomerIdPos=mCursor.getColumnIndex(Customers._ID);
        mEntryDatePos=mCursor.getColumnIndex(Customers.COLUMN_ENTRY_DATE);
        mCustomerNamePos=mCursor.getColumnIndex(Customers.COLUMN_CUSTOMER_NAME);
        mMobileNoPos=mCursor.getColumnIndex(Customers.COLUMN_CONTACT_NUMBER);
        mAlternativeNoPos=mCursor.getColumnIndex(Customers.COLUMN_ALTERNATIVE_NUMBER);
        mEmailIdPos=mCursor.getColumnIndex(Customers.COLUMN_EMAIL_ID);
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
    public CustomerRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=mLayoutInflater.inflate(R.layout.content_customer_item,parent,false);
        return new CustomerRecyclerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomerRecyclerAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        holder.mId=mCursor.getInt(mCustomerIdPos);
        holder.mTextCustomerName.setText(mCursor.getString(mCustomerNamePos));
        holder.mEntryDate.setText(mCursor.getString(mEntryDatePos));
        holder.mTextMobileNo.setText(mCursor.getString(mMobileNoPos));
        holder.mTextAlternativeNo.setText(mCursor.getString(mAlternativeNoPos));
        holder.mTextEmailId.setText(mCursor.getString(mEmailIdPos));
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
        public TextView mTextCustomerName;
        public TextView mEntryDate;
        public TextView mTextMobileNo;
        public TextView mTextAlternativeNo;
        public TextView mTextEmailId;
        public final CheckBox mCheck_Selection;
        private boolean isAddedToSelectionList;
        public int mId;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextCustomerName=itemView.findViewById(R.id.text_customer_name);
            mEntryDate=itemView.findViewById(R.id.text_entry_date);
            mTextMobileNo=itemView.findViewById(R.id.text_contact_no);
            mTextAlternativeNo=itemView.findViewById(R.id.text_alternative_no);
            mTextEmailId=itemView.findViewById(R.id.text_email_id);
            mCheck_Selection=itemView.findViewById(R.id.check_selection);
            mCheck_Selection.setChecked(false);
            isAddedToSelectionList=false;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext,CustomerActivity.class);
                    intent.putExtra(CustomerActivity.CUSTOMER_ID,mId);
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
