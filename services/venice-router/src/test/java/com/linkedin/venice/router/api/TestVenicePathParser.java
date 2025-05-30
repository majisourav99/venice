package com.linkedin.venice.router.api;

import static com.linkedin.venice.HttpConstants.VENICE_CLIENT_COMPUTE_TRUE;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.FileAssert.fail;

import com.linkedin.alpini.netty4.misc.BasicFullHttpRequest;
import com.linkedin.alpini.router.api.RouterException;
import com.linkedin.venice.HttpConstants;
import com.linkedin.venice.compression.CompressionStrategy;
import com.linkedin.venice.compression.CompressorFactory;
import com.linkedin.venice.compute.ComputeRequestWrapper;
import com.linkedin.venice.exceptions.VeniceException;
import com.linkedin.venice.helix.HelixReadOnlyStoreConfigRepository;
import com.linkedin.venice.meta.NameRepository;
import com.linkedin.venice.meta.ReadOnlyStoreRepository;
import com.linkedin.venice.meta.Store;
import com.linkedin.venice.partitioner.DefaultVenicePartitioner;
import com.linkedin.venice.partitioner.VenicePartitioner;
import com.linkedin.venice.read.RequestType;
import com.linkedin.venice.router.VeniceRouterConfig;
import com.linkedin.venice.router.api.path.TestVeniceComputePath;
import com.linkedin.venice.router.api.path.VeniceComputePath;
import com.linkedin.venice.router.api.path.VeniceMultiGetPath;
import com.linkedin.venice.router.api.path.VenicePath;
import com.linkedin.venice.router.stats.AggRouterHttpRequestStats;
import com.linkedin.venice.router.stats.RouterStats;
import com.linkedin.venice.router.stats.StaleVersionStats;
import com.linkedin.venice.schema.avro.ReadAvroProtocolDefinition;
import com.linkedin.venice.serializer.RecordSerializer;
import com.linkedin.venice.serializer.SerializerDeserializerFactory;
import com.linkedin.venice.stats.VeniceMetricsRepository;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Created by mwise on 3/4/16.
 */
public class TestVenicePathParser {
  private final Map<String, String> clusterToD2Map = new HashMap<>();
  private static final String CLUSTER = "cluster";
  private static final VeniceRouterConfig MOCK_ROUTER_CONFIG = mock(VeniceRouterConfig.class);

  VeniceVersionFinder getVersionFinder() {
    // Mock objects
    Store mockStore = mock(Store.class);
    doReturn(1).when(mockStore).getCurrentVersion();
    doReturn(true).when(mockStore).isEnableReads();
    doReturn(CompressionStrategy.NO_OP).when(mockStore).getCompressionStrategy();
    ReadOnlyStoreRepository mockMetadataRepository = mock(ReadOnlyStoreRepository.class);
    doReturn(mockStore).when(mockMetadataRepository).getStore(Mockito.anyString());
    StaleVersionStats stats = mock(StaleVersionStats.class);
    HelixReadOnlyStoreConfigRepository storeConfigRepo = mock(HelixReadOnlyStoreConfigRepository.class);
    CompressorFactory compressorFactory = mock(CompressorFactory.class);
    VeniceVersionFinder veniceVersionFinder = spy(
        new VeniceVersionFinder(
            mockMetadataRepository,
            TestVeniceVersionFinder.getCVBasedMockedRoutingRepo(),
            stats,
            storeConfigRepo,
            clusterToD2Map,
            CLUSTER,
            compressorFactory,
            null));
    doReturn(true).when(veniceVersionFinder).isDecompressorReady(any(), anyString());
    doReturn(true).when(veniceVersionFinder).isPartitionResourcesReady(anyString());
    return veniceVersionFinder;
  }

  RouterStats<AggRouterHttpRequestStats> getMockedStats() {
    RouterStats<AggRouterHttpRequestStats> mockRouterStats = mock(RouterStats.class);
    when(mockRouterStats.getStatsByType(any())).thenReturn(mock(AggRouterHttpRequestStats.class));
    return mockRouterStats;
  }

  @BeforeClass
  public void setUp() {
    RouterExceptionAndTrackingUtils.setRouterStats(
        new RouterStats<>(
            requestType -> new AggRouterHttpRequestStats(
                CLUSTER,
                new VeniceMetricsRepository(),
                requestType,
                mock(ReadOnlyStoreRepository.class),
                true)));
  }

  @AfterClass
  public void cleanUp() {
    RouterExceptionAndTrackingUtils.setRouterStats(null);
  }

