package com.future.bookshelf.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.future.bookshelf.R;
import com.future.bookshelf.model.Book;

import java.io.IOException;

public class BookRecyclerAdapter extends FirestoreRecyclerAdapter<Book, BookRecyclerAdapter.BookViewHolder> {

    private final LayoutInflater mInflater;
    private static ClickListener clickListener;

    public BookRecyclerAdapter(Context context, FirestoreRecyclerOptions options) {
        super(options);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected void onBindViewHolder(@NonNull BookViewHolder holder, int position, @NonNull Book model) {
        if (!TextUtils.isEmpty(model.getImage())) {
            Glide.with(this.mInflater.getContext())
                    .load(decodeBitmapFromBase64(model.getImage()))
                    .placeholder(R.drawable.book_80)
                    .into(holder.imageItemView);
        }
        holder.titleItemView.setText(model.getTitle());
        holder.authorsItemView.setText(model.getAuthors());
        holder.isbnItemView.setText(model.getIsbn());
    }

    private Bitmap decodeBitmapFromBase64(String image) {
        byte[] decodedByteArray = Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.book_item, parent, false);
        return new BookViewHolder(itemView);
    }

    class BookViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageItemView;
        private final TextView authorsItemView;
        private final TextView titleItemView;
        private final TextView isbnItemView;

        private BookViewHolder(View itemView) {
            super(itemView);
            imageItemView = itemView.findViewById(R.id.image_view);
            titleItemView = itemView.findViewById(R.id.title_view);
            authorsItemView = itemView.findViewById(R.id.authors_view);
            isbnItemView = itemView.findViewById(R.id.isbn_view);

            itemView.setOnClickListener(view -> clickListener.onItemClick(view, getBindingAdapterPosition()));
        }
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        BookRecyclerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }
}

