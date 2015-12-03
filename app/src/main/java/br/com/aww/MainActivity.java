package br.com.aww;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mGalleryItems;
    GalleryAdapter adapter;
    private ThumbnailDownloader<GalleryHolder> mThumbnailDownloader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.grid_view);
        mGalleryItems = new ArrayList<>();
        //mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        getItems();
        adapter = new GalleryAdapter(mGalleryItems, this);
        mRecyclerView.setAdapter(adapter);
        mThumbnailDownloader = new ThumbnailDownloader<>();
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
    }
    public void getItems(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("https://www.reddit.com/r/aww.json")
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                final String json = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        mGalleryItems.clear();
                        try {
                            mGalleryItems.addAll(parseItems(json));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("BEHS", e.getMessage());
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter.notifyDataSetChanged();

                            }
                        });
                    } catch (Exception e) {
                        Log.e("porra", e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private List<GalleryItem> parseItems(String json) throws JSONException {
        JSONObject jsonMovie = new JSONObject(json);
        JSONObject firstData = jsonMovie.getJSONObject("data");
        JSONArray children = firstData.getJSONArray("children");

        List<GalleryItem> items = new ArrayList();

        for(int i = 0; i < children.length(); i++ ){
            JSONObject jsonObject = children.getJSONObject(i);
            JSONObject childrenObject = jsonObject.getJSONObject("data");
            GalleryItem item = new GalleryItem();
            item.setTitle(childrenObject.getString("title"));
            item.setUrl(childrenObject.getString("url"));
            if(!childrenObject.getString("url").endsWith(".jpg")){
                continue;
            }else{
                items.add(item);
            }
        }


        return items;
    }

    private class GalleryHolder extends RecyclerView.ViewHolder{
        private ImageView mImageView;
        private TextView mTextView;

        public GalleryHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.country_photo);
            mTextView = (TextView) itemView.findViewById(R.id.country_name);
        }
        public void bindImage(GalleryItem item ){
            Picasso.with(MainActivity.this).load(item.getUrl()).into(mImageView);
        }

    }

    private class GalleryAdapter extends RecyclerView.Adapter<GalleryHolder>{
        private List<GalleryItem> mImages;
        private Context mContext;

        public GalleryAdapter(List<GalleryItem> images, Context context){
            mContext = context;
            mImages = images;
        }

        @Override
        public GalleryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
            View view = inflater.inflate(R.layout.list_item, parent, false);
            return new GalleryHolder(view);

        }

        @Override
        public void onBindViewHolder(GalleryHolder holder, int position) {
            holder.mTextView.setText(mImages.get(position).getTitle());
            holder.bindImage(mImages.get(position));



        }

        @Override
        public int getItemCount() {
            return mImages.size();
        }
    }
}
