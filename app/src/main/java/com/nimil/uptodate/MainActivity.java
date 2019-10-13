package com.nimil.uptodate;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.text.Html;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.nimil.uptodate.UptoDateDatabaseContract.OrderInfoEntry;
import com.nimil.uptodate.UptoDateProviderContract.Customers;
import com.nimil.uptodate.UptoDateProviderContract.Orders;
import com.nimil.uptodate.UptoDateProviderContract.Products;

import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Menu;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener, CustomDeleteDialog.DeleteDialogListener {
    public static final int LOADER_PRODUCTS = 1;
    public static final int LOADER_CUSTOMERS = 2;
    public static final int LOADER_ORDERS = 3;

    public static int mDisplayContent;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mLinearLayoutManager;
    private ProductRecyclerAdapter mProductRecyclerAdapter;
    private CustomerRecyclerAdapter mCustomerRecyclerAdapter;
    private OrderRecyclerAdapter mOrderRecyclerAdapter;
    private String[] mOrderColumns;
    private String[] mProductColumns;
    private String[] mCustomerColumns;

    private static final int ORDERS=0;
    private static final int PRODUCTS=1;
    private static final int CUSTOMERS=2;
    private static final int ID_NEW=-1;
    private MenuItem mMenuItemSortOrderDate;
    private MenuItem mMenuItemSortPurchaseDate;
    private MenuItem mMenuItemSortEntryDate;
    private MenuItem mMenuItemSortCustomerName;
    private MenuItem mMenuItemSortProductName;
    private boolean  mIsDeleteConfirmed;
    private ArrayList<Integer> mDeletionListIds;

    private boolean mIsMenuItemCheckDeliverd;
    private boolean mIsMenuItemCheckPayed;
    private boolean mIsMenuItemAll=true;
    private MenuItem mActionDelivered;
    private MenuItem mActionPayed;
    private MenuItem mMenuItemViewOrders;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityByDisplayContent();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        initialiseDisplayContent();
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateNavHeader();
        getSupportLoaderManager().restartLoader(LOADER_ORDERS,null,this);
        getSupportLoaderManager().restartLoader(LOADER_PRODUCTS,null,this);
        getSupportLoaderManager().restartLoader(LOADER_CUSTOMERS,null,this);
    }
    private void updateNavHeader() {
        NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
        View headerView=navigationView.getHeaderView(0);
        TextView text_user_name=(TextView)headerView.findViewById(R.id.text_name);
        //TextView text_user_email=(TextView)headerView.findViewById(R.id.text_user_email);

        SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
        String user_name=pref.getString("signature","");
        //String user_email=pref.getString("email","");

        text_user_name.setText(user_name);
        //text_user_email.setText(user_email);
    }

    private void startActivityByDisplayContent() {
        Intent intent;
        switch(mDisplayContent){
            case ORDERS:
                intent=new Intent(MainActivity.this,OrderActivity.class);
                intent.putExtra(OrderActivity.ORDER_ID,ID_NEW);
                startActivity(intent);
                break;
            case PRODUCTS:
                intent=new Intent(MainActivity.this,ProductActivity.class);
                intent.putExtra(ProductActivity.PRODUCT_ID,ID_NEW);
                startActivity(intent);
                break;
            case CUSTOMERS:
                intent=new Intent(MainActivity.this,CustomerActivity.class);
                intent.putExtra(CustomerActivity.CUSTOMER_ID,ID_NEW);
                startActivity(intent);
                break;
        }

    }

    private void initialiseDisplayContent() {
        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerItems = (RecyclerView) findViewById(R.id.recycler_item);
        mRecyclerItems.setLayoutManager(mLinearLayoutManager);
        mProductRecyclerAdapter=new ProductRecyclerAdapter(this,null);
        mOrderRecyclerAdapter=new OrderRecyclerAdapter(this,null);
        mCustomerRecyclerAdapter=new CustomerRecyclerAdapter(this,null);

    }

    private void queryBySearchKey(String selectionColumn,String searchKey,String orderby) {

        AsyncTask<String,Void,Cursor> task= new AsyncTask<String, Void, Cursor>() {
            @Override
            protected Cursor doInBackground(String... strings) {
                String selectionColumn=strings[0];
                String searchKey=strings[1];
                String orderBy=strings[2];
                Cursor cursor=null;
                switch(mDisplayContent){
                    case ORDERS:
                        cursor=loadOrdersDetails(selectionColumn,searchKey,orderBy);
                        break;
                    case PRODUCTS:
                        cursor=loadProductDetails(searchKey,orderBy);
                        break;
                    case CUSTOMERS:
                        cursor=loadCustomerDetails(searchKey,orderBy);
                        break;
                }
                return cursor;
            }
            @Override
            protected void onPostExecute(Cursor cursor) {

                switch(mDisplayContent) {
                    case ORDERS:
                        mOrderRecyclerAdapter.setSelectionMode(false);
                        mOrderRecyclerAdapter.changeCursor(cursor);
                        break;
                    case PRODUCTS:
                        mProductRecyclerAdapter.setSelectionMode(false);
                        mProductRecyclerAdapter.changeCursor(cursor);
                        break;
                    case CUSTOMERS:
                        mCustomerRecyclerAdapter.setSelectionMode(false);
                        mCustomerRecyclerAdapter.changeCursor(cursor);
                        break;
                }
            }

        };
        task.execute(selectionColumn,searchKey,orderby);

    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    private Cursor loadOrdersDetails(String selectionColumn,String searchKey, String orderBy) {
        String selections=null;
        String[] selectionArgs=null;
        if(orderBy==null)
            orderBy=Orders.COLUMN_ORDER_DATE;
        if(selectionColumn!=null){
            selections=selectionColumn;
            selectionArgs=searchKey.split(",");

        }
        else {
            if (searchKey != null && !searchKey.equals("")) {
                selections = Orders.COLUMN_ORDER_PRODUCT_NAME + " LIKE ? " + " OR " +
                        Orders.COLUMN_ORDER_CUSTOMER_NAME + " LIKE ? ";
                selectionArgs = new String[]{
                        "%" + searchKey + "%",
                        "%" + searchKey + "%"
                };
            }
        }
        Cursor cursor=getContentResolver().query(Orders.CONTENT_EXTENDED_URI,mOrderColumns,selections,selectionArgs,orderBy);
        return cursor;
    }
    private Cursor loadProductDetails(String searchKey, String orderBy) {
        String selections=null;
        String[] selectionArgs=null;
        if(orderBy==null)
            orderBy=Products.COLUMN_ENTRY_DATE;
        if(searchKey!=null && !searchKey.equals("")) {
            selections = Products.COLUMN_PRODUCT_NAME + " LIKE ? ";
            selectionArgs = new String[]{
                    "%" + searchKey + "%"
            };
        }
        Cursor cursor=getContentResolver().query(Products.CONTENT_URI,mProductColumns,selections,selectionArgs,orderBy);
        return cursor;
    }
    private Cursor loadCustomerDetails(String searchKey, String orderBy) {
        String selections=null;
        String[] selectionArgs=null;
        if(orderBy==null)
            orderBy=Customers.COLUMN_ENTRY_DATE;
        if(searchKey!=null && !searchKey.equals("")) {
            selections = Customers.COLUMN_CUSTOMER_NAME + " LIKE ? ";
            selectionArgs = new String[]{
                    "%" + searchKey + "%"
            };
        }
        Cursor cursor=getContentResolver().query(Customers.CONTENT_URI,mCustomerColumns,selections,selectionArgs,orderBy);
        return cursor;
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else if(mOrderRecyclerAdapter.isSelectionMode()) {
            mOrderRecyclerAdapter.setSelectionMode(false);
            mOrderRecyclerAdapter.notifyDataSetChanged();
        }
        else if(mCustomerRecyclerAdapter.isSelectionMode()) {
            mCustomerRecyclerAdapter.setSelectionMode(false);
            mCustomerRecyclerAdapter.notifyDataSetChanged();
        }
        else if(mProductRecyclerAdapter.isSelectionMode()) {
            mProductRecyclerAdapter.setSelectionMode(false);
            mProductRecyclerAdapter.notifyDataSetChanged();
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        mMenuItemViewOrders = menu.findItem(R.id.action_view_orders);
        mActionDelivered = mMenuItemViewOrders.getSubMenu().findItem(R.id.action_delivered);
        mActionPayed = mMenuItemViewOrders.getSubMenu().findItem(R.id.action_payed);

        mMenuItemSortOrderDate = menu.findItem(R.id.action_sort_orderdate);
        mMenuItemSortPurchaseDate = menu.findItem(R.id.action_sort_purchasedate);
        mMenuItemSortEntryDate = menu.findItem(R.id.action_sort_entrydate);
        mMenuItemSortCustomerName = menu.findItem(R.id.action_sort_customername);
        mMenuItemSortProductName = menu.findItem(R.id.action_sort_productname);
        mMenuItemViewOrders.setVisible(true);
        mMenuItemSortOrderDate.setVisible(true);
        mMenuItemSortPurchaseDate.setVisible(false);
        mMenuItemSortEntryDate.setVisible(true);
        mMenuItemSortCustomerName.setVisible(true);
        mMenuItemSortProductName.setVisible(true);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                queryBySearchKey(null,query,null);
                hideKeyboard(MainActivity.this);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                queryBySearchKey(null,newText,null);

                return false;
            }
        });
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent=new Intent(this,UptodateSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        else if(id==R.id.action_delete_all){
            deleteSelectedItems();
        }
        else if(id==R.id.action_sort_orderdate){
            queryBySearchKey(null,null,Orders.COLUMN_ORDER_DATE);
        }
        else if(id==R.id.action_sort_purchasedate){
            queryBySearchKey(null,null,Products.COLUMN_PURCHASE_DATE);
        }
        else if(id==R.id.action_sort_entrydate){
            //if(mDisplayContent==ORDERS)
                queryBySearchKey(null,null,Orders.COLUMN_ENTRY_DATE);
        }
        else if(id==R.id.action_sort_customername){
            queryBySearchKey(null,null,Customers.COLUMN_CUSTOMER_NAME);
        }
        else if(id==R.id.action_sort_productname){
            queryBySearchKey(null,null,Products.COLUMN_PRODUCT_NAME);
        }
        else if(id==R.id.action_all){
            mIsMenuItemAll=true;
            //mIsMenuItemCheckDeliverd=false;
            //mIsMenuItemCheckPayed=false;
            mActionDelivered.setChecked(false);
            mActionPayed.setChecked(false);
            queryBySearchKey(null,null,null);
        }
        else if(id==R.id.action_delivered){
            mActionPayed.setChecked(false);
            String selection = Orders.COLUMN_ORDER_STATUS + " LIKE ? ";
            String selectionArgs;
            if(!item.isChecked()) {
                item.setChecked(true);
                //mIsMenuItemCheckDeliverd=true;
                selectionArgs = getString(R.string.delivered);
                queryBySearchKey(selection,selectionArgs,null);
            }
            else{
                item.setChecked(false);
                //mIsMenuItemCheckDeliverd=false;
                selectionArgs =getString(R.string.not_delivered);
                queryBySearchKey(selection,selectionArgs,null);
            }
        }
        else if(id==R.id.action_payed){
            mActionDelivered.setChecked(false);
           String selection = Orders.COLUMN_ORDER_STATUS + " LIKE ? ";
           String selectionArgs;
            if(!item.isChecked()) {
                item.setChecked(true);
                //mIsMenuItemCheckPayed=true;
                selectionArgs = getString(R.string.paid);
                queryBySearchKey(selection,selectionArgs,null);
            }
            else{
                item.setChecked(false);
                //mIsMenuItemCheckPayed=false;
                selectionArgs = getString(R.string.not_paid);
                queryBySearchKey(selection,selectionArgs,null);

            }
        }
        return super.onOptionsItemSelected(item);
    }



    private void showDeleteDialog() {
        CustomDeleteDialog customDeleteDialog=new CustomDeleteDialog("Selected Orders will be deleted forever");
        customDeleteDialog.show(getSupportFragmentManager(),"Delete Confirmation");
    }

    @Override
    public void applyDeleteReply(boolean isConfirmed) {
        mIsDeleteConfirmed=isConfirmed;
        if(mIsDeleteConfirmed){
            deleteItems();
        }
    }

    private void deleteSelectedItems() {
        switch(mDisplayContent) {
            case CUSTOMERS:
                mDeletionListIds = mCustomerRecyclerAdapter.getSelectedIds();
                //mCustomerRecyclerAdapter.notifyDataSetChanged();
                break;
            case PRODUCTS:
                mDeletionListIds = mProductRecyclerAdapter.getSelectedIds();
                //mProductRecyclerAdapter.notifyDataSetChanged();
                break;
            case ORDERS:
            default:
                mDeletionListIds = mOrderRecyclerAdapter.getSelectedIds();
                //mOrderRecyclerAdapter.notifyDataSetChanged();
                break;
        }
        if(mDeletionListIds !=null && mDeletionListIds.size()>0) {
            showDeleteDialog();
        }
        else {
            CustomMessageBox customMessageBox=new CustomMessageBox("Please make Selection to Delete");
            customMessageBox.show(getSupportFragmentManager(),"Message");
        }
    }

    public void deleteItems(){
        AsyncTask<Void,Void,Void> task= new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                String selection=null;
                Uri tableName=null;
                String parent="null";
                switch(mDisplayContent) {
                    case CUSTOMERS:
                        selection = Customers._ID + " = ?";
                        tableName=Customers.CONTENT_URI;
                        parent="Customers";

                        break;
                    case PRODUCTS:
                        selection = Products._ID + " = ?";
                        tableName=Products.CONTENT_URI;
                        parent="Products";
                        break;

                        default:
                    case ORDERS:
                        selection = Orders._ID + " = ?";
                        tableName=Orders.CONTENT_URI;
                        parent="Orders";
                        break;

                }
                int deletedRowsCount=0;
                for (Integer id : mDeletionListIds) {
                    String selectionArgs[]={Integer.toString(id)};
                    int noRowsDeleted=getContentResolver().delete(tableName, selection, selectionArgs);
                    deletedRowsCount+=noRowsDeleted;
                }
               /* if(deletedRowsCount<mDeletionListIds.size()) {
                    CustomMessageBox customMessageBox=new CustomMessageBox(parent+" refered by Orders are not deleted.");
                    customMessageBox.show(getSupportFragmentManager(),"Message");
                }*/

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mDeletionListIds=null;
                queryBySearchKey(null,null, null);
            }


        };
        task.execute();
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_orders) {
            mDisplayContent=ORDERS;
            mMenuItemSortOrderDate.setVisible(true);
            mMenuItemSortPurchaseDate.setVisible(false);
            mMenuItemSortEntryDate.setVisible(true);
            mMenuItemSortCustomerName.setVisible(true);
            mMenuItemSortProductName.setVisible(true);
            mMenuItemViewOrders.setVisible(true);
            displayOrders();
        } else if (id == R.id.nav_products) {
            mDisplayContent=PRODUCTS;
            mMenuItemSortOrderDate.setVisible(false);
            mMenuItemSortPurchaseDate.setVisible(true);
            mMenuItemSortEntryDate.setVisible(true);
            mMenuItemSortCustomerName.setVisible(false);
            mMenuItemSortProductName.setVisible(true);
            mMenuItemViewOrders.setVisible(false);
            displayProducts();
        } else if (id == R.id.nav_customers) {
            mDisplayContent=CUSTOMERS;
            mMenuItemSortOrderDate.setVisible(false);
            mMenuItemSortPurchaseDate.setVisible(false);
            mMenuItemSortEntryDate.setVisible(true);
            mMenuItemSortCustomerName.setVisible(true);
            mMenuItemSortProductName.setVisible(false);
            mMenuItemViewOrders.setVisible(false);
            displayCustomers();
        }
         else if (id == R.id.nav_send) {
            sendGroupEmail();
        }
         else if(id==R.id.nav_settings){
            Intent intent=new Intent(this,UptodateSettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void sendGroupEmail() {
        try {
            String msg ="";
            String email=getAllCustomerEmailId();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            String organisation_name = pref.getString("signature", "");
            String subject = organisation_name + " - Order Details";
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("message/plain");
            String mailto = "mailto:"+email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(msg);

            intent.setData(Uri.parse(mailto));
            startActivity(intent);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            //Do Nothing
        }

    }

    private String getAllCustomerEmailId() {
        Cursor cursor=mCustomerRecyclerAdapter.getCursor();
        if(cursor==null)
            return "";
        cursor.moveToFirst();
        StringBuffer emailIds=new StringBuffer(cursor.getString(cursor.getColumnIndex(Customers.COLUMN_EMAIL_ID)));
        cursor.moveToFirst();
        while(cursor.moveToNext()){
            String email=cursor.getString(cursor.getColumnIndex(Customers.COLUMN_EMAIL_ID));
            if(email!=null && !email.equals("")){
                emailIds.append(";"+email);
            }
        }
        return emailIds.toString();
    }

    private void displayOrders() {
        mRecyclerItems.setAdapter(mOrderRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_orders);
    }
    private void displayCustomers() {
        mRecyclerItems.setAdapter(mCustomerRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_customers);
    }

    private void displayProducts() {

        mRecyclerItems.setAdapter(mProductRecyclerAdapter);
        selectNavigationMenuItem(R.id.nav_products);
    }
    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView=(NavigationView)findViewById(R.id.nav_view);
        Menu menu=navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }


    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader=null;
        if(id==LOADER_PRODUCTS){
            mProductColumns = new String[]{
                    Products._ID,
                    Products.COLUMN_PRODUCT_NAME,
                    Products.COLUMN_PURCHASE_DATE,
                    Products.COLUMN_QUANTITY,
                    Products.COLUMN_ACTUAL_PRICE,
                    Products.COLUMN_SELLING_PRICE};
            final String OrderBY= Products.COLUMN_PURCHASE_DATE;
            loader=new CursorLoader(this, Products.CONTENT_URI, mProductColumns,null,null,OrderBY);
        }
        else if(id==LOADER_CUSTOMERS){
            mCustomerColumns = new String[]{
                    Customers._ID,
                    Customers.COLUMN_CUSTOMER_NAME,
                    Customers.COLUMN_CONTACT_NUMBER,
                    Customers.COLUMN_ALTERNATIVE_NUMBER,
                    Customers.COLUMN_EMAIL_ID,
                    Customers.COLUMN_ENTRY_DATE};
            final String OrderBY= Customers.COLUMN_CUSTOMER_NAME;
            loader=new CursorLoader(this, Customers.CONTENT_URI, mCustomerColumns,null,null,OrderBY);
        }
        else if(id==LOADER_ORDERS) {

            mOrderColumns = new String[]{
                    OrderInfoEntry.getQName(OrderInfoEntry._ID),
                    Orders.COLUMN_ORDER_CUSTOMER_NAME,
                    Orders.COLUMN_CONTACT_NUMBER,
                    Orders.COLUMN_ORDER_PRODUCT_NAME,
                    Orders.COLUMN_ORDER_DATE,
                    Orders.COLUMN_ORDER_QUANTITY,
                    Orders.COLUMN_ACTUAL_PRICE,
                    Orders.COLUMN_SELLING_PRICE,
                    Orders.COLUMN_CASH_PAYED,
                    Orders.COLUMN_ORDER_STATUS
            };
            final String orderBY= Orders.COLUMN_ORDER_DATE + " DESC ";
            loader=new CursorLoader(this, Orders.CONTENT_EXTENDED_URI, mOrderColumns,null,null,orderBY );
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if(loader.getId()==LOADER_PRODUCTS){
            mProductRecyclerAdapter.setSelectionMode(false);
            mProductRecyclerAdapter.changeCursor(data);
            if(mDisplayContent==PRODUCTS)
                displayProducts();
        }
        else if(loader.getId()==LOADER_CUSTOMERS) {
            mCustomerRecyclerAdapter.setSelectionMode(false);
            mCustomerRecyclerAdapter.changeCursor(data);
            if(mDisplayContent==CUSTOMERS)
                displayCustomers();
        }
        else if(loader.getId()==LOADER_ORDERS) {
            mOrderRecyclerAdapter.setSelectionMode(false);
            mOrderRecyclerAdapter.changeCursor(data);
            if(mDisplayContent==ORDERS)
                displayOrders();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if(loader.getId()==LOADER_PRODUCTS){
            mProductRecyclerAdapter.changeCursor(null);
        }
        else if(loader.getId()==LOADER_CUSTOMERS) {
            mCustomerRecyclerAdapter.changeCursor(null);
        }
        else if(loader.getId()==LOADER_ORDERS) {
            mOrderRecyclerAdapter.changeCursor(null);
        }

    }
}
