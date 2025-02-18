package com.flipp.flyerkitsample;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.flipp.flyerkit.FlyerView;
import com.flipp.flyerkitsample.databinding.ActivityFlyerBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FlyerActivity extends AppCompatActivity {

  private RequestQueue mRequestQueue;
  private JSONArray mFlyerItems;
  private JSONArray mFlyerPages;
  private List<FlyerView.BadgeAnnotation> mCouponBadges;
  private ActivityFlyerBinding mBinding;
  private FlyerKitApplication mApp;

  private class ItemAnnotation implements FlyerView.TapAnnotation {
    private final RectF mRect;
    private final JSONObject mObject;

    public ItemAnnotation(RectF rect, JSONObject object) {
      mRect = rect;
      mObject = object;
    }

    @Override
    public RectF getTapRect() {
      return mRect;
    }

    public JSONObject getFlyerItem() {
      return mObject;
    }
  }

  private class CouponAnnotation implements FlyerView.BadgeAnnotation {
    private RectF mRect;
    private Drawable mBadge;

    CouponAnnotation(float left, float top, float width, boolean isClipped) {
      mBadge = isClipped ? getResources().getDrawable(R.drawable.coupon_badge_clipped)
          : getResources().getDrawable(R.drawable.coupon_badge);

      int halfWidth = mBadge.getIntrinsicWidth() / 2;
      int halfHeight = mBadge.getIntrinsicHeight() / 2;

      // badge rect is defaulted to the top right of the item
      mRect = new RectF(
          left + width - halfWidth,
          top  - halfHeight,
          left + width + halfWidth,
          top + halfHeight);
    }

    @Override
    public Drawable getBadgeDrawable() {
      return mBadge;
    }

    @Override
    public RectF getBadgeRect() {
      return mRect;
    }

    @Override
    public boolean isZoomIndependent() {
      // coupon badges are large when zoomed out of the flyer, but as you zoom in they should
      // scale and become smaller - making the mRect smaller in the process
      return false;
    }
  }

  private void updateBadges() {
    List<FlyerView.BadgeAnnotation> badgeAnnotations = new ArrayList<>();

    for (JSONObject item : ClippingsManager.getInstance().getFlyerClippings().values()) {
      try {
        float left = (float)item.getDouble("left");
        float top = (float)item.getDouble("top");
        float width = (float)item.getDouble("width");
        float height = (float)item.getDouble("height");
        final RectF rect = new RectF(left, top, left + width, top + height);
        final Drawable drawable = ContextCompat.getDrawable(this, R.drawable.badge);
        FlyerView.BadgeAnnotation annotation = new FlyerView.BadgeAnnotation() {
          @Override
          public RectF getBadgeRect() {
            return rect;
          }

          @Override
          public Drawable getBadgeDrawable() {
            return drawable;
          }

          @Override
          public boolean isZoomIndependent() {
            // clippings are not scaled as they should remain "circling" the item no matter how zoomed in/out
            return true;
          }
        };
        badgeAnnotations.add(annotation);
      } catch (JSONException e) {
        // Skip item.
      }
    }

    badgeAnnotations.addAll(mCouponBadges);

    mBinding.flyerView.setBadgeAnnotations(badgeAnnotations);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mApp = (FlyerKitApplication) getApplicationContext();
    mRequestQueue = Volley.newRequestQueue(this);
    mCouponBadges = new ArrayList<>();
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_flyer);

    mBinding.flyerView.setFlyerViewListener(new FlyerView.FlyerViewListener() {
      @Override
      public void onSingleTap(FlyerView flyerView,
                              FlyerView.TapAnnotation annotation,
                              int x, int y) {
        Log.e("FlyerActivity", "Single Tapped " + annotation + " at (" + x + "," + y + ")");
        if (annotation == null || !(annotation instanceof ItemAnnotation))
          return;

        ItemAnnotation tapAnnotation = (ItemAnnotation)annotation;
        JSONObject flyerItem = tapAnnotation.getFlyerItem();

        if (flyerItem == null)
          return;

        try {
          switch (flyerItem.getInt("item_type")) {
            case 3: // video
              String videoUrl = flyerItem.getString("video_url");
              if (flyerItem.getInt("video_type") == 0) {
                Intent youtubeEmbeddedIntent = new Intent(Intent.ACTION_VIEW);
                youtubeEmbeddedIntent.setData(Uri.parse(videoUrl));
                startActivity(youtubeEmbeddedIntent);
              } else {
                Intent videoIntent = new Intent(FlyerActivity.this, VideoActivity.class);
                videoIntent.putExtra("videoUrl", videoUrl);
                startActivity(videoIntent);
              }
              break;
            case 5: // external link
              String itemUrl = flyerItem.getString("web_url");
              Intent browserIntent = new Intent(Intent.ACTION_VIEW);
              browserIntent.setData(Uri.parse(itemUrl));
              startActivity(browserIntent);
              break;
            case 7: // page anchor
              int itemAnchorPageNumber = flyerItem.getInt("page_destination");
              JSONObject itemAnchorPage = mFlyerPages.getJSONObject(itemAnchorPageNumber - 1);
              double anchorPageLeftCoord = itemAnchorPage.getInt("left");
              flyerView.scrollTo((int) anchorPageLeftCoord, 0);
              break;
            case 15: // Iframe
              Intent iframeIntent = new Intent(FlyerActivity.this, IframeActivity.class);
              iframeIntent.putExtra("iframeUrl", flyerItem.getString("web_url"));
              startActivity(iframeIntent);
              break;
            case 25: //coupon
              Intent couponIntent = new Intent(FlyerActivity.this, CouponActivity.class);
              couponIntent.putExtra("couponId", flyerItem.getInt("id"));
              startActivity(couponIntent);
              break;
            default:
              Intent flyerItemIntent = new Intent(FlyerActivity.this, FlyerItemActivity.class);
              flyerItemIntent.putExtra("flyerItemId", flyerItem.getInt("id"));
              startActivity(flyerItemIntent);
          }
        } catch (JSONException ignored) {
        }
      }

      @Override
      public void onDoubleTap(FlyerView flyerView,
                              FlyerView.TapAnnotation annotation,
                              int x, int y) {
        Log.e("FlyerActivity", "Double Tapped " + annotation + " at (" + x + "," + y + ")");
        if (annotation == null || !(annotation instanceof ItemAnnotation))
          return;

        RectF visibleContent = mBinding.flyerView.getVisibleRect();
        if (Math.abs(visibleContent.height() -
            mBinding.flyerView.getContentSize().y) > 0.001f) {
          float zoomScale = mBinding.flyerView.getHeight() /
              mBinding.flyerView.getContentSize().y;
          float newWidth = mBinding.flyerView.getWidth() / zoomScale;
          float newHeight = mBinding.flyerView.getContentSize().y;
          mBinding.flyerView.zoomToRect(
              new RectF(
                  (visibleContent.left + visibleContent.width() / 2.0f -
                      newWidth / 2.0f),
                  (visibleContent.top + visibleContent.height() / 2.0f -
                      newHeight / 2.0f),
                  (visibleContent.left + visibleContent.width() / 2.0f +
                      newWidth / 2.0f),
                  (visibleContent.top + visibleContent.height() / 2.0f +
                      newHeight / 2.0f)), true);
        } else {
          mBinding.flyerView.zoomToRect(new RectF(x - 350, y - 350, x + 350, y + 350), true);
        }

      }

      @Override
      public void onLongPress(FlyerView flyerView,
                              FlyerView.TapAnnotation annotation,
                              int x, int y) {
        if (annotation != null && annotation instanceof ItemAnnotation) {
          Log.e("FlyerActivity", "Long Press " +
                  ((ItemAnnotation)annotation).getFlyerItem());

          ItemAnnotation tapAnnotation = (ItemAnnotation)annotation;
          JSONObject flyerItem = tapAnnotation.getFlyerItem();
          try {
            HashMap<Long, JSONObject> clippings =
                ClippingsManager.getInstance().getFlyerClippings();
            long itemId = flyerItem.getLong("id");
            if (clippings.containsValue(flyerItem)) {
              clippings.remove(itemId);
            } else {
              clippings.put(itemId, flyerItem);
            }
            updateBadges();
          } catch (JSONException e) {

          }
        }
      }

      @Override
      public void onScroll(FlyerView flyerView) {
        Log.e("FlyerActivity", "Flyer Scrolled To " + flyerView.getVisibleRect());
      }

      @Override
      public void onFlyerLoading(FlyerView flyerView) {
        Log.e("FlyerActivity", "Flyer Loading");
      }

      @Override
      public void onFlyerLoaded(FlyerView flyerView) {
        Log.e("FlyerActivity", "Flyer Loaded");

      }

      @Override
      public void onFlyerLoadError(final FlyerView flyerView, final Exception e) {
        Log.e("FlyerActivity", "Flyer Load Error " + e);
        if (e != null && e.getMessage() != null && !e.getMessage().isEmpty()) {
          Toast toast = Toast.makeText(
              flyerView.getContext(),
              "onFlyerLoadError() - " + e.getMessage(),
              Toast.LENGTH_LONG);
          toast.show();
        }
      }
    });

    mBinding.discountSlider.setOnSeekBarChangeListener(
        new SeekBar.OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mFlyerItems == null || progress == 0) {
              mBinding.flyerView.setHighlightAnnotations(null);
              return;
            }

            List<RectF> highlights = new ArrayList<>();
            for (int i = 0, n = mFlyerItems.length(); i < n; ++i) {
              try {
                JSONObject item = mFlyerItems.getJSONObject(i);
                int discount = item.getInt("percent_off");
                if (discount > progress) {
                  highlights.add(new RectF(item.getInt("left"), item.getInt("top"),
                      item.getInt("left") + item.getInt("width"),
                      item.getInt("top") + item.getInt("height")));
                }
              } catch (JSONException e) {
                // Skip item.
              }
            }
            mBinding.flyerView.setHighlightAnnotations(highlights);
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {
          }

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {
          }
        }
    );

    // update clipped coupons
    LoyaltyProgramCoupon.getClippedCoupons(mApp, mRequestQueue, ClippingsManager.getInstance());
  }

  @Override
  protected void onResume() {
    super.onResume();

    // get flyer id from intent
    final int flyerId = getIntent().getIntExtra("flyerId", 0);
    if (flyerId == 0)
      return;

    // send flyer id to the flyerView
    mBinding.flyerView.setFlyerId(flyerId, mApp.accessToken, mApp.rootUrl, mApp.apiVersion);

    // get the flyer items array
    String itemsUrl =
        mApp.rootUrl + "flyerkit/" + mApp.apiVersion + "/publication/" + flyerId + "/products?"
        + "access_token=" + mApp.accessToken
        + "&postal_code=" + mApp.postalCode
        + "&display_type=1,5,3,25,7,15";
    Log.i("FlyerActivity", "Flyer Products URL: " + itemsUrl);
    JsonArrayRequest itemsRequest =
        new JsonArrayRequest(itemsUrl, new Response.Listener<JSONArray>() {
          @Override
          public void onResponse(JSONArray response) {
            mFlyerItems = response;

            List<FlyerView.TapAnnotation> tapAnnotations = new ArrayList<>();
            for (int i = 0, n = mFlyerItems.length(); i < n; ++i) {
              try {

                // process the flyer item
                JSONObject item = mFlyerItems.getJSONObject(i);
                long id = item.getLong("id");
                float left = (float)item.getDouble("left");
                float top = (float)item.getDouble("top");
                float width = (float)item.getDouble("width");
                float height = (float)item.getDouble("height");
                RectF rect = new RectF(left, top, left + width, top + height);

                // check for coupon matchups
                if (item.has("coupons")) {
                  boolean isClipped = false;
                  JSONArray couponArray = item.getJSONArray("coupons");

                  // check for clipped coupons
                  for (int index = 0; index < couponArray.length(); index++) {
                    JSONObject coupon = couponArray.getJSONObject(index);
                    LoyaltyProgramCoupon lpc = new LoyaltyProgramCoupon(coupon);
                    if (ClippingsManager.getInstance().containsCoupon(lpc.getCouponId())) {
                      isClipped = true;
                      break;
                    }
                  }

                  if (couponArray.length() > 0) {
                    // create coupon badge annotation
                    CouponAnnotation couponAnnotation =
                        new CouponAnnotation(left, top, width, isClipped);
                    mCouponBadges.add(couponAnnotation);
                  }
                }
                FlyerView.TapAnnotation annotation =
                    new ItemAnnotation(rect, item);
                tapAnnotations.add(annotation);
              } catch (JSONException e) {
                // Skip item.
              }
            }
            mBinding.flyerView.setTapAnnotations(tapAnnotations);
            updateBadges();
          }
        }, new Response.ErrorListener() {

          @Override
          public void onErrorResponse(VolleyError error) {

          }
        });
    mRequestQueue.add(itemsRequest);

    // get the flyer pages array
    String pagesUrl = mApp.rootUrl + "flyerkit/" + mApp.apiVersion + "/publication/" +
            flyerId + "/pages?access_token=" + mApp.accessToken;
    Log.i("FlyerActivity", "Flyer Pages URL: " + pagesUrl);
    JsonArrayRequest pagesRequest =
            new JsonArrayRequest(pagesUrl, new Response.Listener<JSONArray>() {
              @Override
              public void onResponse(JSONArray response) {
                mFlyerPages = response;
              }
            }, new Response.ErrorListener() {

              @Override
              public void onErrorResponse(VolleyError error) {

              }
            });
    mRequestQueue.add(pagesRequest);
  }
}
