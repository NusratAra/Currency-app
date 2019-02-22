package com.example.currency;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;


@Database(entities = {Currency.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class CurrencyDatabase extends RoomDatabase {

    public abstract CurrencyDao currencyDao();
}
