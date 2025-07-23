package vn.edu.fpt.musicplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.musicplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickerListerner {
    private ActivityMainBinding binding; // Sử dụng View Binding để truy cập giao diện
    private RecyclerView.Adapter adapter; // Adapter cho RecyclerView hiển thị danh sách bài hát
    private List<Song> songList; // Danh sách các bài hát


    // Đăng ký launcher để xin quyền truy cập bộ nhớ
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
        if(isGranted){
            loadSong(); // Nếu được cấp quyền, tải danh sách bài hát
        } else {
            Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        EdgeToEdgeHelper.enable(this);


        // Thiết lập LayoutManager cho RecyclerView (hiển thị danh sách theo chiều dọc)
        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));

        // Kiểm tra quyền và tải bài hát
        checkPermissonAndLoadSongs();
    }

    // Hàm kiểm tra quyền truy cập và yêu cầu nếu chưa có
    private void checkPermissonAndLoadSongs() {
        String permission;
        // Với Android 13 trở lên, dùng quyền READ_MEDIA_AUDIO
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            permission= Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission= Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Nếu đã có quyền thì tải bài hát, ngược lại yêu cầu quyền
        //if(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
        //    Log.d("PERMISSION", "Permission granted: " + (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED));
        //    loadSong();
        //
        //} else {
        //    requestPermissionLauncher.launch(permission);
        //}

        if(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
            // Quét lại file nhạc
            MediaScannerConnection.scanFile(
                    this,
                    new String[]{ "/sdcard/Music/timeless.mp3" },
                    null,
                    (path, uri) -> Log.d("SCAN", "Scanned: " + path + " → " + uri)
            );

            loadSong();
        }
    }

    // Hàm lấy danh sách bài hát từ thiết bị
    private List<Song> getSongs(){
        List<Song> song = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"; // Chỉ lấy các file là nhạc
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC"; // Sắp xếp theo tiêu đề bài hát

        try(Cursor cursor =getContentResolver().query(uri,null, selection,null, sortOrder)){
            if(cursor!=null){
                // Lấy chỉ số các cột cần thiết
                int idColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);


                // Duyệt qua từng dòng dữ liệu
                while (cursor.moveToNext()){
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String data = cursor.getString(dataColumn);
                    long albumID = cursor.getLong(albumColumn);

                    // Tạo đối tượng Song và thêm vào danh sách
                    song.add(new Song(id, title, artist, data, albumID));
                }
            }
        }
        return song;
    }


    // Hàm thực hiện tải bài hát và gán vào Adapter
    private void loadSong(){
        songList = getSongs(); // Lấy danh sách bài hát

        Log.d("SONG_LIST", "Loaded songs: " + songList.size());

        if (songList.isEmpty()) {
            Toast.makeText(this, "Cannot find any song in the device!", Toast.LENGTH_SHORT).show();
        }
    // Gọi hàm này ở onCreate hoặc nơi phù hợp
        MediaScannerConnection.scanFile(
                this,  // Context
                new String[] {
                        Environment.getExternalStorageDirectory() + "/Music/timeless.mp3"
                        // Thay bằng đường dẫn tới file .mp3 bạn đã chép vào AVD
                },
                null,  // MIME type (null = tự đoán)
                (path, uri) -> Log.d("SCAN", "Scanned: " + path + " → " + uri)
        );
        // Khởi tạo adapter với danh sách bài hát và thiết lập cho RecyclerView
        adapter = new SongAdapter(songList, this);
        binding.recyclerViewSongs.setAdapter(adapter);

    }


    // Xử lý sự kiện click vào một bài hát trong danh sách
    @Override
    public void OnClick(int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
        intent.putExtra("position", position);
        startActivity(intent);
    }
}