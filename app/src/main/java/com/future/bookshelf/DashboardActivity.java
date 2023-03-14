package com.future.bookshelf;

import static com.future.bookshelf.util.ExtraConstants.EXTRA_REPLY_BOOK;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.util.ExtraConstants;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.future.bookshelf.adapter.BookRecyclerAdapter;
import com.future.bookshelf.databinding.ActivityDashboardBinding;
import com.future.bookshelf.model.Book;
import com.future.bookshelf.repository.FirestoreRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding mBinding;
    private FirebaseFirestore mDatabase;
    private BookRecyclerAdapter mAdapter;
    private FirestoreRepository<Book> mFirestoreRepository;

    @SuppressLint("RestrictedApi")
    @NonNull
    public static Intent createIntent(@NonNull Context context, @Nullable IdpResponse response) {
        return new Intent().setClass(context, DashboardActivity.class)
                .putExtra(ExtraConstants.IDP_RESPONSE, response);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mDatabase = FirebaseFirestore.getInstance();
        mFirestoreRepository = new FirestoreRepository(Book.class, "books");

        mAdapter = setUpAdapter(mDatabase);
        setUpRecyclerView(mBinding.recyclerView, mAdapter);

        mBinding.fabAdd.setOnClickListener(view -> launchBookActivity(null));

        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getBindingAdapterPosition();
                        Book book = (Book) mAdapter.getItem(position);
                        Toast.makeText(DashboardActivity.this, "Deleting " +
                                book.getIsbn(), Toast.LENGTH_LONG).show();

                        mFirestoreRepository.delete(book.getIsbn());
                    }
                });

        helper.attachToRecyclerView(mBinding.recyclerView);

        mAdapter.setOnItemClickListener((v, position) -> {
            Book book = (Book) mAdapter.getItem(position);
            launchBookActivity(book);
        });
    }

    private void setUpRecyclerView(RecyclerView rv, FirestoreRecyclerAdapter adapter) {
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }

    private BookRecyclerAdapter setUpAdapter(FirebaseFirestore db) {
        Query query = db.collection("books").orderBy("title").limit(100);
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder()
                .setQuery(query, Book.class)
                .setLifecycleOwner(this)
                .build();

        return new BookRecyclerAdapter(this, options);
    }

    ActivityResultLauncher<Intent> mBookActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        Book book = (Book) intent.getSerializableExtra(EXTRA_REPLY_BOOK);
                        mFirestoreRepository.createOrUpdate(book);
                        mAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(
                                DashboardActivity.this, R.string.empty_not_saved, Toast.LENGTH_LONG).show();
                    }
                }
            });

    private void launchBookActivity(Book book) {
        Intent intent = new Intent(this, BookActivity.class);
        if (book != null) {
            intent.putExtra(com.future.bookshelf.util.ExtraConstants.EXTRA_DATA_BOOK, book);
        }

        mBookActivityLauncher.launch(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.actionSearch);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterBooks(newText);
                return false;
            }
        });
        return true;
    }

    private void filterBooks(String text) {
        Query query = mDatabase.collection("books");
        if (!TextUtils.isEmpty(text))
            query = query.whereEqualTo("isbn", text);
        query = query.orderBy("title").limit(100);
        FirestoreRecyclerOptions options = new FirestoreRecyclerOptions.Builder()
                .setQuery(query, Book.class)
                .setLifecycleOwner(this)
                .build();
        mAdapter.updateOptions(options);
    }
}