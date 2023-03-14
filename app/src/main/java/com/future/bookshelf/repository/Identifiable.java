package com.future.bookshelf.repository;

import com.google.firebase.firestore.Exclude;

public interface Identifiable<TKey> {

    @Exclude
    TKey getEntityKey();
}