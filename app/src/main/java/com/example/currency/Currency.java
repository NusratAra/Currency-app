package com.example.currency;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "currency")
public class Currency {

    @PrimaryKey
    private int id;


    @ColumnInfo(name = "country_name")
    public String countryName;

    @Embedded
    Tex tex;
}
