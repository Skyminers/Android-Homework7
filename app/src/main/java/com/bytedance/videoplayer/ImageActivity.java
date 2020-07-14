package com.bytedance.videoplayer;

import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

public class ImageActivity extends AppCompatActivity {
    private ImageView imageView;
    private EditText editText;
    private Button button;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setTitle("Image");
        imageView = findViewById(R.id.image);
        editText = findViewById(R.id.text);
        button = findViewById(R.id.submit);

        button.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                String str = editText.getText().toString();

                RequestOptions cropOptions = RequestOptions.bitmapTransform(new RoundedCorners(50));
                Glide.with(ImageActivity.this)
                        .load(str)
                        .apply(cropOptions)
                        .placeholder(R.drawable.ic_loading)
                        .error(R.drawable.ic_reeor_fill)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageView);
            }
        });
    }
}
