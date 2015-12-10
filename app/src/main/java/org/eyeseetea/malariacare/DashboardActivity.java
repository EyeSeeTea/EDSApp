/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.LocalActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.utils.Session;
import org.eyeseetea.malariacare.fragments.DashboardSentFragment;
import org.eyeseetea.malariacare.fragments.DashboardUnsentFragment;
import org.eyeseetea.malariacare.fragments.MonitorFragment;
import org.eyeseetea.malariacare.services.SurveyService;
import org.hisp.dhis.android.sdk.events.UiEvent;

import java.io.IOException;
import java.util.List;


public class DashboardActivity extends BaseActivity {

    private final static String TAG=".DDetailsActivity";
    private boolean reloadOnResume=true;
    TabHost tabHost;
    LocalActivityManager mlam;
    MonitorFragment monitorFragment;
    DashboardUnsentFragment unsentFragment;
    DashboardSentFragment sentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_dashboard);
        try {
            initDataIfRequired();
            loadSessionIfRequired();
        } catch (IOException e){
            Log.e(".DashboardActivity", e.getMessage());
        }
        if(savedInstanceState==null) {
            initImprove();
            initAssess();
            initMonitor();
        }
        //Tabs
        /* TabHost will have Tabs */
        mlam = new LocalActivityManager(this, false);
        tabHost = (TabHost)findViewById(R.id.tabHost);
        mlam.dispatchCreate(savedInstanceState);
        tabHost.setup(mlam );
        /* TabSpec used to create a new tab.
        * By using TabSpec only we can able to setContent to the tab.
        * By using TabSpec setIndicator() we can set name to tab. */

        TabHost.TabSpec tab_plan = tabHost.newTabSpec("tab_plan");
        TabHost.TabSpec tab_monitor= tabHost.newTabSpec("tab_monitor");
        TabHost.TabSpec tab_assess = tabHost.newTabSpec("tab_assess");
        TabHost.TabSpec tab_improve= tabHost.newTabSpec("tab_improve");

        /* TabSpec setContent() is used to set content for a particular tab. It can be a R.id.layout or a .Class */

        tab_plan.setContent(R.id.tab_plan_layout);
        tab_plan.setIndicator("", getResources().getDrawable(R.drawable.tab_plan));

        tab_monitor.setContent(R.id.tab_monitor_layout);
        tab_monitor.setIndicator("", getResources().getDrawable(R.drawable.tab_monitor));


        tab_assess.setContent(R.id.tab_assess_layout);
        tab_assess.setIndicator("", getResources().getDrawable(R.drawable.tab_assess));

        tab_improve.setContent(R.id.tab_improve_layout);
        tab_improve.setIndicator("", getResources().getDrawable(R.drawable.tab_improve));

        /* Add tabSpec to the TabHost to display. */
        tabHost.addTab(tab_plan);
        tabHost.addTab(tab_assess);
        tabHost.addTab(tab_improve);
        tabHost.addTab(tab_monitor);

        /** Defining Tab Change Listener event. This is invoked when tab is changed */
        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {

                /** If current tab is android */
                if(tabId.equalsIgnoreCase("tab_improve")){
                    unsentFragment.reloadUnsentSurveys();
                }else if(tabId.equalsIgnoreCase("tab_assess")){
                    sentFragment.reloadSentSurveys();
                }else if(tabId.equalsIgnoreCase("tab_plan")){
                    //tab_plan on click code
                }else if(tabId.equalsIgnoreCase("tab_monitor")){
                        monitorFragment.reloadSentSurveys();
                }
            }
        };
        tabHost.setOnTabChangedListener(tabChangeListener);
        setTitle(getString(R.string.app_name) + " app - " + Session.getUser().getName());
        android.support.v7.app.ActionBar actionBar =  getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.action_bar_title_layout);
        ((TextView) findViewById(R.id.action_bar_title)).setText(getString(R.string.app_name));
        ((TextView) findViewById(R.id.action_bar_subtitle)).setText(Session.getUser().getName() +"\n"+"line3"+"\n"+"line5");
    }
    public void initImprove(){
        unsentFragment = new DashboardUnsentFragment();
        unsentFragment.setArguments(getIntent().getExtras());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.dashboard_details_container, unsentFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }
    public void initAssess(){
        sentFragment = new DashboardSentFragment();
        unsentFragment.setArguments(getIntent().getExtras());
        FragmentTransaction ftr = getFragmentManager().beginTransaction();
        ftr.add(R.id.dashboard_completed_container, sentFragment);
        ftr.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ftr.commit();
    }
    public void initMonitor(){
        monitorFragment = new MonitorFragment();
        monitorFragment.setArguments(getIntent().getExtras());
        FragmentTransaction ftm = getFragmentManager().beginTransaction();
        ftm.add(R.id.dashboard_charts_container, monitorFragment);
        ftm.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ftm.commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Any common option
        if(item.getItemId()!=R.id.action_pull){
            return super.onOptionsItemSelected(item);
        }

        //Pull
        final List<Survey> unsentSurveys = Survey.getAllUnsentSurveys();

        //No unsent data -> pull (no confirmation)
        if(unsentSurveys==null || unsentSurveys.size()==0){
            pullMetadata();
            return true;
        }

        //Unsent data -> ask if pull || push before pulling
        final Activity activity = this;
        new AlertDialog.Builder(this)
                .setTitle("Push unsent surveys?")
                .setMessage("Metadata refresh will delete your unsent data. You have "+unsentSurveys.size()+" unsent surveys. Do you to push them before refresh?")
                .setNeutralButton(android.R.string.no, null)
                .setNegativeButton(activity.getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Pull directly
                        pullMetadata();
                    }
                })
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Try to push before pull
                        pushUnsentBeforePull();
                    }
                })
                .setCancelable(true)
                .create().show();
        return true;
    }

    private void pushUnsentBeforePull() {

        //Launch Progress Push before pull
        Intent progressActivityIntent = new Intent(this, ProgressActivity.class);
        progressActivityIntent.putExtra(ProgressActivity.TYPE_OF_ACTION,ProgressActivity.ACTION_PUSH_BEFORE_PULL);
        finish();
        startActivity(progressActivityIntent);
    }

    private void pullMetadata(){
        finishAndGo(ProgressActivity.class);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void initTransition(){
        this.overridePendingTransition(R.transition.anim_slide_in_right, R.transition.anim_slide_out_right);
    }

    @Override
    public void onResume(){
        Log.d(TAG, "onResume");
        super.onResume();
        getSurveysFromService();
        mlam.dispatchResume();
    }

    @Override
    public void onPause(){
        Log.d(TAG, "onPause");
        super.onPause();
        mlam.dispatchPause(isFinishing());
    }

    public void setReloadOnResume(boolean doReload){
        this.reloadOnResume=false;
    }

    public void getSurveysFromService(){
        Log.d(TAG, "getSurveysFromService ("+reloadOnResume+")");
        if(!reloadOnResume){
            //Flag is readjusted
            reloadOnResume=true;
            return;
        }
        Intent surveysIntent=new Intent(this, SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.RELOAD_DASHBOARD_ACTION);
        this.startService(surveysIntent);
    }

    /**
     * Just to avoid trying to navigate back from the dashboard. There's no parent activity here
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, "back pressed");
        new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit the app?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                }).create().show();
    }

    /**
     * PUll data from DHIS server and turn into our model
     * @throws IOException
     */
    private void initDataIfRequired() throws IOException {
//        PullController.getInstance().pull(this);
    }

    /**
     * In case Session doesn't have the user set, here we set it to the first entry of User table
     */
    private void loadSessionIfRequired(){
        // already a user in session -> done
        if(Session.getUser()!=null){
            return;
        }

        // If we're in dashboard and User is not yet in session we have to put it
        // FIXME: for the moment there will be only one user in the User table, but in the future we will have to think about tagging the logged user in the DB
        User user = User.getLoggedUser();
        Session.setUser(user);
    }

    /**
     * Logging out from sdk is an async method.
     * Thus it is required a callback to finish logout gracefully.
     *
     * XXX: So far this @subscribe annotation does not work with inheritance since relies on 'getDeclaredMethods'
     * @param uiEvent
     */
    @Subscribe
    public void onLogoutFinished(UiEvent uiEvent){
        super.onLogoutFinished(uiEvent);
    }
}
