package com.example.myapplication.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.ButterKnife;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.base.BaseAdapter;
import com.example.myapplication.base.BaseViewHolder;
import com.example.myapplication.bean.Feed;
import com.example.myapplication.bean.FeedResponse;
import com.example.myapplication.network.IMiniDouyinService;
import com.example.myapplication.network.NetCheckUtil;
import com.example.myapplication.widget.MyVideoPlayer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class HomeFragment extends Fragment {
    public static final String TAG = "HomeFragment: ";
    private View view;
    private RecyclerView rvVideos;
    private PagerSnapHelper snapHelper;
    private List<String> videoUrls;
    private List<Feed> mFeeds;
    private ListVideoAdapter videoAdapter;
    private LinearLayoutManager layoutManager;
    private static final String URL = "http://test.androidcamp.bytedance.com";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        view = inflater.inflate(R.layout.home_fragment, container, false);
        rvVideos = view.findViewById(R.id.rv_videos);
        ButterKnife.bind(getActivity());
//        initView();
        addListener();

        return view;
    }

    private void initView() {
//        if (NetCheckUtil.checkNet(getContext())) {
            new FetchTask().execute(URL);
//        } else {
//            Toast.makeText(getActivity(), "没有网络连接", Toast.LENGTH_SHORT).show();
//        }
    }

    private class FetchTask extends AsyncTask<String, Void, FeedResponse> {
        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        protected FeedResponse doInBackground(String... urls) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(urls[0])
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            Response<FeedResponse> response = null;
            try {
                response = retrofit.create(IMiniDouyinService.class).getVideo().
                        execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Log.d(TAG, "doInBackground: fetch status is successful? " + response.body().isSuccess());
            return response.body();
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(FeedResponse response) {
            if (response.isSuccess()) {
                Toast.makeText(getActivity(), "fetch suceesfully!", Toast.LENGTH_SHORT).show();
                mFeeds = response.getFeeds();
                videoUrls = new LinkedList<>();
                for (Feed feed : mFeeds) {
                    videoUrls.add(feed.getVideo_url());
                }
                snapHelper = new PagerSnapHelper();
                try {
                    snapHelper.attachToRecyclerView(rvVideos);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                videoAdapter = new ListVideoAdapter(videoUrls);
                layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
                rvVideos.setLayoutManager(layoutManager);
                rvVideos.setAdapter(videoAdapter);
            } else {
                Toast.makeText(getActivity(), "fetch fail!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addListener() {

        rvVideos.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {


            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE://停止滚动
                        View view = snapHelper.findSnapView(layoutManager);
                        JZVideoPlayer.releaseAllVideos();
                        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(view);
                        if (viewHolder != null && viewHolder instanceof VideoViewHolder) {
                            ((VideoViewHolder) viewHolder).mp_video.startVideo();
                        }

                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING://拖动
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING://惯性滑动
                        break;
                }

            }
        });
    }

    class ListVideoAdapter extends BaseAdapter<String, VideoViewHolder> {


        public ListVideoAdapter(List<String> list) {
            super(list);
        }

        @Override
        public void onHolder(VideoViewHolder holder, String bean, int position) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

            holder.mp_video.setUp(bean, JZVideoPlayerStandard.CURRENT_STATE_NORMAL);
            if (position == 0) {
                holder.mp_video.startVideo();
            }
            Glide.with(context).load(mFeeds.get(position).getImage_url()).into(holder.mp_video.thumbImageView);
            holder.video_title.setText("第" + position + "个视频");
        }

        @Override
        public VideoViewHolder onCreateHolder() {
            return new VideoViewHolder(getViewByRes(R.layout.item_page2));

        }


    }

    public class VideoViewHolder extends BaseViewHolder {
        public View rootView;
        public MyVideoPlayer mp_video;
        public TextView video_title;

        public VideoViewHolder(View rootView) {
            super(rootView);
            this.rootView = rootView;
            this.mp_video = rootView.findViewById(R.id.mp_video);
            this.video_title = rootView.findViewById(R.id.video_title);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        JZVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        JZVideoPlayer.releaseAllVideos();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
        JZVideoPlayer.releaseAllVideos();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            JZVideoPlayer.releaseAllVideos();
        } else {
            initView();
        }
    }

}
