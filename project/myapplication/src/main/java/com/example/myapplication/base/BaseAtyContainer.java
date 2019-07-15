package com.example.myapplication.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

public class BaseAtyContainer {
    private BaseAtyContainer() {
    }

    private static BaseAtyContainer instance = new BaseAtyContainer();
    private static List<Activity> activityStack = new ArrayList<Activity>();

    public static BaseAtyContainer getInstance() {
        return instance;
    }

    public void addActivity(Activity aty) {
        activityStack.add(aty);
    }

    public void removeActivity(Activity aty) {
        activityStack.remove(aty);
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                activityStack.get(i).finish();
            }
        }
        activityStack.clear();
    }

} 