  @Test
  public void testParseResourceUri_ComputeRequest() throws RouterException {
    VenicePartitionFinder partitionFinder = mock(VenicePartitionFinder.class);
    doReturn(10).when(partitionFinder).getNumPartitions(any());
    doReturn(3).when(partitionFinder).findPartitionNumber(any(), anyInt(), any(), anyInt());
    doReturn(new DefaultVenicePartitioner()).when(partitionFinder).findPartitioner(any(), anyInt());

    ReadOnlyStoreRepository storeRepository = mock(ReadOnlyStoreRepository.class);
    doReturn(10).when(storeRepository).getBatchGetLimit(any());

    VeniceRouterConfig routerConfig = mock(VeniceRouterConfig.class);
    TreeMap<Integer, Integer> batchGetRetryThresholds = new TreeMap<>();
    batchGetRetryThresholds.put(1, 1);
    when(routerConfig.getLongTailRetryForBatchGetThresholdMs()).thenReturn(batchGetRetryThresholds);

    VenicePathParser parser = new VenicePathParser(
        getVersionFinder(),
        partitionFinder,
        getMockedStats(),
        storeRepository,
        routerConfig,
        mock(CompressorFactory.class),
        mock(VeniceMetricsRepository.class),
        mock(ScheduledExecutorService.class),
        new NameRepository());

    String storeName = "test-store";
    String uri = "storage/" + storeName;
    List<ByteBuffer> keys = Collections.singletonList(ByteBuffer.wrap(("test-key").getBytes()));
    BasicFullHttpRequest request = TestVeniceComputePath.getComputeHttpRequest(
        storeName,
        TestVeniceComputePath.getComputeRequest(),
        keys,
        ComputeRequestWrapper.LATEST_SCHEMA_VERSION_FOR_COMPUTE_REQUEST);

    // Verify request rejection when neither read-compute or client-compute is available.
    doReturn(false).when(storeRepository).isReadComputationEnabled(any());
    RouterException e = Assert.expectThrows(RouterException.class, () -> parser.parseResourceUri(uri, request));
    Assert.assertEquals(
        e.getMessage(),
        "Read compute is not enabled for the store. Please contact Venice team to enable the feature.");

    // Verify request handling when read-compute is enabled.
    doReturn(true).when(storeRepository).isReadComputationEnabled(any());
    request.content().resetReaderIndex();
    VenicePath path = parser.parseResourceUri(uri, request);
    Assert.assertTrue(path instanceof VeniceComputePath);
    assertNull(path.getClientComputeHeader());

    // Verify request handling when both read-compute and client-compute are available.
    request.headers().add(HttpConstants.VENICE_CLIENT_COMPUTE, "1");
    request.content().resetReaderIndex();
    path = parser.parseResourceUri(uri, request);
    Assert.assertTrue(path instanceof VeniceComputePath);
    assertNull(path.getClientComputeHeader());

    // Verify compute to multi-get conversion when read-compute is disabled, but client-compute is available.
    doReturn(false).when(storeRepository).isReadComputationEnabled(any());
    request.headers().add(HttpConstants.VENICE_CLIENT_COMPUTE, "1");
    request.content().resetReaderIndex();
    path = parser.parseResourceUri(uri, request);
    Assert.assertTrue(path instanceof VeniceMultiGetPath);
    assertNotNull(path.getClientComputeHeader());
    assertEquals(path.getClientComputeHeader(), VENICE_CLIENT_COMPUTE_TRUE);
  }

  @Test
  public void parsesQueries() throws RouterException {
    String uri = "storage/store/key";
    VenicePartitionFinder partitionFinder = mock(VenicePartitionFinder.class);
    CompressorFactory compressorFactory = mock(CompressorFactory.class);
    doReturn(3).when(partitionFinder).findPartitionNumber(any(), anyInt(), anyString(), anyInt());
    VenicePathParser parser = new VenicePathParser(
        getVersionFinder(),
        partitionFinder,
        getMockedStats(),
        mock(ReadOnlyStoreRepository.class),
        MOCK_ROUTER_CONFIG,
        compressorFactory,
        mock(VeniceMetricsRepository.class),
        mock(ScheduledExecutorService.class),
        new NameRepository());
    BasicFullHttpRequest request = new BasicFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uri, 0, 0);
    VenicePath path = parser.parseResourceUri(uri, request);
    String keyB64 = Base64.getEncoder().encodeToString("key".getBytes());
    Assert.assertEquals(path.getLocation(), "storage/store_v1/3/" + keyB64 + "?f=b64");

