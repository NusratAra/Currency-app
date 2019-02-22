package com.example.currency;


import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
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

import java.io.IOException;
import java.io.InputStream;
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
    private EditText currency;
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


    public HomeFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        countryName = view.findViewById(R.id.country_name);
        currency = view.findViewById(R.id.currency);
        radioGroupTex = view.findViewById(R.id.radio_group_tex);
        texAmount = view.findViewById(R.id.tex_amount);
        totalAmount = view.findViewById(R.id.total_amount);

        countryArray = new ArrayList<>();
        detailsPairArray = new ArrayList<>();
        mQueue = Volley.newRequestQueue(getContext());

//        loadJSONFromAsset();

        //load local assets when failed to load data from api
        if(jsonArray == null){
            loadJSONFromAssetLocal();
        }
        setValues();
        return view;
    }


    private void loadJSONFromAssetLocal() {
        String json = null;
        try {
            InputStream is = this.getActivity().getAssets().open("vat.json");

            int size = is.available();

            byte[] buffer = new byte[size];

            is.read(buffer);

            is.close();

            json = new String(buffer, "UTF-8");
            JSONObject obj = new JSONObject(json);
            jsonArray = obj.getJSONArray("rates");
            getJsonArray(jsonArray);

            Log.d(TAG, "loadJSONFromAssetLocal: "+ obj);


        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadJSONFromAsset() {
        String url = "https://jsonvat.com/";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            jsonArray = response.getJSONArray("rates");
                            getJsonArray(jsonArray);


                        } catch (JSONException e) {
                            Log.e(TAG, "onResponse: ", e);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("TAG", "onErrorResponse: ", error);
                Toast.makeText(getContext(), "Failed to load!", Toast.LENGTH_LONG).show();
            }
        });


        Log.d(TAG, "loadJSONFromAsset: "+ jsonArray);
        mQueue.add(jsonObjectRequest);
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
                        //tex object foreach country.
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
        final ArrayList<Pair<String, Double>> rate = new ArrayList<>(arrayList);
        if(radioGroupTex != null){
            radioGroupTex.removeAllViews();
        }

        for (int row = 0; row < 1; row++) {
            RadioGroup radioGroup = new RadioGroup(getContext());
            radioGroup.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < rate.size(); i++) {
                final RadioButton rdbtn = new RadioButton(getContext());
                if(i==0){
                    rdbtn.setChecked(true);
                }
                rdbtn.setId(i);
                rdbtn.setText(""+rate.get(i).first);
                radioGroup.addView(rdbtn);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @SuppressLint("ResourceType")
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        //call calculateCurrency foreach tex type.
                        calculateCurrency(currency.getText().toString(), rate.get(checkedId).second);
                    }
                });

                //call calculateCurrency for default tex type.
                if(rdbtn.isChecked()){
                    calculateCurrency(currency.getText().toString(), rate.get(rdbtn.getId()).second);
                }

                currency.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) { }

                    @Override
                    public void afterTextChanged(Editable s) {
                        //call calculateCurrency foreach input digit.
                        if(rdbtn.isChecked()){
                            calculateCurrency(s.toString(), rate.get(rdbtn.getId()).second);
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
