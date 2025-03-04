package com.linkedin.davinci.blobtransfer;

import com.linkedin.davinci.blobtransfer.client.NettyFileTransferClient;
import com.linkedin.davinci.blobtransfer.server.P2PBlobTransferService;
import com.linkedin.davinci.stats.AggVersionedBlobTransferStats;
import com.linkedin.davinci.storage.StorageEngineRepository;
import com.linkedin.davinci.storage.StorageMetadataService;
import com.linkedin.venice.blobtransfer.DaVinciBlobFinder;
import com.linkedin.venice.blobtransfer.ServerBlobFinder;
import com.linkedin.venice.client.store.ClientConfig;
import com.linkedin.venice.helix.HelixCustomizedViewOfflinePushRepository;
import com.linkedin.venice.meta.ReadOnlyStoreRepository;
import java.util.concurrent.CompletableFuture;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class BlobTransferUtil {
  private static final Logger LOGGER = LogManager.getLogger(BlobTransferUtil.class);

  /**
   * Get a P2P blob transfer manager for DaVinci Client and start it.
   * @param p2pTransferServerPort, the port used by the P2P transfer server
   * @param p2pTransferClientPort, the port used by the P2P transfer client
   * @param baseDir, the base directory of the underlying storage
   * @param clientConfig, the client config to start up a transport client
   * @param storageMetadataService, the storage metadata service
   * @return the blob transfer manager
   * @throws Exception
   */
  public static BlobTransferManager<Void> getP2PBlobTransferManagerForDVCAndStart(
      int p2pTransferServerPort,
      int p2pTransferClientPort,
      String baseDir,
      ClientConfig clientConfig,
      StorageMetadataService storageMetadataService,
      ReadOnlyStoreRepository readOnlyStoreRepository,
      StorageEngineRepository storageEngineRepository,
      int maxConcurrentSnapshotUser,
      int snapshotRetentionTimeInMin,
      int blobTransferMaxTimeoutInMin,
      AggVersionedBlobTransferStats aggVersionedBlobTransferStats,
      BlobTransferUtils.BlobTransferTableFormat transferSnapshotTableFormat,
      int peersConnectivityFreshnessInSeconds) {
    try {
      BlobSnapshotManager blobSnapshotManager = new BlobSnapshotManager(
          readOnlyStoreRepository,
          storageEngineRepository,
          storageMetadataService,
          maxConcurrentSnapshotUser,
          snapshotRetentionTimeInMin,
          transferSnapshotTableFormat);
      BlobTransferManager<Void> manager = new NettyP2PBlobTransferManager(
          new P2PBlobTransferService(p2pTransferServerPort, baseDir, blobTransferMaxTimeoutInMin, blobSnapshotManager),
          new NettyFileTransferClient(
              p2pTransferClientPort,
              baseDir,
              storageMetadataService,
              peersConnectivityFreshnessInSeconds),
          new DaVinciBlobFinder(clientConfig),
          baseDir,
          aggVersionedBlobTransferStats);
      manager.start();
      return manager;
    } catch (Exception e) {
      // swallow the exception and continue the consumption via pubsub system
      LOGGER.warn("Failed to start up the P2P blob transfer manager for DaVinci Client", e);
      return null;
    }
  }

  /**
   * Get a P2P blob transfer manager for Server and start it.
   * @param p2pTransferServerPort the port used by the P2P transfer server
   * @param p2pTransferClientPort the port used by the P2P transfer client
   * @param baseDir the base directory of the underlying storage
   * @param customizedViewFuture the future of the customized view repository
   * @return the blob transfer manager
   */
  public static BlobTransferManager<Void> getP2PBlobTransferManagerForServerAndStart(
      int p2pTransferServerPort,
      int p2pTransferClientPort,
      String baseDir,
      CompletableFuture<HelixCustomizedViewOfflinePushRepository> customizedViewFuture,
      StorageMetadataService storageMetadataService,
      ReadOnlyStoreRepository readOnlyStoreRepository,
      StorageEngineRepository storageEngineRepository,
      int maxConcurrentSnapshotUser,
      int snapshotRetentionTimeInMin,
      int blobTransferMaxTimeoutInMin,
      AggVersionedBlobTransferStats aggVersionedBlobTransferStats,
      BlobTransferUtils.BlobTransferTableFormat transferSnapshotTableFormat,
      int peersConnectivityFreshnessInSeconds) {
    try {
      BlobSnapshotManager blobSnapshotManager = new BlobSnapshotManager(
          readOnlyStoreRepository,
          storageEngineRepository,
          storageMetadataService,
          maxConcurrentSnapshotUser,
          snapshotRetentionTimeInMin,
          transferSnapshotTableFormat);
      BlobTransferManager<Void> manager = new NettyP2PBlobTransferManager(
          new P2PBlobTransferService(p2pTransferServerPort, baseDir, blobTransferMaxTimeoutInMin, blobSnapshotManager),
          new NettyFileTransferClient(
              p2pTransferClientPort,
              baseDir,
              storageMetadataService,
              peersConnectivityFreshnessInSeconds),
          new ServerBlobFinder(customizedViewFuture),
          baseDir,
          aggVersionedBlobTransferStats);
      manager.start();
      return manager;
    } catch (Exception e) {
      // swallow the exception and continue the consumption via pubsub system
      LOGGER.warn("Failed to start up the P2P blob transfer manager for server", e);
      return null;
    }
  }
}
