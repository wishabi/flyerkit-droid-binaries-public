package com.flipp.flyerkitsample;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.flipp.flyerkitsample.databinding.ActivityCouponBinding;

import org.json.JSONObject;

public class CouponActivity extends AppCompatActivity {
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private FlyerKitApplication mgv;
    private ActivityCouponBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_coupon);

        // queue for JSON and Image requests
        mRequestQueue = Volley.newRequestQueue(this);

        // image loader for coupon image
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

        // get instance of global variables
        mgv = (FlyerKitApplication) getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // get coupon id from intent
        final int couponId = getIntent().getIntExtra("couponId", 0);
        if (couponId == 0)
            return;

        // get needed views from layout
        final TextView nameLabel = (TextView) findViewById(R.id.item_name);
        final TextView saleStoryLabel = (TextView) findViewById(R.id.sale_story);
        final TextView descriptionLabel = (TextView) findViewById(R.id.description);
        final NetworkImageView imageView = (NetworkImageView) findViewById(R.id.coupon_image);

        // create coupon url
        String url = mgv.rootUrl + "flyerkit/" + mgv.apiVersion + "/product/" +
                couponId + "?access_token=" + mgv.accessToken;
        Log.i("CouponActivity", "Coupon URL: " + url);

        // request coupon data
        JsonObjectRequest request = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // put response data into the layout
                        try {
                            String couponImageUrl = response.getString("image_url");
                            String couponName = response.getString("name");
                            String couponSaleStory = response.getString("sale_story");
                            String couponDescription = response.getString("description");

                            if (!couponImageUrl.equals("null")) {
                                mBinding.setImageLoader(mImageLoader);
                                mBinding.setImageUrl(couponImageUrl);
                            } else {
                                imageView.setVisibility(View.GONE);
                            }

                            if (!couponName.equals("null")) {
                                mBinding.setName(couponName);
                            } else {
                                nameLabel.setVisibility(View.GONE);
                            }

                            if (!couponSaleStory.equals("null")) {
                                mBinding.setSaleStory(couponSaleStory);
                            } else {
                                saleStoryLabel.setVisibility(View.GONE);
                            }
                            if (!couponDescription.equals("null")) {
                                mBinding.setDescription(couponDescription);
                            } else {
                                descriptionLabel.setVisibility(View.GONE);
                            }
                        } catch(Exception e){
                            Log.e("CouponActivity", "Bad JSON!");
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("CouponActivity", "Coupon request failed!");

                    }
                });
        mRequestQueue.add(request);
    }
}
