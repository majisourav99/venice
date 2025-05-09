package com.linkedin.venice.meta;

import com.linkedin.venice.VeniceResource;
import com.linkedin.venice.exceptions.VeniceNoStoreException;
import com.linkedin.venice.utils.Utils;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Interface defined readonly operations to access stores.
 */
public interface ReadOnlyStoreRepository extends VeniceResource {
  /**
   * Get one store by given name from repository.
   *
   * @param storeName name of wanted store.
   *
   * @return Store for given name.
   */
  Store getStore(String storeName);

  Store getStoreOrThrow(String storeName) throws VeniceNoStoreException;

  /**
   * Wait for a specified store/version to appear in the Store Repository and retrieve them.
   *
   * @param storeName       Store name to wait for.
   * @param versionNumber   Version number to wait for.
   * @param timeout         Maximum wait time allowed before giving up.
   * @return (store, version) pair on success.
   *         (store, null) if store exists, but version still isn't after waiting for allowed time.
   *         (null, null) if store still doesn't exit after waiting for allowed time.
   */
  default StoreVersionInfo waitVersion(String storeName, int versionNumber, Duration timeout) {
    return waitVersion(storeName, versionNumber, timeout, TimeUnit.SECONDS.toMillis(1));
  }

  default StoreVersionInfo waitVersion(String storeName, int versionNumber, Duration timeout, long delayMs) {
    long expirationTime = System.currentTimeMillis() + timeout.toMillis() - delayMs;
    Store store = getStore(storeName);
    for (;;) {
      if (store != null) {
        Version version = store.getVersion(versionNumber);
        if (version != null) {
          return new StoreVersionInfo(store, version);
        }
      }
      if (expirationTime < System.currentTimeMillis() || !Utils.sleep(delayMs)) {
        return new StoreVersionInfo(store, null);
      }
      store = refreshOneStore(storeName);
    }
  }

  /**
   * Whether the store exists or not.
   *
   * @param storeName store name
   * @return
   */
  boolean hasStore(String storeName);

  /**
   * Selective refresh operation which fetches one store from ZK
   *
   * @param storeName store name
   * @return the newly refreshed store
   */
  Store refreshOneStore(String storeName);

  /**
   * Get all stores in the current repository
   * @return
   */
  List<Store> getAllStores();

  /**
   * Get total read quota of all stores.
   */
  long getTotalStoreReadQuota();

  /**
   * Register store data change listener.
   *
   * @param listener
   */
  void registerStoreDataChangedListener(StoreDataChangedListener listener);

  /**
   * Unregister store data change listener.
   * @param listener
   */
  void unregisterStoreDataChangedListener(StoreDataChangedListener listener);

  /**
   * Get batch-get limit for the specified store
   * @param storeName
   * @return
   */
  int getBatchGetLimit(String storeName);

  /**
   * Whether computation is enabled for the specified store.
   * @param storeName store name
   * @return
   */
  boolean isReadComputationEnabled(String storeName);
}
