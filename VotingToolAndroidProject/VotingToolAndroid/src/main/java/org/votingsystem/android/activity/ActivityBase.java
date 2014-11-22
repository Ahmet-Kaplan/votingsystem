package org.votingsystem.android.activity;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.votingsystem.android.AppContextVS;
import org.votingsystem.android.R;
import org.votingsystem.android.fragment.MessageDialogFragment;
import org.votingsystem.android.fragment.PinDialogFragment;
import org.votingsystem.android.fragment.ProgressDialogFragment;
import org.votingsystem.android.service.WebSocketService;
import org.votingsystem.android.ui.debug.DebugActionRunnerFragment;
import org.votingsystem.android.util.BuildConfig;
import org.votingsystem.android.util.HelpUtils;
import org.votingsystem.android.util.PrefUtils;
import org.votingsystem.android.util.UIUtils;
import org.votingsystem.android.util.WebSocketRequest;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.TypeVS;
import org.votingsystem.model.UserVS;
import org.votingsystem.util.ResponseVS;
import org.votingsystem.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

import static org.votingsystem.android.util.LogUtils.LOGD;
import static org.votingsystem.android.util.LogUtils.makeLogTag;


public abstract class ActivityBase extends ActionBarActivity {

    private static final String TAG = makeLogTag(ActivityBase.class);

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private AppContextVS contextVS = null;
    private ObjectAnimator mStatusBarColorAnimator;
    private ViewGroup mDrawerItemsListContainer;

    // When set, these components will be shown/hidden in sync with the action bar
    // to implement the "quick recall" effect (the Action Bar and the header views disappear
    // when you scroll down a list, and reappear quickly when you scroll up).
    private ArrayList<View> mHideableHeaderViews = new ArrayList<View>();
    // Durations for certain animations we use:
    private static final int HEADER_HIDE_ANIM_DURATION = 300;

