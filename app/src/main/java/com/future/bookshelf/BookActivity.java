package com.future.bookshelf;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import android.content.Intent;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.future.bookshelf.databinding.ActivityBookBinding;
import com.future.bookshelf.model.Book;
import com.future.bookshelf.util.ExtraConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BookActivity extends AppCompatActivity {
    private ActivityBookBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityBookBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            Book book = (Book) extras.getSerializable(ExtraConstants.EXTRA_DATA_BOOK);
            if (book != null) {
                if (!TextUtils.isEmpty(book.getImage())) {
                    Glide.with(this)
                            .load(decodeBitmapFromBase64(book.getImage()))
                            .placeholder(R.drawable.book_80)
                            .into(mBinding.image);
                }
                if (!TextUtils.isEmpty(book.getIsbn())) {
                    mBinding.isbn.setText(book.getIsbn());
                }
                if (!TextUtils.isEmpty(book.getTitle())) {
                    mBinding.title.setText(book.getTitle());
                }
            }
        }

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(view -> {
            Intent replyIntent = new Intent();
            if (TextUtils.isEmpty(mBinding.isbn.getText())) {
                setResult(RESULT_CANCELED, replyIntent);
            } else {
                Book book = new Book();
                book.setIsbn(mBinding.isbn.getText().toString());
                book.setTitle(mBinding.title.getText().toString());
                book.setAuthors(mBinding.authors.getText().toString());
                book.setImage(encodeBitmapToBase64(mBinding.image));
                replyIntent.putExtra(ExtraConstants.EXTRA_REPLY_BOOK, book);
                setResult(RESULT_OK, replyIntent);
            }
            finish();
        });

       mBinding.image.setOnClickListener(v -> imageChooser());
    }

    private Bitmap decodeBitmapFromBase64(String image) {
        byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    private String encodeBitmapToBase64(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
            Bitmap bitmap = bitmapDrawable.getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        }

        return "";
    }

    ActivityResultLauncher<Intent> mImageActivityLauncher
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        Bundle extras = intent.getExtras();
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        mBinding.image.setImageBitmap(imageBitmap);
                    }
                }
            });


    private void imageChooser()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mImageActivityLauncher.launch(intent);
    }
}