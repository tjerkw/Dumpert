package io.jari.dumpert;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Item;

import java.io.IOException;


public class Main extends Base {

    DrawerLayout drawerLayout;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.drawerLayout = (DrawerLayout)this.findViewById(R.id.drawer_layout);
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        this.loadData();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getFrontpage();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            RecyclerView.Adapter cardAdapter = new CardAdapter(items, getApplicationContext());
                            recyclerView.setAdapter(cardAdapter);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