    // symbols for navdrawer items (indices must correspond to array below). This is
    // not a list of items that are necessarily *present* in the Nav Drawer; rather,
    // it's a list of all possible items.
    protected static final int NAVDRAWER_ITEM_POLLS = 0;
    protected static final int NAVDRAWER_ITEM_REPRESENTATIVES = 1;
    protected static final int NAVDRAWER_ITEM_RECEIPTS = 2;
    protected static final int NAVDRAWER_ITEM_FINANCE = 3;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 4;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.polls_lbl,
            R.string.representatives_lbl,
            R.string.receipts_lbl,
            R.string.finance_lbl,
            R.string.navdrawer_item_settings,
    };

    // icons for navdrawer items (indices must correspond to above array)
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.poll_32,
            R.drawable.system_users_32,
            R.drawable.fa_cert_32,
            R.drawable.fa_money_32,
            R.drawable.ic_drawer_settings,
    };

    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();
    private View[] mNavDrawerItemViews = null;

    // handle to our sync observer (that notifies us about changes in our sync state)
    private Object mSyncObserverHandle;
    Thread mDataBootstrapThread = null;

    // variables that control the Action Bar auto hide behavior (aka "quick recall")
    private boolean mActionBarAutoHideEnabled = false;
    private boolean mActionBarShown = true;
    private TextView connectionStatusText;
    private ImageView connectionStatusView;

    // A Runnable that we should execute when the navigation drawer finishes its closing animation
    private Runnable mDeferredOnDrawerClosedRunnable;
    private static final TypeEvaluator ARGB_EVALUATOR = new ArgbEvaluator();

    private String broadCastId = ActivityBase.class.getSimpleName();
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override public void onReceive(Context context, Intent intent) {
            LOGD(TAG + ".broadcastReceiver", "extras: " + intent.getExtras());
            ResponseVS responseVS = intent.getParcelableExtra(ContextVS.RESPONSEVS_KEY);
            WebSocketRequest request = intent.getParcelableExtra(ContextVS.WEBSOCKET_REQUEST_KEY);
            if(intent.getStringExtra(ContextVS.PIN_KEY) != null) {
                switch(responseVS.getTypeVS()) {
                    case WEB_SOCKET_INIT:
                        ProgressDialogFragment.showDialog(
                                getString(R.string.connecting_caption),
                                getString(R.string.connecting_to_service_msg),
                                getSupportFragmentManager());
                        new Thread(new Runnable() {
                            @Override public void run() { toggleWebSocketServiceConnection(); }
                        }).start();
                        break;
                }
            } else if(request != null) {
                LOGD(TAG + ".broadcastReceiver", "WebSocketRequest typeVS: " + request.getTypeVS());
                ProgressDialogFragment.hide(getSupportFragmentManager());
                switch(request.getTypeVS()) {
                    case INIT_VALIDATED_SESSION:
                        if(ResponseVS.SC_OK == request.getStatusCode()) {
                            connectionStatusText.setText(getString(R.string.connected_lbl));
                            connectionStatusView.setVisibility(View.VISIBLE);
                        } else if(ResponseVS.SC_ERROR == request.getStatusCode())
                            MessageDialogFragment.showDialog(request.getStatusCode(),
                                request.getCaption(), request.getMessage(), getSupportFragmentManager());
                        break;
                    case WEB_SOCKET_CLOSE:
                        connectionStatusText.setText(getString(R.string.disconnected_lbl));
                        connectionStatusView.setVisibility(View.GONE);
                        break;
                }
            }
        }
    };

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contextVS = (AppContextVS) getApplicationContext();
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Returns the navigation drawer item that corresponds to this Activity. Subclasses
     * of BaseActivity override this to indicate what nav drawer item corresponds to them
     * Return NAVDRAWER_ITEM_INVALID to mean that this Activity should not have a Nav Drawer.
     */
    protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_INVALID;
    }

    /**
     * Sets up the navigation drawer as appropriate. Note that the nav drawer will be
     * different depending on whether the attendee indicated that they are attending the
     * event on-site vs. attending remotely.
     */
    private void setupNavDrawer() {
        // What nav drawer item should be selected?
        int selfItem = getSelfNavDrawerItem();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout == null) {
            return;
        }
        final DrawerLayout.DrawerListener drawerListener =  new DrawerLayout.DrawerListener() {
            @Override public void onDrawerClosed(View drawerView) {
                // run deferred action, if we have one
                if (mDeferredOnDrawerClosedRunnable != null) {
                    mDeferredOnDrawerClosedRunnable.run();
                    mDeferredOnDrawerClosedRunnable = null;
                }
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                updateStatusBarForNavDrawerSlide(0f);
                onNavDrawerStateChanged(false, false);
            }
            @Override public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                updateStatusBarForNavDrawerSlide(1f);
                onNavDrawerStateChanged(true, false);
            }
            @Override public void onDrawerStateChanged(int newState) {
                invalidateOptionsMenu();
                onNavDrawerStateChanged(isNavDrawerOpen(), newState != DrawerLayout.STATE_IDLE);
            }
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {
                updateStatusBarForNavDrawerSlide(slideOffset);
                onNavDrawerSlide(slideOffset);
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerListener.onDrawerClosed(drawerView);
            }
            @Override public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerListener.onDrawerOpened(drawerView);
            }
            @Override public void onDrawerStateChanged(int newState) {
                super.onDrawerStateChanged(newState);
                drawerListener.onDrawerStateChanged(newState);
            }
            @Override public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                drawerListener.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        populateNavDrawer();
        mDrawerToggle.syncState();
        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.isDataBootstrapDone(this)) {
            PrefUtils.markDataBootstrapDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    // Subclasses can override this for custom behavior
    protected void onNavDrawerStateChanged(boolean isOpen, boolean isAnimating) {
        if (mActionBarAutoHideEnabled && isOpen) {
            autoShowOrHideActionBar(true);
        }
    }

    protected void onNavDrawerSlide(float offset) {}

    protected boolean isNavDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.START);
    }

    private void populateNavDrawer() {
        mNavDrawerItems.clear();
        mNavDrawerItems.add(NAVDRAWER_ITEM_POLLS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_REPRESENTATIVES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_RECEIPTS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_FINANCE);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR_SPECIAL);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }
        connectionStatusText = (TextView) findViewById(R.id.connection_status_text);
        connectionStatusView = (ImageView) findViewById(R.id.connection_status_img);
        LinearLayout userBox = (LinearLayout)findViewById(R.id.user_box);
        userBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                LOGD(TAG, "userBox clicked");
                if(contextVS.getWebSocketSession() == null) {
                    PinDialogFragment.showPinScreen(getSupportFragmentManager(), broadCastId, getString(
                            R.string.init_authenticated_session_pin_msg), false, TypeVS.WEB_SOCKET_INIT);
                } else {showConnectionStatusDialog();}
            }
        });
        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    /**
     * Sets up the given navdrawer item's appearance to the selected state. Note: this could
     * also be accomplished (perhaps more cleanly) with state-based layouts.
     */
    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    public void requestDataRefresh() {
        LOGD(TAG + ".requestDataRefresh", "requestDataRefresh");
    }

    @Override protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupNavDrawer();
        setupAccountBox();
    }

    private void setupAccountBox() {
        final View chosenAccountView = findViewById(R.id.chosen_account_view);
        if(chosenAccountView != null) {
            chosenAccountView.setVisibility(View.VISIBLE);
            TextView nameTextView = (TextView) chosenAccountView.findViewById(R.id.profile_name_text);
            TextView email = (TextView) chosenAccountView.findViewById(R.id.profile_email_text);
            UserVS sessionUserVS = PrefUtils.getSessionUserVS(this);
            if(sessionUserVS != null) {
                nameTextView.setText(sessionUserVS.getName());
                email.setText(sessionUserVS.getEmail());
                chosenAccountView.setEnabled(true);
            }
        }
    }

    @Override public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_base, menu);
        MenuItem debugItem = menu.findItem(R.id.menu_debug);
        if (debugItem != null) {
            debugItem.setVisible(BuildConfig.DEBUG);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (id) {
            case R.id.menu_about:
                HelpUtils.showAbout(this);
                return true;
            case R.id.menu_debug:
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(getBaseContext(), FragmentContainerActivity.class);
                    intent.putExtra(ContextVS.FRAGMENT_KEY, DebugActionRunnerFragment.class.getName());
                    startActivity(intent);
                }
                return true;
            case R.id.close_app:
                ((AppContextVS)getApplicationContext()).finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void goToNavDrawerItem(int item) {
        Intent intent;
        switch (item) {
            case NAVDRAWER_ITEM_POLLS:
                intent = new Intent(this, EventVSMainActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_REPRESENTATIVES:
                intent = new Intent(this, RepresentativesMainActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_RECEIPTS:
                intent = new Intent(getBaseContext(), ReceiptsMainActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_FINANCE:
                intent = new Intent(this, FinanceMainActivity.class);
                startActivity(intent);
                finish();
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void onNavDrawerItemClicked(final int itemId) {
        LOGD(TAG + ".onNavDrawerItemClicked", "itemId: " + itemId +
                " - selfNavDrawerItem: " + getSelfNavDrawerItem());
        if (itemId == getSelfNavDrawerItem()) {
            mDrawerLayout.closeDrawer(Gravity.START);
            return;
        }
        if (isSpecialItem(itemId)) {
            goToNavDrawerItem(itemId);
        } else {
            goToNavDrawerItem(itemId);
            // change the active item on the list so the user can see the item changed
            setSelectedNavDrawerItem(itemId);
        }
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    @Override protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                broadcastReceiver, new IntentFilter(broadCastId));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                broadcastReceiver, new IntentFilter(ContextVS.WEB_SOCKET_BROADCAST_ID));
        mSyncStatusObserver.onStatusChanged(0);
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).
                unregisterReceiver(broadcastReceiver);
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override public void onStart() {
        super.onStart();
        if (!PrefUtils.isDataBootstrapDone(this) && mDataBootstrapThread == null) {
            performDataBootstrap();
        }
    }

    private void performDataBootstrap() {
        final Context appContext = getApplicationContext();
        LOGD(TAG, "performDataBootstrap - starting activity bootstrap background thread");
        mDataBootstrapThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LOGD(TAG, "Starting data bootstrap process.");
                try {// Load data from bootstrap raw resource
                    String bootstrapJson = StringUtils.parseResource(appContext, R.raw.bootstrap_data);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                mDataBootstrapThread = null;
            }
        });
        mDataBootstrapThread.start();
    }

    protected void autoShowOrHideActionBar(boolean show) {
        if (show == mActionBarShown)  return;
        mActionBarShown = show;
        if (show) getSupportActionBar().show();
        else getSupportActionBar().hide();
        onActionBarAutoShowOrHide(show);
    }

    private void showConnectionStatusDialog() {
        if(contextVS.getWebSocketSession() != null) {
            View dialogView = getLayoutInflater().inflate(R.layout.connection_status_dialog, null);
            TextView userInfoText = (TextView) dialogView.findViewById(R.id.user_info_text);
            UserVS sessionUserVS = PrefUtils.getSessionUserVS(this);
            if(sessionUserVS != null) {
                userInfoText.setText(sessionUserVS.getEmail());
            }
            final AlertDialog dialog = new AlertDialog.Builder(this).setTitle(
                    getString(R.string.connected_with_lbl)).setView(dialogView).create();
            Button connectionButton = (Button) dialogView.findViewById(R.id.connection_button);
            connectionButton.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    toggleWebSocketServiceConnection();
                    dialog.hide();
                }
            });
            dialog.show();
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = getSelfNavDrawerItem() == itemId;
        int layoutToInflate = 0;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);
        if (isSeparator(itemId)) {
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0)  iconView.setImageResource(iconId);
        titleView.setText(getString(titleId));
        formatNavDrawerItem(view, itemId, selected);
        view.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return view;
    }

    private boolean isSpecialItem(int itemId) {
        return itemId == NAVDRAWER_ITEM_SETTINGS;
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            return;// not applicable
        }
        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                getResources().getColor(R.color.navdrawer_text_color_selected) :
                getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ?
                getResources().getColor(R.color.navdrawer_icon_tint_selected) :
                getResources().getColor(R.color.navdrawer_icon_tint));
    }

    @Override protected void onDestroy() {
        super.onDestroy();
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(final int which) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LOGD(TAG, "SyncStatusObserver  - onStatusChanged - which: " + which);
                }
            });
        }
    };

    protected void registerHideableHeaderView(View hideableHeaderView) {
        if (!mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.add(hideableHeaderView);
        }
    }

    protected void deregisterHideableHeaderView(View hideableHeaderView) {
        if (mHideableHeaderViews.contains(hideableHeaderView)) {
            mHideableHeaderViews.remove(hideableHeaderView);
        }
    }

    private void updateStatusBarForNavDrawerSlide(float slideOffset) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
    }

    private void toggleWebSocketServiceConnection() {
        Intent startIntent = new Intent(((AppContextVS)getApplicationContext()), WebSocketService.class);
        TypeVS typeVS = TypeVS.WEB_SOCKET_INIT;
        if(((AppContextVS)getApplicationContext()).getWebSocketSession().getSessionId() != null)
            typeVS = TypeVS.WEB_SOCKET_CLOSE;
        LOGD(TAG + ".toggleWebSocketServiceConnection", "operation: " + typeVS.toString());
        startIntent.putExtra(ContextVS.TYPEVS_KEY, typeVS);
        startService(startIntent);
    }

    protected void onActionBarAutoShowOrHide(boolean shown) {
        if (mStatusBarColorAnimator != null) {
            mStatusBarColorAnimator.cancel();
        }
        mStatusBarColorAnimator.setEvaluator(ARGB_EVALUATOR);
        mStatusBarColorAnimator.start();
        for (View view : mHideableHeaderViews) {
            if (shown) {
                view.animate().translationY(0).alpha(1).setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            } else {
                view.animate().translationY(-view.getBottom()).alpha(0).setDuration(HEADER_HIDE_ANIM_DURATION)
                        .setInterpolator(new DecelerateInterpolator());
            }
        }
    }

    @Override public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
