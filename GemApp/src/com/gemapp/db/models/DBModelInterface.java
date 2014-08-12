package com.gemapp.db.models;

import android.content.ContentValues;

public interface DBModelInterface<T> {
    public T create(ContentValues data);
    public ContentValues convertToCV();
}
