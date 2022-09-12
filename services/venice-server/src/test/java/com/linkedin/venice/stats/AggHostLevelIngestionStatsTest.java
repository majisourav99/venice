package com.linkedin.venice.stats;

import static org.mockito.Mockito.*;

import com.linkedin.davinci.config.VeniceServerConfig;
import com.linkedin.davinci.kafka.consumer.StoreIngestionTask;
import com.linkedin.davinci.stats.AggHostLevelIngestionStats;
import com.linkedin.davinci.stats.HostLevelIngestionStats;
import com.linkedin.venice.meta.ReadOnlyStoreRepository;
import com.linkedin.venice.tehuti.MockTehutiReporter;
import com.linkedin.venice.utils.Utils;
import io.tehuti.metrics.MetricsRepository;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Collections;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;


public class AggHostLevelIngestionStatsTest {
  private AggHostLevelIngestionStats aggStats;
  private HostLevelIngestionStats fooStats;
  private HostLevelIngestionStats barStats;
  private MetricsRepository metricsRepository;
  private MockTehutiReporter reporter;

  private static final String STORE_FOO = Utils.getUniqueString("store_foo");
  private static final String STORE_BAR = Utils.getUniqueString("store_bar");

  @BeforeTest
  public void setUp() {
    metricsRepository = new MetricsRepository();
    this.reporter = new MockTehutiReporter();
    metricsRepository.addReporter(reporter);
    VeniceServerConfig mockVeniceServerConfig = Mockito.mock(VeniceServerConfig.class);
    doReturn(Int2ObjectMaps.emptyMap()).when(mockVeniceServerConfig).getKafkaClusterIdToAliasMap();
    aggStats = new AggHostLevelIngestionStats(
        metricsRepository,
        mockVeniceServerConfig,
        Collections.emptyMap(),
        mock(ReadOnlyStoreRepository.class),
        true);
    fooStats = aggStats.getStoreStats(STORE_FOO);
    barStats = aggStats.getStoreStats(STORE_BAR);

    StoreIngestionTask task = Mockito.mock(StoreIngestionTask.class);
    Mockito.doReturn(true).when(task).isRunning();

    fooStats.recordPollResultNum(1);
    barStats.recordPollResultNum(2);

    fooStats.recordStorageQuotaUsed(0.6);
    fooStats.recordStorageQuotaUsed(1);
    fooStats.recordTotalBytesReadFromKafkaAsUncompressedSize(100);
    barStats.recordTotalBytesReadFromKafkaAsUncompressedSize(200);
    fooStats.recordDiskQuotaAllowed(100);
    fooStats.recordDiskQuotaAllowed(200);
  }

  @AfterTest
  public void cleanUp() {
    metricsRepository.close();
  }

  @Test
  public void testMetrics() {
    Assert.assertEquals(reporter.query("." + STORE_FOO + "--kafka_poll_result_num.Total").value(), 1d);
    Assert.assertEquals(reporter.query("." + STORE_BAR + "--kafka_poll_result_num.Total").value(), 2d);
    Assert.assertEquals(reporter.query(".total--kafka_poll_result_num.Avg").value(), 1.5d);
    Assert.assertEquals(reporter.query("." + STORE_FOO + "--storage_quota_used.Avg").value(), 0.8);
    Assert.assertEquals(reporter.query(".total--bytes_read_from_kafka_as_uncompressed_size.Total").value(), 300d);
    Assert.assertEquals(reporter.query("." + STORE_FOO + "--global_store_disk_quota_allowed.Max").value(), 200d);

    aggStats.handleStoreDeleted(STORE_FOO);
    Assert.assertNull(metricsRepository.getMetric("." + STORE_FOO + "--kafka_poll_result_num.Total"));
  }
}
