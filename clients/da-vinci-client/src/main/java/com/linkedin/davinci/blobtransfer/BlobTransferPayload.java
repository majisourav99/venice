package com.linkedin.davinci.blobtransfer;

import static com.linkedin.venice.store.rocksdb.RocksDBUtils.composePartitionDbDir;
import static com.linkedin.venice.store.rocksdb.RocksDBUtils.composeSnapshotDir;

import com.linkedin.davinci.blobtransfer.BlobTransferUtils.BlobTransferTableFormat;
import com.linkedin.venice.utils.Utils;


/**
 * POJO to hold the payload for blob transfer related informartion
 */
public class BlobTransferPayload {
  private final int partition;
  private final String topicName;
  private final String partitionDir;
  private final String storeName;
  private final BlobTransferTableFormat requestTableFormat;

  public BlobTransferPayload(
      String baseDir,
      String storeName,
      int version,
      int partition,
      BlobTransferTableFormat requestTableFormat) {
    this.partition = partition;
    this.storeName = storeName;
    this.topicName = storeName + "_v" + version;
    this.partitionDir = composePartitionDbDir(baseDir, topicName, partition);
    this.requestTableFormat = requestTableFormat;
  }

  public String getPartitionDir() {
    return partitionDir;
  }

  public String getSnapshotDir() {
    return composeSnapshotDir(partitionDir);
  }

  public String getFullResourceName() {
    return Utils.getReplicaId(topicName, partition);
  }

  public String getTopicName() {
    return topicName;
  }

  public int getPartition() {
    return partition;
  }

  public String getStoreName() {
    return storeName;
  }

  public BlobTransferTableFormat getRequestTableFormat() {
    return requestTableFormat;
  }
}
