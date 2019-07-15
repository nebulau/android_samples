package com.example.chapter3.homework;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;

import java.util.List;

public class PlaceholderFragment extends Fragment {

    private LottieAnimationView animationView;
    private ListView friendsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO ex3-3: 修改 fragment_placeholder，添加 loading 控件和列表视图控件
        View view = inflater.inflate(R.layout.fragment_placeholder, container, false);
        animationView = view.findViewById(R.id.animation_friends_view);
        friendsList = view.findViewById(R.id.lvFriends);
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里会在 5s 后执行
                // TODO ex3-4：实现动画，将 lottie 控件淡出，列表数据淡入
                animationView.animate()
                        .alpha(0f)
                        .setDuration(1000)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animationView.setVisibility(View.GONE);
                            }
                        });

                String data[] = {"Apple", "Monkey", "Cherry","Orange","Banana","NA"};
                friendsList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, data));

                ObjectAnimator animator4 = ObjectAnimator.ofFloat(friendsList, "alpha", 0, 1);
                animator4.setDuration(1000);
                animator4.setRepeatCount(ObjectAnimator.INFINITE);
                animator4.setRepeatMode(ObjectAnimator.REVERSE);



            }
        }, 5000);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 这里会在 5s 后执行
                // TODO ex3-4：实现动画，将 lottie 控件淡出，列表数据淡入
                animationView.animate()
                        .alpha(0f)
                        .setDuration(1000)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                animationView.setVisibility(View.GONE);
                            }
                        });

                String data[] = {"Apple", "Monkey", "Cherry","Orange","Banana","NA","Apple", "Monkey", "Cherry","Orange","Banana","NA","Apple", "Monkey", "Cherry","Orange","Banana","NA"};
                friendsList.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, data));

//                ObjectAnimator animator4 = ObjectAnimator.ofFloat(friendsList, "alpha", 0, 1);
//                animator4.setDuration(1000);
//                animator4.setRepeatCount(ObjectAnimator.INFINITE);
//                animator4.setRepeatMode(ObjectAnimator.REVERSE);
                friendsList.animate()
                        .alpha(1f)
                        .setDuration(3000)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                animationView.setVisibility(View.VISIBLE);
                            }
                        });



            }
        }, 5000);
    }
}


/*
* // 在代码中实现列表动画
Animation animation = (Animation) AnimationUtils.loadAnimation(
mContext, R.anim.list_anim);
LayoutAnimationController lac = new LayoutAnimationController(animation);
lac.setDelay(0.4f); //设置动画间隔时间
lac.setOrder(LayoutAnimationController.ORDER_NORMAL); //设置列表的显示顺序
mListView.setLayoutAnimation(lac); //为ListView 添加动画
* */
