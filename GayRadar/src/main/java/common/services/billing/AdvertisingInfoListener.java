package common.services.billing;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;

public interface AdvertisingInfoListener {

	void onInfoReceived(AdvertisingIdClient.Info info);

}