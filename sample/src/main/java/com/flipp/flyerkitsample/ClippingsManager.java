package com.flipp.flyerkitsample;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClippingsManager implements LoyaltyProgramCoupon.ClippedCouponsCallback {

  private static ClippingsManager sClippingsManager;

  private HashMap<Long, JSONObject> mFlyerClippings;
  private Set<Integer> mLocalCouponClippings;
  private Set<Integer> mSyncedCouponClippings;

  public static synchronized ClippingsManager getInstance() {
    if (sClippingsManager == null) {
      sClippingsManager = new ClippingsManager();
    }
    return sClippingsManager;
  }

  public HashMap<Long, JSONObject> getFlyerClippings() {
    return mFlyerClippings;
  }

  public synchronized boolean containsCoupon(int couponId) {
    return mSyncedCouponClippings.contains(couponId) || mLocalCouponClippings.contains(couponId);
  }

  public synchronized void clipCoupon(int couponId) {
    if (!mSyncedCouponClippings.contains(couponId)) {
      mLocalCouponClippings.add(couponId);
    }
  }

  public synchronized void syncCouponClippings(List<Integer> syncClips) {
    /**
     * Because of a possible delay between clipping a coupon and retrieving a list of clipped
     * coupons, we maintain 2 lists and sync them to avoid duplicates.
     */
    if (syncClips == null) {
      return;
    }
    mSyncedCouponClippings = new HashSet<>(syncClips);
    for (Integer couponId : syncClips) {
      if (mLocalCouponClippings.contains(couponId)) {
        mLocalCouponClippings.remove(couponId);
      }
    }
  }

  @Override
  public void clippedCoupons(List<Integer> coupons) {
    syncCouponClippings(coupons);
  }

  private ClippingsManager() {
    mFlyerClippings = new HashMap<>();
    mLocalCouponClippings = new HashSet<>();
    mSyncedCouponClippings = new HashSet<>();
  }
}
