package com.ahtesam.cameraxmapsfb.Gallery;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ahtesam.cameraxmapsfb.R;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    private Context context;
    private List<String> mediaUrls;

    public GalleryAdapter(Context context) {
        this.context = context;
        mediaUrls = new ArrayList<>();
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        String mediaUrl = mediaUrls.get(position);
        return mediaUrl.endsWith(".mp4") ? TYPE_VIDEO : TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;
        if (viewType == TYPE_VIDEO) {
            view = inflater.inflate(R.layout.gallery_video_item, parent, false);
            return new VideoViewHolder(view);
        } else {
            view = inflater.inflate(R.layout.gallery_image_item, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        String mediaUrl = mediaUrls.get(position);
        if (holder instanceof ImageViewHolder) {
            ((ImageViewHolder) holder).bind(mediaUrl);
        } else if (holder instanceof VideoViewHolder) {
            ((VideoViewHolder) holder).bind(mediaUrl);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }

        public void bind(String imageUrl) {
            Glide.with(context)
                    .load(imageUrl)
                    .into(imageView);
        }
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {

        VideoView videoView;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.video_view);
        }

        public void bind(String videoUrl) {
            videoView.setVideoURI(Uri.parse(videoUrl));
            videoView.start();
        }
    }
}
