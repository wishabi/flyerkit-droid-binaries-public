package com.flipp.flyerkitsample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.flipp.flyerkitsample.databinding.ActivityStoreListBinding;
import com.flipp.flyerkitsample.databinding.StoreListItemBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreListingActivity extends AppCompatActivity {
    private ActivityStoreListBinding mBinding;
    private RequestQueue mRequestQueue;
    private FlyerKitApplication mgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestQueue = Volley.newRequestQueue(this);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_store_list);
        mgv = (FlyerKitApplication) getApplicationContext();

        // create store list URL
        String url = mgv.rootUrl + "flyerkit/" + mgv.apiVersion + "/stores/" +
                mgv.merchantIdentifier + "?postal_code=" + mgv.postalCode +
                "&access_token=" + mgv.accessToken;
        Log.i("StoreListingActivity", "Store Listing URL: " + url);

        // request store listing
        JsonArrayRequest request =
                new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // create store array
                        final List<JSONObject> stores = new ArrayList<>();
                        for (int i = 0, n = response.length(); i < n; ++i) {
                            try {
                                stores.add(response.getJSONObject(i));
                            } catch (JSONException e) {
                                // Skip.
                            }
                        }

                        mBinding.listView.setAdapter(new ArrayAdapter<JSONObject>(
                                StoreListingActivity.this, R.layout.flyer_list_item, stores) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                JSONObject store = stores.get(position);

                                @SuppressLint("ViewHolder")
                                StoreListItemBinding binding = StoreListItemBinding.inflate(
                                        LayoutInflater.from(
                                                StoreListingActivity.this), parent, false);
                                try {
                                    binding.setName(store.getString("name"));
                                    binding.executePendingBindings();
                                } catch (JSONException ignored) {
                                }

                                return binding.getRoot();
                            }
                        });

                        mBinding.listView.setOnItemClickListener(
                                new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                Intent intent = new Intent(StoreListingActivity.this,
                                        FlyerListingActivity.class);
                                JSONObject store = (JSONObject) parent.getItemAtPosition(position);
                                if (store == null)
                                    return;

                                try {
                                    mgv.setStoreCode(store.getString("merchant_store_code"));
                                    startActivity(intent);
                                } catch (JSONException ignored) {
                                }
                            }
                        });
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("StoreListingActivity", "Network error " + error.toString());

                    }
                });
        mRequestQueue.add(request);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
