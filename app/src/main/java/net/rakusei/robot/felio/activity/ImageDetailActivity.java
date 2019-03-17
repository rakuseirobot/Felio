package net.rakusei.robot.felio.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import net.rakusei.robot.felio.R;

public class ImageDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        ImageView imageView = findViewById(R.id.detailed_imageview);

        GlideUrl glideUrl = new GlideUrl(getIntent().getData().toString(), new LazyHeaders.Builder()
                .addHeader("authorization", "BEARER " + this.getSharedPreferences("main", Context.MODE_PRIVATE).getString("token", ""))
                .build());
        Glide.with(this).load(glideUrl).into(imageView);
    }
}
