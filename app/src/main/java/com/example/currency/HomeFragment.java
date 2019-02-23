package com.example.currency;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;



/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    public static final String TAG = HomeFragment.class.getSimpleName();

    private RequestQueue mQueue;
    private ArrayList<String> countryArray;
    private JSONArray jsonArray;

    private Spinner countryName;
    private EditText currencyEdit;
    private RadioGroup radioGroupTex;
    private String countryNameString;
    private JSONObject currencyInfo;
    private Pair<String, JSONObject> simplePair;
    private Pair<String, ArrayList<Pair<String, Double>>> detailsPair;
    private Pair<String, Double> rateDetails;
    private ArrayList<Pair<String, ArrayList<Pair<String, Double>>>> detailsPairArray;
    private ArrayList<Pair<String, Double>> rateDetailsArray;

    private TextView texAmount;
    private TextView totalAmount;

    private SaveJson saveJson;
    private String json;


    public HomeFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        countryName = view.findViewById(R.id.country_name);
        currencyEdit = view.findViewById(R.id.currency);
        radioGroupTex = view.findViewById(R.id.radio_group_tex);
        texAmount = view.findViewById(R.id.tex_amount);
        totalAmount = view.findViewById(R.id.total_amount);

        countryArray = new ArrayList<>();
        detailsPairArray = new ArrayList<>();
        mQueue = Volley.newRequestQueue(getContext());
        saveJson = new SaveJson();

        loadJSONFromAsset();

        if(json == null){
            json = saveJson.getSavedJsonInSharedPreference(getActivity());
        }
        try {
            Log.d(TAG, "onCreateView: "+ json);
            JSONObject response = new JSONObject(json);
            jsonArray = response.getJSONArray("rates");
            getJsonArray(jsonArray);
            setValues();

        } catch (JSONException e) {
            Log.e(TAG, "onCreateView: ", e);
        }
        return view;
    }


    public String loadJSONFromAsset() {
        String url = "https://jsonvat.com/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        json= response.toString();
                        saveJson.saveJsonInSharedPreference(getActivity(),response.toString());

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "onErrorResponse: ", error);
            }
        });


        Log.d(TAG, "loadJSONFromAsset: "+ jsonArray);
        mQueue.add(jsonObjectRequest);
        return json;
    }


    private void getJsonArray(JSONArray jsonArray) {
        Log.d(TAG, "onResponse: "+ jsonArray);
        try{
            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject =jsonArray.getJSONObject(i);


                //For multiple periods.
                for(int j=0; j<jsonObject.getJSONArray("periods").length(); j++){
                    JSONObject jsonObjectPeriods = jsonObject.getJSONArray("periods").getJSONObject(j).getJSONObject("rates");

                    if(j>0){
                        //Ex. Luxembourg, Romania, Greece etc.
                        simplePair = new Pair<>(jsonObject.getString("name")+"["+(j)+"]", jsonObjectPeriods);
                    } else {
                        simplePair = new Pair<>(jsonObject.getString("name"), jsonObjectPeriods);
                    }
                    countryNameString = simplePair.first;
                    currencyInfo = simplePair.second;

                    countryArray.add(countryNameString);

                    Iterator<?> keys = currencyInfo.keys();
                    rateDetailsArray = new ArrayList<>();

                    while (keys.hasNext()){
                        String ratesTypeNext = keys.next().toString();
                        //rates object foreach country.
                        rateDetails = new Pair<>(ratesTypeNext, currencyInfo.getDouble(ratesTypeNext));
                        rateDetailsArray.add(rateDetails);
                    }

                    detailsPair = new Pair<>(simplePair.first, rateDetailsArray);
                    detailsPairArray.add(detailsPair);
                }
            }
        } catch (Exception e){
            Log.e(TAG, "getJsonArray: ", e);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void addRadioButtons(ArrayList<Pair<String, Double>> arrayList){
        final ArrayList<Pair<String, Double>> newRate = new ArrayList<>(arrayList);
        if(radioGroupTex != null){
            radioGroupTex.removeAllViews();
        }

        for (int row = 0; row < 1; row++) {
            RadioGroup radioGroup = new RadioGroup(getContext());
            radioGroup.setOrientation(LinearLayout.VERTICAL);


            for (int i = 0; i < newRate.size(); i++) {
                final RadioButton rdbtn = new RadioButton(getContext());
                if(i==0){
                    rdbtn.setChecked(true);
                }
                rdbtn.setId(i);
                rdbtn.setText(newRate.get(i).first);
                radioGroup.addView(rdbtn);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        //call calculateCurrency foreach rates type.
                        calculateCurrency(currencyEdit.getText().toString(), newRate.get(checkedId).second);
                    }
                });

                //call calculateCurrency for default rates type.
                if(rdbtn.isChecked()){
                    calculateCurrency(currencyEdit.getText().toString(), newRate.get(rdbtn.getId()).second);
                }

                currencyEdit.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //call calculateCurrency foreach input digit.
                        if(rdbtn.isChecked()){
                            calculateCurrency(s.toString(), newRate.get(rdbtn.getId()).second);
                        }
                    }
                });

            }
            radioGroupTex.addView(radioGroup);


        }

    }

    private void calculateCurrency(String text, Double second) {

        try{
            double inputCurrency = Double.parseDouble(text);
            double tex = second;
            double totalTex;
            double totalSum;

            totalTex = (tex / 100) * inputCurrency;
            totalSum = inputCurrency + totalTex;
            texAmount.setText(""+ totalTex);
            totalAmount.setText(""+ totalSum);
        } catch (Exception e){
            Log.e(TAG, "calculateCurrency: ", e);
        }


    }


    private void setValues(){
        //from country
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_dropdown_item, countryArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryName.setAdapter(adapter);
        countryName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0; i<detailsPairArray.size(); i++){
                    if(parent.getItemAtPosition(position).equals(detailsPairArray.get(i).first)){
                        rateDetailsArray = new ArrayList<>();
                        rateDetailsArray = detailsPairArray.get(i).second;
                        addRadioButtons(rateDetailsArray);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

    }
}
