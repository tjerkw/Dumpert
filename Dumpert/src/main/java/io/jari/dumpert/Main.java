package io.jari.dumpert;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.listeners.ActionClickListener;
import io.jari.dumpert.animators.SlideInOutBottomItemAnimator;
import io.jari.dumpert.api.API;
import io.jari.dumpert.api.Item;

import java.io.IOException;


public class Main extends Base {

    DrawerLayout drawerLayout;
    RecyclerView drawerRecyclerView;
    RecyclerView recyclerView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    SwipeRefreshLayout swipeRefreshLayout;
    String currentPath = "/";
    NavigationAdapter navigationAdapter;

    private boolean loading = false;
    int pastVisibleItems, visibleItemCount, totalItemCount;
    int page = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.drawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        this.drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                if(cardAdapter != null) cardAdapter.removeAll();
                loadData(true, currentPath);
                page = 0;
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

                if (!loading && (visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    page++;
                    addData(page, currentPath);
                    loading = true;
                }
            }
        });

        drawerRecyclerView = (RecyclerView) findViewById(R.id.left_drawer);
        drawerRecyclerView.setHasFixedSize(true);

        final LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(this);
        drawerRecyclerView.setLayoutManager(linearLayoutManager2);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_drawer, R.string.close_drawer);

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                actionBarDrawerToggle.syncState();
            }
        });

        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        this.populateNavigation();

        currentPath = "/"; //load frontpage
        this.loadData(false, currentPath);

        if(preferences.getBoolean("switchtosettings", false)) {
            preferences.edit().putBoolean("switchtosettings", false).apply();
            switchToSettings(settingsNavItem);
        }
    }

    public void switchToSettings(NavigationItem navigationItem) {
        drawerLayout.closeDrawer(drawerRecyclerView);
        if(cardAdapter != null) cardAdapter.removeAll();
        navigationAdapter.setActive(navigationItem);
        FrameLayout preferences = (FrameLayout)findViewById(R.id.settings_frame);
        preferences.setVisibility(View.VISIBLE);
        getFragmentManager().beginTransaction().replace(R.id.settings_frame, new PreferencesFragment()).commit();
    }

    NavigationItem settingsNavItem;

    public void populateNavigation() {
        //nieuw
        NavigationItem navigationItem = new NavigationItem();
        navigationItem.title = getResources().getString(R.string.nav_new);
        navigationItem.drawable = getResources().getDrawable(R.drawable.ic_new_releases);
        navigationItem.selected = true;
        navigationItem.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                currentPath = "/";
                super.onClick(navigationItem);
            }
        };

        //toppers
        NavigationItem navigationItemHot = new NavigationItem();
        navigationItemHot.title = getResources().getString(R.string.nav_top);
        navigationItemHot.drawable = getResources().getDrawable(R.drawable.ic_whatshot);
        navigationItemHot.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                currentPath = "/toppers/";
                super.onClick(navigationItem);
            }
        };

        //plaatjes
        NavigationItem navigationItemPlaatjes = new NavigationItem();
        navigationItemPlaatjes.title = getResources().getString(R.string.nav_images);
        navigationItemPlaatjes.hasDivider = true;
        navigationItemPlaatjes.drawable = getResources().getDrawable(R.drawable.ic_photo2);
        navigationItemPlaatjes.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                currentPath = "/plaatjes/";
                super.onClick(navigationItem);
            }
        };

        //filmpjes
        NavigationItem navigationItemVideos = new NavigationItem();
        navigationItemVideos.title = getResources().getString(R.string.nav_videos);
        navigationItemVideos.drawable = getResources().getDrawable(R.drawable.ic_play_circle_fill2);
        navigationItemVideos.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                currentPath = "/filmpjes/";
                super.onClick(navigationItem);
            }
        };

        //audio
        NavigationItem navigationItemAudio = new NavigationItem();
        navigationItemAudio.title = getResources().getString(R.string.nav_audio);
        navigationItemAudio.drawable = getResources().getDrawable(R.drawable.ic_volume_up);
        navigationItemAudio.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                currentPath = "/audio/";
                super.onClick(navigationItem);
            }
        };

        //settings
        NavigationItem navigationItemSettings = new NavigationItem();
        navigationItemSettings.title = getResources().getString(R.string.nav_settings);
        navigationItemSettings.hasDivider = true;
        navigationItemSettings.drawable = getResources().getDrawable(R.drawable.ic_settings);
        settingsNavItem = navigationItemSettings;
        navigationItemSettings.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                switchToSettings(settingsNavItem);
            }
        };

        //about
        NavigationItem navigationItemAbout = new NavigationItem();
        navigationItemAbout.title = getResources().getString(R.string.nav_about);
        navigationItemAbout.drawable = getResources().getDrawable(R.drawable.ic_info);
        navigationItemAbout.callback = new NavigationItemCallback() {
            @Override
            public void onClick(NavigationItem navigationItem) {
                drawerLayout.closeDrawer(drawerRecyclerView);
                Intent intent = new Intent(Main.this, About.class);
                startActivity(intent);
            }
        };

        navigationAdapter =
                new NavigationAdapter(new NavigationItem[]{
                        navigationItem,
                        navigationItemHot,
                        navigationItemPlaatjes,
                        navigationItemVideos,
                        navigationItemAudio,
                        navigationItemSettings,
                        navigationItemAbout
                }, this);
        drawerRecyclerView.setAdapter(navigationAdapter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    Snackbar offlineSnackbar;
    boolean offlineSnackDismissed = false;

    public void offlineSnack() {
        if (Utils.isOffline(this)) {
            getSupportActionBar().setSubtitle(R.string.cached_version);
            if (offlineSnackDismissed) return;

            if (offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();

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
            if (offlineSnackDismissed) return;

            if (offlineSnackbar != null && offlineSnackbar.isShowing()) offlineSnackbar.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBarDrawerToggle.onOptionsItemSelected(item);
    }

    CardAdapter cardAdapter;

    public void loadData(final boolean refresh, final String path) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getListing(getApplicationContext(), path);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter = new CardAdapter(new Item[0], Main.this);
                            recyclerView.setAdapter(cardAdapter);

                            if (refresh) {
                                swipeRefreshLayout.setRefreshing(false);
                                cardAdapter.removeAll();
                            }

                            cardAdapter.addItems(items);
                        }
                    });

                } catch (IOException e) {
                    if (refresh)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                swipeRefreshLayout.setRefreshing(false);
                            }
                        });
                    errorSnack(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void errorSnack(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.with(Main.this)
                        .text(R.string.items_failed)
                        .textColor(Color.parseColor("#FFCDD2"))
                        .actionLabel(R.string.moreinfo)
                        .duration(10000)
                        .actionListener(new ActionClickListener() {
                            @Override
                            public void onActionClicked(Snackbar snackbar) {
                                new AlertDialog.Builder(Main.this)
                                        .setTitle(R.string.moreinfo)
                                        .setMessage(e.getMessage())
                                        .create()
                                        .show();
                            }
                        })
                        .show(Main.this);
            }
        });
    }

    public void addData(final Integer page, final String path) {
        this.offlineSnack();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Item[] items = API.getListing(page, getApplicationContext(), path);
                    if (items.length == 0) Main.this.page--; //if API returned nothing, put page number back
                    loading = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cardAdapter.addItems(items);
                        }
                    });
                } catch (IOException e) {
                    Main.this.page--;
                    errorSnack(e);
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public abstract class NavigationItemCallback {
        public void onClick(NavigationItem navigationItem) {
            navigationAdapter.setActive(navigationItem);
            drawerLayout.closeDrawer(drawerRecyclerView);
            findViewById(R.id.settings_frame).setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            cardAdapter.removeAll();
            page = 1;
            loading = false;
            loadData(true, currentPath);
        }
    }
}
