package com.flipp.flyerkitsample;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.*;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.flipp.flyerkitsample.databinding.ActivityFlyerItemBinding;

import org.json.JSONObject;

public class FlyerItemActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private FlyerKitApplication mgv;
    private ActivityFlyerItemBinding mBinding;
    private CouponListView mCouponList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_flyer_item);

        // queue for JSON and Image requests
        mRequestQueue = Volley.newRequestQueue(this);

        // image loader for flyer item image
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

        mCouponList = (CouponListView) findViewById(R.id.coupon_list);

        // get instance of global variables
        mgv = (FlyerKitApplication) getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // get flyer item id from intent
        final int flyerItemId = getIntent().getIntExtra("flyerItemId", 0);
        if (flyerItemId == 0)
            return;

        // get needed views from layout
        final TextView nameLabel = (TextView) findViewById(R.id.item_name);
        final TextView validDatesLabel = (TextView) findViewById(R.id.valid_dates);
        final TextView saleStoryLabel = (TextView) findViewById(R.id.sale_story);
        final TextView priceLabel = (TextView) findViewById(R.id.price);
        final TextView descriptionLabel = (TextView) findViewById(R.id.description);
        final NetworkImageView imageView = (NetworkImageView) findViewById(R.id.item_image);

        // create flyer item url
        String url = mgv.rootUrl + "flyerkit/" + mgv.apiVersion + "/product/" +
                flyerItemId + "?access_token=" + mgv.accessToken + "&postal_code=" + mgv.postalCode;
        Log.i("FlyerItemActivity", "Flyer Item URL: " + url);

        // request flyer item data
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // put response data into the layout
                        try {
                            String itemValidFrom = response.getString("valid_from");
                            String itemValidTo = response.getString("valid_to");
                            String itemImageUrl = response.getString("image_url");
                            String itemName = response.getString("name");
                            String itemSaleStory = response.getString("sale_story");
                            String itemPrice = response.getString("current_price");
                            String itemDescription = response.getString("description");
                            if (!itemImageUrl.equals("null")) {
                                mBinding.setImageLoader(mImageLoader);
                                mBinding.setImageUrl(itemImageUrl);
                            } else {
                                imageView.setVisibility(View.GONE);
                            }

                            if (!itemName.equals("null")) {
                                mBinding.setName(response.getString("name"));
                            } else {
                                nameLabel.setVisibility(View.GONE);
                            }
                            if (!itemValidFrom.equals("null") && !itemValidTo.equals("null")) {
                                mBinding.setValidDates(itemValidFrom + " - " + itemValidTo);
                            } else {
                                validDatesLabel.setVisibility(View.GONE);
                            }
                            if (!itemSaleStory.equals("null")) {
                                mBinding.setSaleStory(itemSaleStory);
                            } else {
                                saleStoryLabel.setVisibility(View.GONE);
                            }
                            if (!itemPrice.equals("null")) {
                                mBinding.setPrice(itemPrice);
                            } else {
                                priceLabel.setVisibility(View.GONE);
                            }
                            if (!itemDescription.equals("null")) {
                                mBinding.setDescription(itemDescription);
                            } else {
                                descriptionLabel.setVisibility(View.GONE);
                            }

                            // display coupon information
                            if (response.has("coupons")) {
                                mCouponList.setCoupons(response.getJSONArray("coupons"));
                            }

                        } catch (Exception e){
                            Log.e("FlyerItemActivity", "Bad JSON!");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("FlyerItemActivity", "Flyer Item request failed!");
                    }
                });
        mRequestQueue.add(request);
    }
}
