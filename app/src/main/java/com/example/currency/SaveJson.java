package com.example.currency;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;

import static android.content.Context.MODE_PRIVATE;

public class SaveJson {

    String json;

    public void saveJsonInSharedPreference(FragmentActivity activity, String json) {

        this.json = json;
        SharedPreferences.Editor editor = activity.getSharedPreferences(SaveJson.class.getName(), MODE_PRIVATE).edit();
        editor.putString("key_from_json", json);
        editor.commit();
    }

    public String getSavedJsonInSharedPreference(FragmentActivity activity){
        SharedPreferences editor = activity.getSharedPreferences(SaveJson.class.getName(), MODE_PRIVATE);
        return editor.getString("key_from_json", "");
    }
}
