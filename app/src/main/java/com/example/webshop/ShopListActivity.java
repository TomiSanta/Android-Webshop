package com.example.webshop;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ShopListActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopListActivity.class.getName();
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private RecyclerView mRecycleView;
    private ArrayList<ShoppingItem> mItemList;
    private ShoppingItemAdapter mAdapter;
    private FrameLayout purpleCircle;
    private TextView contentTextView;
    private int gridNumber = 1;
    private int cartItems = 0;
    private FirebaseFirestore mFirestore;
    private CollectionReference mItems;
    private Notification mNotification;
    private boolean viewRow = true;
    private int queryLimit = 15;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_list);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Log.d(LOG_TAG, "Hitelesített felhasználó!");
        }else{
            Log.d(LOG_TAG, "Nem hitelesített felhasználó!");
            finish();
        }

        mRecycleView = findViewById(R.id.recycleView);
        mRecycleView.setLayoutManager(new GridLayoutManager(this, gridNumber));
        mItemList = new ArrayList<>();
        
        mAdapter = new ShoppingItemAdapter(this, mItemList);
        mRecycleView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        queryData();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        this.registerReceiver(powerReceiver, filter);

        mNotification = new Notification(this);
    }

    BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null)
                return;
            switch(action){
                case Intent.ACTION_POWER_CONNECTED:
                    queryLimit = 15;
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    queryLimit = 8;
                    break;
            }
            queryData();
        }
    };
    private void intializeData() {
        String[] itemsList = getResources().getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources().getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources().getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources = getResources().obtainTypedArray(R.array.shopping_item_image);
        TypedArray itemsRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        for(int i = 0; i < itemsList.length; i++)
            mItems.add(new ShoppingItem(itemsList[i], itemsInfo[i], itemsPrice[i], itemsRate.getFloat(i, 0), itemsImageResources.getResourceId(i,0), 0));

        itemsImageResources.recycle();
    }

    private void queryData() {
        mItemList.clear();
        mItems.orderBy("cartedCount", Query.Direction.DESCENDING).limit(queryLimit).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots){
                ShoppingItem item = document.toObject(ShoppingItem.class);
                item.setId(document.getId());
                mItemList.add(item);
            }
            if(mItemList.size() == 0){
                intializeData();
                queryData();
            }
            mAdapter.notifyDataSetChanged();
        });
    }

    public void deleteItem(ShoppingItem item) {
        DocumentReference referencia = mItems.document(item._getId());
        referencia.delete().addOnSuccessListener(succes -> {
            Log.d(LOG_TAG, "Az elem sikeresen törölve lett: " + item._getId());
        });
        referencia.delete().addOnFailureListener(failure -> {
            Toast.makeText(this, "Az elem: " + item._getId() + " nem lett sikeresen törölve:(", Toast.LENGTH_LONG).show();
        });
        queryData();
        mNotification.cancel();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shoplist_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searcView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searcView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                Log.d(LOG_TAG, "Kiléptél!");
                FirebaseAuth.getInstance().signOut();
                finish();
                return true;
            case R.id.settings:
                Log.d(LOG_TAG, "Beállításokra kattintottál!");
                return true;
            case R.id.cart:
                Log.d(LOG_TAG, "Bevásárló kosárra kattintottál!");
                return true;
            case R.id.view_selector:
                Log.d(LOG_TAG, "Elrendezési nézet gombra kattintottál!");
                if(viewRow){
                    changeSpanCount(item, R.drawable.view_module_logo, 1);
                }else{
                    changeSpanCount(item, R.drawable.view_stream_logo, 2);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount){
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecycleView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();
        purpleCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_purple_circle);
        contentTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onOptionsItemSelected(alertMenuItem);
            }
        });
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon(ShoppingItem item) {
        cartItems = (cartItems + 1);
        if(0<cartItems){
            contentTextView.setText(String.valueOf(cartItems));
        }else{
            contentTextView.setText("");
        }
        purpleCircle.setVisibility((cartItems>0) ? View.VISIBLE : View.GONE);

        mItems.document(item._getId()).update("cartedCount", item.getCartedCount() + 1)
            .addOnFailureListener(failure -> {
                Toast.makeText(this, "Az elem: " + item._getId() + " nem lehet megváltoztatni!", Toast.LENGTH_LONG).show();
            });

        mNotification.send(item.getName());

        queryData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(powerReceiver);
    }

}