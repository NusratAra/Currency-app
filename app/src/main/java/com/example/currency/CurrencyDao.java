package com.example.currency;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;

@Dao
public interface CurrencyDao {

    @Insert
    public void addCurrency(Currency currency);

}

