package com.future.bookshelf.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;

public interface Repository<TEntity extends Identifiable<TKey>, TKey> {

    Task<Boolean> exists(TKey id);

    Task<TEntity> get(TKey id);

    Task<Void> createOrUpdate(TEntity entity);

    Task<Void> delete(TKey id);
}
