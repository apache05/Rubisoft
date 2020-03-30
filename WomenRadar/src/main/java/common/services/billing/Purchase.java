/* Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package common.services.billing;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;

/**
 * Represents an in-app billing purchase.
 */
public class Purchase {
    private final String mItemType;  // ITEM_TYPE_INAPP or ITEM_TYPE_SUBS
    private final String mOrderId;
    private final String mPackageName;
    private final String mSku;
    private final long mPurchaseTime;
    private final int mPurchaseState;
    private final String mDeveloperPayload;
    private final String mToken;
    private final String mOriginalJson;
    private final String mSignature;
    private final boolean mIsAutoRenewing;

    public Purchase(String itemType, String jsonPurchaseInfo, String signature) throws JSONException {
		this.mItemType = itemType;
		this.mOriginalJson = jsonPurchaseInfo;
		JSONObject o = new JSONObject(this.mOriginalJson);
		this.mOrderId = o.optString("orderId");
		this.mPackageName = o.optString("packageName");
		this.mSku = o.optString("productId");
		this.mPurchaseTime = o.optLong("purchaseTime");
		this.mPurchaseState = o.optInt("purchaseState");
		this.mDeveloperPayload = o.optString("developerPayload");
		this.mToken = o.optString("token", o.optString("purchaseToken"));
		this.mIsAutoRenewing = o.optBoolean("autoRenewing");
		this.mSignature = signature;
	}

    public String getItemType() {
        return this.mItemType;
    }

    public String getOrderId() {
        return this.mOrderId;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getSku() {
        return this.mSku;
    }

    public long getPurchaseTime() {
        return this.mPurchaseTime;
    }

    public int getPurchaseState() {
        return this.mPurchaseState;
    }

    public String getDeveloperPayload() {
        return this.mDeveloperPayload;
    }

    public String getToken() {
        return this.mToken;
    }

    public String getOriginalJson() {
        return this.mOriginalJson;
    }

    public String getSignature() {
        return this.mSignature;
    }

    public boolean isAutoRenewing() {
        return this.mIsAutoRenewing;
    }

    @NonNull
    @Override
    public String toString() {
        return "PurchaseInfo(type:" + this.mItemType + "):" + this.mOriginalJson;
    }
}
