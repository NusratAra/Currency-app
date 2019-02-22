package com.example.currency;

import android.arch.persistence.room.TypeConverter;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Converters {
    @TypeConverter
    public static ArrayList<String> fromString(String value) {
        Log.d("TAG", "fromString: "+ value);
        Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }
    @TypeConverter
    public static String fromArrayLisr(ArrayList<String> list) {
        Log.d("TAG", "fromArrayLisr: "+ list);
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}
