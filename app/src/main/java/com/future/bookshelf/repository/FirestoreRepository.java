package com.future.bookshelf.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirestoreRepository<TEntity extends Identifiable<String>> implements Repository<TEntity, String> {

    private static final String TAG = FirestoreRepository.class.getSimpleName();

    private final Class<TEntity> entityClass;

    private final CollectionReference collectionReference;
    private final String collectionName;

    public FirestoreRepository(Class<TEntity> entityClass, String collectionName) {
        this.collectionName = collectionName;
        this.entityClass = entityClass;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.collectionReference = db.collection(this.collectionName);
    }

    @Override
    public Task<Boolean> exists(final String documentName) {
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Checking existence of '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.get().continueWith(task -> {
            Log.d(TAG, "Checking if '" + documentName + "' exists in '" + collectionName + "'.");
            return task.getResult().exists();
        });
    }

    @Override
    public Task<TEntity> get(String documentName) {
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Getting '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.get().continueWith(task -> {
            DocumentSnapshot documentSnapshot = task.getResult();
            if (documentSnapshot.exists()) {
                return documentSnapshot.toObject(entityClass);
            } else {
                Log.d(TAG, "Document '" + documentName + "' does not exist in '" + collectionName + "'.");
                return entityClass.newInstance();
            }
        });
    }

    @Override
    public Task<Void> createOrUpdate(TEntity entity) {
        final String documentName = entity.getEntityKey();
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Writing '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.set(entity).addOnSuccessListener(aVoid -> Log.d(TAG, "Document successfully written!")).addOnFailureListener(e -> Log.d(TAG, "Error writing document", e));
    }

    @Override
    public Task<Void> delete(final String documentName) {
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Deleting '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.delete().addOnSuccessListener(aVoid -> Log.d(TAG, "Document successfully deleted!")).addOnFailureListener(e -> Log.d(TAG, "Error deleting document", e));
    }
}
