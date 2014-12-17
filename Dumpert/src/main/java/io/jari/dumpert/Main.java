package io.jari.dumpert;

import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Item;

import java.io.IOException;


public class Main extends Base {

    DrawerLayout drawerLayout;
    RecyclerView recyclerView;

    private boolean loading = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.drawerLayout = (DrawerLayout)this.findViewById(R.id.drawer_layout);
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                cardAdapter.removeAll();
                loadData(true);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        recyclerView.setItemAnimator(new SlideInOutBottomItemAnimator(recyclerView));

        // use a linear layout manager
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

                if (!loading) {
                    if ( (visibleItemCount+ pastVisibleItems) >= totalItemCount) {
                        page++;
                        addData(page);
                        loading = true;
                    }
                }
            }
        });

        this.loadData(false);
    }

    Snackbar offlineSnackbar;
    boolean offlineSnackDismissed = false;
    public void offlineSnack() {
        if(offlineSnackDismissed) return;
        if(Utils.isOffline(this)) {
            getSupportActionBar().setSubtitle(R.string.cached_version);
            if(offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();

            offlineSnackbar = Snackbar.with(this).text(getResources().getString(R.string.tip_offline))
                    .duration(999999999)
                    .animation(false)
                    .swipeToDismiss(false)
                    .actionLabel(R.string.tip_close)
                    .actionListener(new ActionClickListener() {
                        @Override
                        public void onActionClicked(Snackbar snackbar) {
                            offlineSnackDismissed = true;
                        }
                    });

            offlineSnackbar.show(this);
        } else {
            getSupportActionBar().setSubtitle("");
            if(offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();
        }
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

    CardAdapter cardAdapter;
    public void loadData(final boolean refresh) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getFrontpage(getApplicationContext());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter = new CardAdapter(new Item[0], Main.this);
                            recyclerView.setAdapter(cardAdapter);

                            if(refresh) {
                                final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
                                swipeRefreshLayout.setRefreshing(false);
                                cardAdapter.removeAll();
                            }

                            cardAdapter.addItems(items);
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void addData(final Integer page) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getFrontpage(page, getApplicationContext());
                    if(items.length == 0) Main.this.page--; //if API returned nothing, put page number back
                    loading = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter.addItems(items);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
