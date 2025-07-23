package vn.edu.fpt.musicplayer;

import android.content.ContentUris;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import vn.edu.fpt.musicplayer.databinding.ItemSongBinding;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewholder> { // Adapter cho RecyclerView để hiển thị danh sách bài hát

    private final List<Song> songs; // Danh sách các bài hát
    private final OnItemClickerListerner listerner;

    // Interface để xử lý sự kiện khi người dùng click vào một bài hát
    public interface OnItemClickerListerner{
        void OnClick(int postion); // Trả về vị trí bài hát được click
    }

    // Constructor khởi tạo Adapter với danh sách bài hát và listener
    public SongAdapter(List<Song> songs, OnItemClickerListerner listerner) {

        this.songs = songs;
        this.listerner = listerner;
    }

    // Hàm tạo ViewHolder, được gọi khi cần tạo một item view mới
    @NonNull
    @Override
    public SongAdapter.SongViewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_song.xml bằng ViewBinding
        ItemSongBinding binding=ItemSongBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new SongViewholder(binding,listerner);
    }

    // Hàm gán dữ liệu cho ViewHolder tại vị trí được chỉ định
    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewholder holder, int position) {
        Song song = songs.get(position); // Lấy bài hát tại vị trí

        // Gán tiêu đề bài nhạc và tên nghệ sĩ vào TextView
        holder.binding.textTitle.setText(song.title);
        holder.binding.textArtist.setText(song.artist);

        // Tạo URI để lấy ảnh album từ MediaStore
        Uri albumArtUri = ContentUris.withAppendedId(Uri.parse("conent://media/external/audio/albumart"), song.albumId);

        // Dùng Glide để load ảnh album vào ImageView
        Glide.with(holder.binding.getRoot().getContext())
                .load(albumArtUri)
                .circleCrop()
                .placeholder(R.drawable.ic_music_note)
                .error(R.drawable.ic_music_note)
                .into(holder.binding.imageAlbumArt);


    }

    // Trả về số lượng bài hát trong danh sách
    @Override
    public int getItemCount() {
        return songs.size();
    }

    // ViewHolder để giữ view của từng item trong RecyclerView
    public class SongViewholder extends RecyclerView.ViewHolder {
        final ItemSongBinding binding;
        final OnItemClickerListerner listener;

        public SongViewholder(ItemSongBinding binding,OnItemClickerListerner listerner) {
            super(binding.getRoot());
            this.binding=binding;
            this.listener=listerner;

            // Thiết lập sự kiện click cho toàn bộ item
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listerner != null){
                        int pos = getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            listener.OnClick(pos);
                        }
                    }
                }
            });
        }
    }
}
