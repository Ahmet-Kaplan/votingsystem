package org.votingsystem.android.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import org.votingsystem.android.AppContextVS;
import org.votingsystem.android.R;
import org.votingsystem.android.fragment.TransactionVSGridFragment;
import org.votingsystem.android.fragment.UserVSAccountsFragment;
import org.votingsystem.android.ui.NavigatorDrawerOptionsAdapter;
import org.votingsystem.model.ContextVS;
import org.votingsystem.util.DateUtils;

import java.util.Calendar;

import static org.votingsystem.android.util.LogUtils.LOGD;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class FinanceMainActivity extends ActivityBase {

    public static final String TAG = FinanceMainActivity.class.getSimpleName();

    private AppContextVS contextVS;

    @Override public void onCreate(Bundle savedInstanceState) {
        LOGD(TAG + ".onCreate", "savedInstanceState: " + savedInstanceState);
        super.onCreate(savedInstanceState);
        contextVS = (AppContextVS) getApplicationContext();
        setContentView(R.layout.finance_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_vs);
        setSupportActionBar(toolbar);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        LOGD(TAG + ".onCreate", "savedInstanceState: " + savedInstanceState);
        VicketPagerAdapter pagerAdapter = new VicketPagerAdapter(getSupportFragmentManager(),
                getIntent().getExtras());
        mViewPager.setAdapter(pagerAdapter);
        getSupportActionBar().setTitle(getString(R.string.uservs_accounts_lbl));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        LOGD(TAG + ".onOptionsItemSelected", " - item: " + item.getTitle());
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.admin_vickets_menu_item:
                intent = new Intent(this, BrowserVSActivity.class);
                intent.putExtra(ContextVS.URL_KEY, contextVS.getVicketServer().getMenuAdminURL());
                startActivity(intent);
                return true;
            case R.id.vickets_menu_user_item:
                intent = new Intent(this, BrowserVSActivity.class);
                intent.putExtra(ContextVS.URL_KEY, contextVS.getVicketServer().getMenuUserURL());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override protected int getSelfNavDrawerItem() {
        return NAVDRAWER_ITEM_FINANCE;// we only have a nav drawer if we are in top-level
    }

    @Override public void requestDataRefresh() {
        LOGD(TAG, ".requestDataRefresh() - Requesting manual data refresh - refreshing:");
    }

    class VicketPagerAdapter extends FragmentStatePagerAdapter {

        final String TAG = VicketPagerAdapter.class.getSimpleName();

        private NavigatorDrawerOptionsAdapter.ChildPosition selectedChild = null;
        private NavigatorDrawerOptionsAdapter.GroupPosition selectedGroup =
                NavigatorDrawerOptionsAdapter.GroupPosition.VICKETS;

        private String searchQuery = null;
        private Bundle args;

        public VicketPagerAdapter(FragmentManager fragmentManager, Bundle args) {
            super(fragmentManager);
            this.args = (args != null)? args:new Bundle();
        }

        @Override public Fragment getItem(int position) {
            NavigatorDrawerOptionsAdapter.ChildPosition childPosition = selectedGroup.getChildList().get(position);
            Fragment selectedFragment = null;
            switch(childPosition) {
                case VICKET_USER_INFO:
                    selectedFragment = new UserVSAccountsFragment();
                    break;
                case VICKET_LIST:
                    selectedFragment = new TransactionVSGridFragment();
                    break;
            }
            args.putString(SearchManager.QUERY, searchQuery);
            selectedFragment.setArguments(args);
            LOGD(TAG + ".getItem", "position:" + position + " - args: " + args +
                    " - selectedFragment.getClass(): " + ((Object)selectedFragment).getClass());
            return selectedFragment;
        }

        public String getSelectedChildDescription(AppContextVS context) {
            switch(selectedChild) {
                case VICKET_USER_INFO:
                    DateUtils.TimePeriod timePeriod = DateUtils.getWeekPeriod(Calendar.getInstance());
                    String periodLbl = context.getString(R.string.week_lapse_lbl, DateUtils.getDayWeekDateStr(
                            timePeriod.getDateFrom()), DateUtils.getDayWeekDateStr(timePeriod.getDateTo()));
                    return periodLbl;
                case VICKET_LIST:
                    return context.getString(R.string.vickets_list_lbl);
                default:
                    return context.getString(R.string.unknown_event_state_lbl);
            }
        }

        public String getSelectedGroupDescription(AppContextVS context) {
            return selectedGroup.getDescription(context);
        }

        public void setSearchQuery(String searchQuery) {
            this.searchQuery = searchQuery;
        }

        public void selectItem(Integer groupPosition, Integer childPosition) {
            selectedChild = selectedGroup.getChildList().get(childPosition);
        }

        public void updateChildPosition(int childPosition) {
            selectedChild = selectedGroup.getChildList().get(childPosition);
        }

        public int getSelectedChildPosition() {
            return selectedGroup.getChildList().indexOf(selectedChild);
        }

        public int getSelectedGroupPosition() {
            return selectedGroup.getPosition();
        }

        public Drawable getLogo(AppContextVS context) {
            return context.getResources().getDrawable(selectedGroup.getLogo());
        }

        @Override public int getCount() {
            return selectedGroup.getChildList().size();
        }

    }

}