    try {
      parser.substitutePartitionKey(path, RouterKey.fromString("key2"));
      Assert.fail(
          "A VeniceException should be thrown when passing different key to function: substitutePartitionKey for single-get");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof VeniceException);
    }
  }

  @Test
  public void parsesB64Uri() throws RouterException {
    String myUri = "/storage/storeName/bXlLZXk=?f=b64";
    String expectedKey = "myKey";
    VenicePartitionFinder partitionFinder = mock(VenicePartitionFinder.class);
    CompressorFactory compressorFactory = mock(CompressorFactory.class);

    BasicFullHttpRequest request = new BasicFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, myUri, 0, 0);
    VenicePath path = new VenicePathParser(
        getVersionFinder(),
        partitionFinder,
        getMockedStats(),
        mock(ReadOnlyStoreRepository.class),
        MOCK_ROUTER_CONFIG,
        compressorFactory,
        mock(VeniceMetricsRepository.class),
        mock(ScheduledExecutorService.class),
        new NameRepository()).parseResourceUri(myUri, request);
    ByteBuffer partitionKey = path.getPartitionKey().getKeyBuffer();
    Assert.assertEquals(
        path.getPartitionKey().getKeyBuffer(),
        ByteBuffer.wrap(expectedKey.getBytes()),
        new String(partitionKey.array(), partitionKey.position(), partitionKey.remaining()) + " should match "
            + expectedKey);
  }

  @Test(expectedExceptions = RouterException.class)
  public void failsToParseOtherActions() throws RouterException {
    VenicePartitionFinder partitionFinder = mock(VenicePartitionFinder.class);
    CompressorFactory compressorFactory = mock(CompressorFactory.class);
    new VenicePathParser(
        getVersionFinder(),
        partitionFinder,
        getMockedStats(),
        mock(ReadOnlyStoreRepository.class),
        MOCK_ROUTER_CONFIG,
        compressorFactory,
        mock(VeniceMetricsRepository.class),
        mock(ScheduledExecutorService.class),
        new NameRepository()).parseResourceUri("/badAction/storeName/key");
  }

  @Test
  public void parseRequestWithBatchSizeViolation() throws RouterException {
    String storeName = "storeName";
    String myUri = "/storage/" + storeName;
    VenicePartitionFinder partitionFinder = mock(VenicePartitionFinder.class);
    CompressorFactory compressorFactory = mock(CompressorFactory.class);
    ReadAvroProtocolDefinition expectedProtocol = ReadAvroProtocolDefinition.MULTI_GET_CLIENT_REQUEST_V1;
    RecordSerializer<ByteBuffer> serializer =
        SerializerDeserializerFactory.getAvroGenericSerializer(expectedProtocol.getSchema());
    int maxKeyCount = 10;
    ArrayList<ByteBuffer> keys = new ArrayList<>();
    for (int cur = 0; cur <= maxKeyCount; ++cur) {
      keys.add(ByteBuffer.wrap(("key_prefix_" + cur).getBytes()));
    }
    ByteBuf content = Unpooled.wrappedBuffer(serializer.serializeObjects(keys));
    HttpHeaders headers =
        new DefaultHttpHeaders().add(HttpConstants.VENICE_API_VERSION, expectedProtocol.getProtocolVersion());
    BasicFullHttpRequest request = new BasicFullHttpRequest(
        HttpVersion.HTTP_1_1,
        HttpMethod.POST,
        myUri,
        content,
        headers,
        EmptyHttpHeaders.INSTANCE,
        UUID.randomUUID(),
        0,
        0);
    ReadOnlyStoreRepository storeRepository = mock(ReadOnlyStoreRepository.class);
    doReturn(maxKeyCount).when(storeRepository).getBatchGetLimit(anyString());
    RouterStats mockRouterStats = mock(RouterStats.class);
    AggRouterHttpRequestStats multiGetStats = mock(AggRouterHttpRequestStats.class);
    AggRouterHttpRequestStats singleGetStats = mock(AggRouterHttpRequestStats.class);
    when(mockRouterStats.getStatsByType(RequestType.MULTI_GET)).thenReturn(multiGetStats);
    when(mockRouterStats.getStatsByType(RequestType.SINGLE_GET)).thenReturn(singleGetStats);
    when(partitionFinder.findPartitioner(anyString(), anyInt())).thenReturn(mock(VenicePartitioner.class));

    VenicePathParser pathParser = new VenicePathParser(
        getVersionFinder(),
        partitionFinder,
        mockRouterStats,
        storeRepository,
        MOCK_ROUTER_CONFIG,
        compressorFactory,
        mock(VeniceMetricsRepository.class),
        mock(ScheduledExecutorService.class),
        new NameRepository());
    try {
      pathParser.parseResourceUri(myUri, request);
      fail("A RouterException should be thrown here");
    } catch (RouterException e) {
      // expected and validate bad request metric
      verify(multiGetStats, times(1))
          .recordBadRequestKeyCount(storeName, HttpResponseStatus.REQUEST_ENTITY_TOO_LARGE, maxKeyCount + 1);
    } catch (Throwable t) {
      t.printStackTrace();
      fail("Only RouterException is expected, but got: " + t.getClass());
    }
  }

  @Test
  public void validatesResourceNames() {
    String[] goodNames =
        { "goodName", "good_name_with_underscores", "good-name-with-dashes", "goodNameWithNumbers1234545" };

    for (String name: goodNames) {
      Assert.assertTrue(VenicePathParser.isStoreNameValid(name), "Store name: " + name + " should be valid");
    }

    String[] badNames = { "bad name with space", "bad.name.with.dots", "8startsWithNumber",
        "bad-name-that-is-just-fine-except-that-the-name-is-really-long-like-longer-than-128-chars-bad-name-that-is-just-fine-except-that-the-name-is-really-long-like-longer-than-128-chars" };

    for (String name: badNames) {
      Assert.assertFalse(VenicePathParser.isStoreNameValid(name), "Store name: " + name + " should not be valid");
    }
  }
}
