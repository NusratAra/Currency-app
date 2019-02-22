package com.example.currency;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "currency")
public class Tex {

    @PrimaryKey
    private int id;

    String texType;
    double texValue;

}
