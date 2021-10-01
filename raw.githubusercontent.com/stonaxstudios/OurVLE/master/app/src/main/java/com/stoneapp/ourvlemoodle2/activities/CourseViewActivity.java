/*
 * Copyright 2016 Matthew Stone and Romario Maxwell.
 *
 * This file is part of OurVLE.
 *
 * OurVLE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OurVLE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OurVLE.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stoneapp.ourvlemoodle2.activities;

import java.util.List;

import com.activeandroid.query.Select;
import com.stoneapp.ourvlemoodle2.activities.settings.SettingsActivity;
import com.stoneapp.ourvlemoodle2.fragments.CourseContentFragment;
import com.stoneapp.ourvlemoodle2.fragments.EventFragment;
import com.stoneapp.ourvlemoodle2.fragments.ForumFragment;
import com.stoneapp.ourvlemoodle2.fragments.MembersFragment;

import com.stoneapp.ourvlemoodle2.models.SiteInfo;

import com.stoneapp.ourvlemoodle2.R;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

@SuppressWarnings("FieldCanBeLocal")
public class CourseViewActivity extends AppCompatActivity
        implements SearchView.OnQueryTextListener {
    private Bundle extras;
    private String coursefname;
    private String coursename;
    private Long coursepid;
    private int  courseid;
    private String token;
    private MenuItem searchitem;
    private String name;
    private int userid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        extras = getIntent().getExtras();
        coursefname = extras.getString("coursefname");
        coursename = extras.getString("coursename");
        coursepid = extras.getLong("coursepid");
        courseid = extras.getInt("courseid");

        setContentView(R.layout.activity_courseview);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.pager);

        setSupportActionBar(toolbar);
        ActionBar abar = getSupportActionBar();
        if (abar != null) {
            abar.setDisplayHomeAsUpEnabled(true);
            abar.setTitle(coursename);
            abar.getThemedContext();
        }

        // first tab inserted so it is set as currently selected by default

        Drawable  contents = AppCompatDrawableManager.get().getDrawable(this,R.drawable.ic_tab_contents );
        Drawable  forum = AppCompatDrawableManager.get().getDrawable(this,R.drawable.ic_tab_forum );
        Drawable  event = AppCompatDrawableManager.get().getDrawable(this,R.drawable.ic_tab_events );
        Drawable  member = AppCompatDrawableManager.get().getDrawable(this,R.drawable.ic_tab_members );

        tabLayout.addTab(tabLayout.newTab().
                setIcon(contents));

        tabLayout.addTab(tabLayout.newTab().setIcon(forum));

        tabLayout.addTab(tabLayout.newTab().setIcon(event));

        tabLayout.addTab(tabLayout.newTab().setIcon(member));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        viewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        List<SiteInfo> sites = new Select().all().from(SiteInfo.class).execute();
        token = sites.get(0).getToken();
        name = sites.get(0).getFullname();
        userid = sites.get(0).getUserid();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_coursedetail, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            searchitem = menu.findItem(R.id.action_search);
            searchView = (SearchView) MenuItemCompat.getActionView(searchitem);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                onSearchRequested();
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        searchView.setQuery("", false); // clear search view
    }

    @Override
    public boolean onQueryTextSubmit(String query) { return false; }

    @Override
    public boolean onQueryTextChange(String newText) { return false; }

    private class TabsPagerAdapter extends FragmentPagerAdapter {
        public TabsPagerAdapter(FragmentManager fm) { super(fm); }

        @Override
        public Fragment getItem(int index) {
            switch (index) {
                case 0:
                    CourseContentFragment cfragment = new CourseContentFragment();
                    cfragment.setArguments(getIntent().getExtras());
                    return cfragment;

                case 1:
                    ForumFragment frag = new ForumFragment();
                    frag.setArguments(getIntent().getExtras());
                    return frag;

                case 2:
                    EventFragment evfrag = new EventFragment();
                    evfrag.setArguments(getIntent().getExtras());
                    return evfrag;

                case 3:
                    MembersFragment memFrag = new MembersFragment();
                    memFrag.setArguments(getIntent().getExtras());
                    return memFrag;

                default:
                    return null;
            }
        }

        @Override
        public int getCount() { return tabLayout.getTabCount(); }
    }

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Toolbar toolbar;
    private SearchView searchView;
}
