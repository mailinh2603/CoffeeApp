package com.example.coffee2.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.coffee2.Domain.Comment;
import com.example.coffee2.R;


import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private List<Comment> commentList;

    public CommentAdapter(List<Comment> commentList) {
        this.commentList = commentList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);  // đảm bảo bạn có file item_comment.xml
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        String userName = comment.getUserName();
        Log.d("Adapter", "Hiển thị comment của: " + userName);
        holder.userNameTextView.setText(comment.getUserName());
        holder.commentTextView.setText(comment.getCommentDetail());
        holder.ratingBar.setRating(comment.getRating());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        TextView commentTextView;
        RatingBar ratingBar;

        public CommentViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.tvUserName);
            commentTextView = itemView.findViewById(R.id.tvComment);
            ratingBar = itemView.findViewById(R.id.ratingBar);
        }
    }
}
