package com.example.myapplication;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.myapplication.base.BaseAtyContainer;
import com.example.myapplication.fragment.ChatListFragment;
import com.example.myapplication.fragment.HomeFragment;
import com.example.myapplication.fragment.RecordUploadFragment;
import com.example.myapplication.fragment.UserFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager pager = findViewById(R.id.view_pager);
//        pager .setOffscreenPageLimit(1);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int i) {
                switch (i) {
                    case 0:
                        return new HomeFragment();
                    case 1:
                        return new RecordUploadFragment();
                    case 2:
                        return new ChatListFragment();
                    case 3:
                        return new UserFragment();
                    default:
                        return new HomeFragment();
                }
            }

            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "首页";
                    case 1:
                        return "上传";
                    case 2:
                        return "消息";
                    case 3:
                        return "我的";
                    default:
                        return "首页";
                }
            }
        });
        tabLayout.setupWithViewPager(pager);

        BaseAtyContainer.getInstance().addActivity(this);
    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis()-exitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                BaseAtyContainer.getInstance().finishAllActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseAtyContainer.getInstance().removeActivity(this);
    }
}
