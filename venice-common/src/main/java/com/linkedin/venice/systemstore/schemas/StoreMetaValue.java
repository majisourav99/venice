/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.linkedin.venice.systemstore.schemas;

@SuppressWarnings("all")
public class StoreMetaValue extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = org.apache.avro.Schema.parse("{\"type\":\"record\",\"name\":\"StoreMetaValue\",\"namespace\":\"com.linkedin.venice.systemstore.schemas\",\"fields\":[{\"name\":\"timestamp\",\"type\":\"long\",\"doc\":\"Timestamp when the value or a partial update for the value was generated by the writer (Venice Controller/Venice Server).\",\"default\":0},{\"name\":\"storeProperties\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StoreProperties\",\"fields\":[{\"name\":\"name\",\"type\":\"string\",\"doc\":\"Store name.\"},{\"name\":\"owner\",\"type\":\"string\",\"doc\":\"Owner of this store.\"},{\"name\":\"createdTime\",\"type\":\"long\",\"doc\":\"Timestamp when this store was created.\"},{\"name\":\"currentVersion\",\"type\":\"int\",\"doc\":\"The number of version which is used currently.\",\"default\":0},{\"name\":\"partitionCount\",\"type\":\"int\",\"doc\":\"Default partition count for all of versions in this store. Once first version become online, the number will be assigned.\",\"default\":0},{\"name\":\"enableWrites\",\"type\":\"boolean\",\"doc\":\"If a store is disabled from writing, new version can not be created for it.\",\"default\":true},{\"name\":\"enableReads\",\"type\":\"boolean\",\"doc\":\"If a store is disabled from being read, none of versions under this store could serve read requests.\",\"default\":true},{\"name\":\"storageQuotaInByte\",\"type\":\"long\",\"doc\":\"Maximum capacity a store version is able to have, and default is 20GB\",\"default\":21474836480},{\"name\":\"persistenceType\",\"type\":\"int\",\"doc\":\"Type of persistence storage engine, and default is 'ROCKS_DB'\",\"default\":2},{\"name\":\"routingStrategy\",\"type\":\"int\",\"doc\":\"How to route the key to partition, and default is 'CONSISTENT_HASH'\",\"default\":0},{\"name\":\"readStrategy\",\"type\":\"int\",\"doc\":\"How to read data from multiple replications, and default is 'ANY_OF_ONLINE'\",\"default\":0},{\"name\":\"offlinePushStrategy\",\"type\":\"int\",\"doc\":\"When doing off-line push, how to decide the data is ready to serve, and default is 'WAIT_N_MINUS_ONE_REPLCIA_PER_PARTITION'\",\"default\":1},{\"name\":\"largestUsedVersionNumber\",\"type\":\"int\",\"doc\":\"The largest version number ever used before for this store.\",\"default\":0},{\"name\":\"readQuotaInCU\",\"type\":\"long\",\"doc\":\"Quota for read request hit this store. Measurement is capacity unit.\",\"default\":0},{\"name\":\"hybridConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StoreHybridConfig\",\"fields\":[{\"name\":\"rewindTimeInSeconds\",\"type\":\"long\"},{\"name\":\"offsetLagThresholdToGoOnline\",\"type\":\"long\"},{\"name\":\"producerTimestampLagThresholdToGoOnlineInSeconds\",\"type\":\"long\"}]}],\"doc\":\"Properties related to Hybrid Store behavior. If absent (null), then the store is not hybrid.\",\"default\":null},{\"name\":\"accessControlled\",\"type\":\"boolean\",\"doc\":\"Store-level ACL switch. When disabled, Venice Router should accept every request.\",\"default\":true},{\"name\":\"compressionStrategy\",\"type\":\"int\",\"doc\":\"Strategy used to compress/decompress Record's value, and default is 'NO_OP'\",\"default\":0},{\"name\":\"clientDecompressionEnabled\",\"type\":\"boolean\",\"doc\":\"le/Disable client-side record decompression (default: true)\",\"default\":true},{\"name\":\"chunkingEnabled\",\"type\":\"boolean\",\"doc\":\"Whether current store supports large value (typically more than 1MB). By default, the chunking feature is disabled.\",\"default\":false},{\"name\":\"batchGetLimit\",\"type\":\"int\",\"doc\":\"Batch get key number limit, and Venice will use cluster-level config if it is not positive.\",\"default\":-1},{\"name\":\"numVersionsToPreserve\",\"type\":\"int\",\"doc\":\"How many versions this store preserve at most. By default it's 0 means we use the cluster level config to determine how many version is preserved.\",\"default\":0},{\"name\":\"incrementalPushEnabled\",\"type\":\"boolean\",\"doc\":\"Flag to see if the store supports incremental push or not\",\"default\":false},{\"name\":\"migrating\",\"type\":\"boolean\",\"doc\":\"Whether or not the store is in the process of migration.\",\"default\":false},{\"name\":\"writeComputationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not write-path computation feature is enabled for this store.\",\"default\":false},{\"name\":\"readComputationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether read-path computation is enabled for this store.\",\"default\":false},{\"name\":\"bootstrapToOnlineTimeoutInHours\",\"type\":\"int\",\"doc\":\"Maximum number of hours allowed for the store to transition from bootstrap to online state.\",\"default\":24},{\"name\":\"leaderFollowerModelEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not to use leader follower state transition model for upcoming version.\",\"default\":false},{\"name\":\"nativeReplicationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not native should be enabled for this store.  Will only successfully apply if leaderFollowerModelEnabled is also true either in this update or a previous version of the store.\",\"default\":false},{\"name\":\"pushStreamSourceAddress\",\"type\":\"string\",\"doc\":\"Address to the kafka broker which holds the source of truth topic for this store version.\",\"default\":\"\"},{\"name\":\"backupStrategy\",\"type\":\"int\",\"doc\":\"Strategies to store backup versions, and default is 'DELETE_ON_NEW_PUSH_START'\",\"default\":1},{\"name\":\"schemaAutoRegisteFromPushJobEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not value schema auto registration enabled from push job for this store.\",\"default\":false},{\"name\":\"latestSuperSetValueSchemaId\",\"type\":\"int\",\"doc\":\"For read compute stores with auto super-set schema enabled, stores the latest super-set value schema ID.\",\"default\":-1},{\"name\":\"hybridStoreDiskQuotaEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not storage disk quota is enabled for a hybrid store. This store config cannot be enabled until the routers and servers in the corresponding cluster are upgraded to the right version: 0.2.249 or above for routers and servers.\",\"default\":false},{\"name\":\"storeMetadataSystemStoreEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not the store metadata system store is enabled for this store.\",\"default\":false},{\"name\":\"etlConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StoreETLConfig\",\"fields\":[{\"name\":\"etledUserProxyAccount\",\"type\":\"string\",\"doc\":\"If enabled regular ETL or future version ETL, this account name is part of path for where the ETLed snapshots will go. for example, for user account veniceetl001, snapshots will be published to HDFS /jobs/veniceetl001/storeName.\"},{\"name\":\"regularVersionETLEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not enable regular version ETL for this store.\"},{\"name\":\"futureVersionETLEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not enable future version ETL - the version that might come online in future - for this store.\"}]}],\"doc\":\"Properties related to ETL Store behavior.\",\"default\":null},{\"name\":\"partitionerConfig\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StorePartitionerConfig\",\"fields\":[{\"name\":\"partitionerClass\",\"type\":\"string\"},{\"name\":\"partitionerParams\",\"type\":{\"type\":\"map\",\"values\":\"string\"}},{\"name\":\"amplificationFactor\",\"type\":\"int\"}]}],\"doc\":\"\",\"default\":null},{\"name\":\"incrementalPushPolicy\",\"type\":\"int\",\"doc\":\"Incremental Push Policy to reconcile with real time pushes, and default is 'PUSH_TO_VERSION_TOPIC'\",\"default\":0},{\"name\":\"latestVersionPromoteToCurrentTimestamp\",\"type\":\"long\",\"doc\":\"This is used to track the time when a new version is promoted to current version. For now, it is mostly to decide whether a backup version can be removed or not based on retention. For the existing store before this code change, it will be set to be current timestamp.\",\"default\":-1},{\"name\":\"backupVersionRetentionMs\",\"type\":\"long\",\"doc\":\"Backup retention time, and if it is not set (-1), Venice Controller will use the default configured retention. {@link com.linkedin.venice.ConfigKeys#CONTROLLER_BACKUP_VERSION_DEFAULT_RETENTION_MS}.\",\"default\":-1},{\"name\":\"replicationFactor\",\"type\":\"int\",\"doc\":\"The number of replica each store version will keep.\",\"default\":3},{\"name\":\"migrationDuplicateStore\",\"type\":\"boolean\",\"doc\":\"Whether or not the store is a duplicate store in the process of migration.\",\"default\":false},{\"name\":\"nativeReplicationSourceFabric\",\"type\":\"string\",\"doc\":\"The source fabric name to be uses in native replication. Remote consumption will happen from kafka in this fabric.\",\"default\":\"\"},{\"name\":\"daVinciPushStatusStoreEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not davinci push status store is enabled.\",\"default\":false},{\"name\":\"storeMetaSystemStoreEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not the store meta system store is enabled for this store.\",\"default\":false},{\"name\":\"versions\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"StoreVersion\",\"fields\":[{\"name\":\"storeName\",\"type\":\"string\",\"doc\":\"Name of the store which this version belong to.\"},{\"name\":\"number\",\"type\":\"int\",\"doc\":\"Version number.\"},{\"name\":\"createdTime\",\"type\":\"long\",\"doc\":\"Time when this version was created.\"},{\"name\":\"status\",\"type\":\"int\",\"doc\":\"Status of version, and default is 'STARTED'\",\"default\":1},{\"name\":\"pushJobId\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"compressionStrategy\",\"type\":\"int\",\"doc\":\"strategies used to compress/decompress Record's value, and default is 'NO_OP'\",\"default\":0},{\"name\":\"leaderFollowerModelEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not to use leader follower state transition.\",\"default\":false},{\"name\":\"nativeReplicationEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not native replication is enabled.\",\"default\":false},{\"name\":\"pushStreamSourceAddress\",\"type\":\"string\",\"doc\":\"Address to the kafka broker which holds the source of truth topic for this store version.\",\"default\":\"\"},{\"name\":\"bufferReplayEnabledForHybrid\",\"type\":\"boolean\",\"doc\":\"Whether or not to enable buffer replay for hybrid.\",\"default\":true},{\"name\":\"chunkingEnabled\",\"type\":\"boolean\",\"doc\":\"Whether or not large values are supported (via chunking).\",\"default\":false},{\"name\":\"pushType\",\"type\":\"int\",\"doc\":\"Producer type for this version, and default is 'BATCH'\",\"default\":0},{\"name\":\"partitionCount\",\"type\":\"int\",\"doc\":\"Partition count of this version.\",\"default\":0},{\"name\":\"partitionerConfig\",\"type\":[\"null\",\"StorePartitionerConfig\"],\"doc\":\"Config for custom partitioning.\",\"default\":null},{\"name\":\"incrementalPushPolicy\",\"type\":\"int\",\"doc\":\"Incremental Push Policy to reconcile with real time pushes., and default is 'PUSH_TO_VERSION_TOPIC'\",\"default\":0},{\"name\":\"replicationFactor\",\"type\":\"int\",\"doc\":\"The number of replica this store version is keeping.\",\"default\":3},{\"name\":\"nativeReplicationSourceFabric\",\"type\":\"string\",\"doc\":\"The source fabric name to be uses in native replication. Remote consumption will happen from kafka in this fabric.\",\"default\":\"\"},{\"name\":\"incrementalPushEnabled\",\"type\":\"boolean\",\"doc\":\"Flag to see if the store supports incremental push or not\",\"default\":false},{\"name\":\"useVersionLevelIncrementalPushEnabled\",\"type\":\"boolean\",\"doc\":\"Flag to see if incrementalPushEnabled config at StoreVersion should used. This is needed during migration of this config from Store level to Version level. We can deprecate this field later.\",\"default\":false}]}},\"doc\":\"List of non-retired versions. It's currently sorted and there is code run under the assumption that the last element in the list is the largest. Check out {VeniceHelixAdmin#getIncrementalPushVersion}, and please make it in mind if you want to change this logic\",\"default\":[]},{\"name\":\"systemStores\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"record\",\"name\":\"SystemStoreProperties\",\"fields\":[{\"name\":\"largestUsedVersionNumber\",\"type\":\"int\",\"default\":0},{\"name\":\"currentVersion\",\"type\":\"int\",\"default\":0},{\"name\":\"latestVersionPromoteToCurrentTimestamp\",\"type\":\"long\",\"default\":-1},{\"name\":\"versions\",\"type\":{\"type\":\"array\",\"items\":\"StoreVersion\"},\"default\":[]}]}},\"doc\":\"This field is used to maintain a mapping between each type of system store and the corresponding distinct properties\",\"default\":{}}]}],\"default\":null},{\"name\":\"storeKeySchemas\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StoreKeySchemas\",\"fields\":[{\"name\":\"keySchemaMap\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"A string to string map representing the mapping from id to key schema.\"}]}],\"doc\":\"\",\"default\":null},{\"name\":\"storeValueSchemas\",\"type\":[\"null\",{\"type\":\"record\",\"name\":\"StoreValueSchemas\",\"fields\":[{\"name\":\"valueSchemaMap\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"A string to string map representing the mapping from schema id to value schema string.\"}]}],\"doc\":\"\",\"default\":null},{\"name\":\"storeReplicaStatuses\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"record\",\"name\":\"StoreReplicaStatus\",\"fields\":[{\"name\":\"status\",\"type\":\"int\",\"doc\":\"replica status\"}]}},\"doc\":\"This field describes the replica statuses per version per partition, and the mapping is 'host_port' -> 'replica status'\",\"default\":{}}]}");
  /** Timestamp when the value or a partial update for the value was generated by the writer (Venice Controller/Venice Server). */
  public long timestamp;
  public com.linkedin.venice.systemstore.schemas.StoreProperties storeProperties;
  /**  */
  public com.linkedin.venice.systemstore.schemas.StoreKeySchemas storeKeySchemas;
  /**  */
  public com.linkedin.venice.systemstore.schemas.StoreValueSchemas storeValueSchemas;
  /** This field describes the replica statuses per version per partition, and the mapping is 'host_port' -> 'replica status' */
  public java.util.Map<java.lang.CharSequence,com.linkedin.venice.systemstore.schemas.StoreReplicaStatus> storeReplicaStatuses;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return timestamp;
    case 1: return storeProperties;
    case 2: return storeKeySchemas;
    case 3: return storeValueSchemas;
    case 4: return storeReplicaStatuses;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: timestamp = (java.lang.Long)value$; break;
    case 1: storeProperties = (com.linkedin.venice.systemstore.schemas.StoreProperties)value$; break;
    case 2: storeKeySchemas = (com.linkedin.venice.systemstore.schemas.StoreKeySchemas)value$; break;
    case 3: storeValueSchemas = (com.linkedin.venice.systemstore.schemas.StoreValueSchemas)value$; break;
    case 4: storeReplicaStatuses = (java.util.Map<java.lang.CharSequence,com.linkedin.venice.systemstore.schemas.StoreReplicaStatus>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
}
