package com.flipp.flyerkitsample;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.flipp.flyerkitsample.databinding.ActivityMainBinding;
import com.flipp.flyerkitsample.databinding.FlyerListItemBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FlyerListingActivity extends AppCompatActivity {
  private ActivityMainBinding mBinding;
  private RequestQueue mRequestQueue;
  private ImageLoader mImageLoader;
  private FlyerKitApplication mgv;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mRequestQueue = Volley.newRequestQueue(this);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
    mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
      final LruCache<String, Bitmap> cache = new LruCache<>(100);

      @Override
      public Bitmap getBitmap(String url) {
        return cache.get(url);
      }

      @Override
      public void putBitmap(String url, Bitmap bitmap) {
        cache.put(url, bitmap);
      }
    });
    mgv = (FlyerKitApplication) getApplicationContext();
  }

  @Override
  protected void onResume() {
    super.onResume();
    // create flyer listing URL
    String url = mgv.rootUrl + "flyerkit/" + mgv.apiVersion + "/publications/" +
            mgv.merchantIdentifier + "?store_code=" + mgv.storeCode + "&locale=" +
            mgv.locale + "&access_token=" + mgv.accessToken;
    Log.i("FlyerListingActivity", "Flyer Listing URL: " + url);

    // request flyer listing
    JsonArrayRequest request =
        new JsonArrayRequest(url, new Response.Listener<JSONArray>() {
          @Override
          public void onResponse(JSONArray response) {
            final List<JSONObject> flyers = new ArrayList<>();
            for (int i = 0, n = response.length(); i < n; ++i) {
              try {
                flyers.add(response.getJSONObject(i));
              } catch (JSONException e) {
                // Skip.
              }
            }

            mBinding.listView.setAdapter(new ArrayAdapter<JSONObject>(
                FlyerListingActivity.this, R.layout.flyer_list_item, flyers) {
              @Override
              public View getView(int position, View convertView, ViewGroup parent) {
                JSONObject flyer = flyers.get(position);

                @SuppressLint("ViewHolder")
                FlyerListItemBinding binding = FlyerListItemBinding.inflate(
                    LayoutInflater.from(FlyerListingActivity.this), parent, false);
                try {
                  binding.setTitle(flyer.getString("external_display_name"));
                  binding.setImageUrl(flyer.getString("thumbnail_image_url"));
                  binding.setImageLoader(mImageLoader);
                  binding.executePendingBindings();
                } catch (JSONException ignored) {
                }

                return binding.getRoot();
              }
            });

            mBinding.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
                                      int position, long id) {
                Intent intent = new Intent(FlyerListingActivity.this, FlyerActivity.class);
                JSONObject flyer = (JSONObject) parent.getItemAtPosition(position);
                if (flyer == null)
                  return;

                try {
                  intent.putExtra("flyerId", flyer.getInt("id"));
                  intent.putExtra("postalCode", mgv.postalCode);
                  intent.putExtra("accessToken", mgv.accessToken);
                  startActivity(intent);
                } catch (JSONException ignored) {
                }
              }
            });
          }
        }, new Response.ErrorListener() {
          @Override
          public void onErrorResponse(VolleyError error) {
            Log.e("FlyerListingActivity", "Network error " + error.toString());

          }
        });
    mRequestQueue.add(request);
  }

  @BindingAdapter({"imageUrl", "imageLoader"})
  public static void setImageUrl(NetworkImageView view, String url, ImageLoader imageLoader) {
    if (url != "null") {
      view.setImageUrl(url, imageLoader);
    }
  }
}
