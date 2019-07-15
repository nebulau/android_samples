package com.example.myapplication.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.widget.CircleImageView;

import java.util.List;



public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.PersonViewHolder> {

    private static final String TAG = "MyAdapter";
    private int mNumberItems;

    private final ListItemClickListener mOnClickListener;
    private List<Message> list;

    private static int viewHolderCount;

    public ChatListAdapter(int numListItems, ListItemClickListener listener, List<Message> list) {
        mNumberItems = numListItems;
        mOnClickListener = listener;
        this.list = list;
        viewHolderCount = 0;
    }


    @NonNull
    @Override
    public PersonViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.chat_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        PersonViewHolder viewHolder = new PersonViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PersonViewHolder personViewHolder, int i) {
        personViewHolder.bind(i);
    }

    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    public class PersonViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView title;
        private final TextView description;
        private final TextView time;
        private final CircleImageView icon;
        private ImageView mark;

        public PersonViewHolder(@NonNull View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.video_title);
            description = (TextView) itemView.findViewById(R.id.tv_description);
            time = (TextView) itemView.findViewById(R.id.tv_time);
            icon = (CircleImageView) itemView.findViewById(R.id.iv_avatar);
            mark = (ImageView) itemView.findViewById(R.id.robot_notice);
            itemView.setOnClickListener(this);
        }

        public Bitmap stringToBitmap(String st) {
            // 将字符串转换成Bitmap类型
            // OutputStream out;
            Bitmap bitmap = null;
            try {
                // out = new FileOutputStream("/sdcard/aa.jpg");
                byte[] bitmapArray;
                bitmapArray = Base64.decode(st, Base64.DEFAULT);
                bitmap =
                        BitmapFactory.decodeByteArray(bitmapArray, 0,
                                bitmapArray.length);
                // bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                return bitmap;
            } catch (Exception e) {
                return null;
            }
        }

        @SuppressLint("WrongConstant")
        public void bind(int position) {
            title.setText(list.get(position).getTitle());
            description.setText(list.get(position).getDescription());
            time.setText(list.get(position).getTime());
            switch (list.get(position).getIcon()) {
                case "TYPE_ROBOT":
                    icon.setImageResource(R.drawable.session_robot);
                    break;
                case "TYPE_GAME":
                    icon.setImageResource(R.drawable.icon_micro_game_comment);
                    break;
                case "TYPE_SYSTEM":
                    icon.setImageResource(R.drawable.session_system_notice);
                    break;
                case "TYPE_STRANGER":
                    icon.setImageResource(R.drawable.session_stranger);
                    break;
                case "TYPE_USER":
                    icon.setImageResource(R.drawable.icon_girl);
                    break;
                default:
                    icon.setImageResource(R.drawable.session_stranger);
            }
            if (list.get(position).isOfficial()) {
                mark.setVisibility(View.VISIBLE);
            } else {
                mark.setVisibility(View.INVISIBLE);
            }

        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            if (mOnClickListener != null) {
                mOnClickListener.onListItemClick(clickedPosition);
            }
        }
    }

    public interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }
}
