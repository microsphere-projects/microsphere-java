/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.micrometer.instrument.binder.sentinel;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.ProcessorSlotEntryCallback;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import io.microsphere.micrometer.instrument.binder.AbstractMeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.TimeGauge;
import io.micrometer.core.instrument.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import static com.alibaba.csp.sentinel.slots.statistic.StatisticSlotCallbackRegistry.addEntryCallback;
import static java.util.Collections.emptyList;

/**
 * Abstract Micrometer Metrics for Alibaba Sentinel
 *
 * @param <M> the metadata type that may source from a resource, e,g. Feign, Redis and on on
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractSentinelMetrics<M> extends AbstractMeterBinder implements ProcessorSlotEntryCallback<DefaultNode> {

    /**
     * The cache for Sentinel resource name and metadata
     */
    private final Map<String, M> resourceMetadataCache = new HashMap<>(64);

    /**
     * Processed the mapping between Sentinel resource name and {@link ClusterNode}
     */
    private final ConcurrentMap<String, ClusterNode> processedResourceClusterNodes = new ConcurrentHashMap<>(64);

    protected MeterRegistry registry;

    public AbstractSentinelMetrics() {
        this(emptyList());
    }

    public AbstractSentinelMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    @Override
    public final void onPass(Context context, ResourceWrapper resourceWrapper, DefaultNode defaultNode, int count, Object... args) throws Exception {
        addMetrics(resourceWrapper, defaultNode);
    }

    @Override
    public final void onBlocked(BlockException ex, Context context, ResourceWrapper resourceWrapper, DefaultNode defaultNode, int count, Object... args) {
        addMetrics(resourceWrapper, defaultNode);
    }

    @Override
    protected boolean supports(MeterRegistry registry) {
        // unsupported if resourceMetadataCache is empty
        return !resourceMetadataCache.isEmpty();
    }

    @PostConstruct
    public void init() {
        addEntryCallback(getClass().getName(), this);
        initResourceMetadataCache(resourceMetadataCache);
    }

    @PreDestroy
    public void destroy() throws Exception {
        clearResourceMetadataCache();
        clearProcessedResourceClusterNodes();
    }

    @Override
    protected void doBindTo(MeterRegistry registry) throws Throwable {
        this.registry = registry;
    }

    protected abstract void initResourceMetadataCache(Map<String, M> resourceMetadataCache);

    protected void addMetrics(ResourceWrapper resourceWrapper, DefaultNode defaultNode) {
        String resourceName = resourceWrapper.getName();
        M metadata = getMetadata(resourceName);
        if (metadata != null) { // If metadata was found
            ClusterNode clusterNode = defaultNode.getClusterNode();
            ClusterNode processedClusterNode = processedResourceClusterNodes.get(resourceName);
            if (!Objects.equals(processedClusterNode, clusterNode)) {
                addMetrics(resourceName, metadata, clusterNode, registry);
                processedResourceClusterNodes.put(resourceName, clusterNode);
            }
        }
    }

    private void addMetrics(String resourceName, M metadata, ClusterNode clusterNode, MeterRegistry registry) {
        String metricNamePrefix = resolveMetricNamePrefix();

        List<Tag> tags = buildTags(resourceName);

        addTags(resourceName, metadata, clusterNode, tags);

        TimeGauge.builder(metricNamePrefix + "rt", clusterNode, TimeUnit.MILLISECONDS, ClusterNode::avgRt)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "total", clusterNode::totalRequest)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "success", clusterNode::totalSuccess)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "pass", clusterNode::totalPass)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "block", clusterNode::blockRequest)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "exception", clusterNode::totalException)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "total-qps", clusterNode::totalQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "success-qps", clusterNode::successQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "max-success-qps", clusterNode::maxSuccessQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "pass-qps", clusterNode::passQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "block-qps", clusterNode::blockQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);

        Gauge.builder(metricNamePrefix + "exception-qps", clusterNode::exceptionQps)
                .strongReference(true)
                .tags(tags)
                .register(registry);
    }


    protected abstract String getMetricsNamePrefix();

    protected abstract void addTags(String resourceName, M metadata, ClusterNode clusterNode, List<Tag> tags);

    private M getMetadata(String resourceName) {
        return resourceMetadataCache.get(resourceName);
    }

    private String resolveMetricNamePrefix() {
        String metricNamePrefix = getMetricsNamePrefix();
        if (StringUtils.isBlank(metricNamePrefix)) {
            throw new IllegalStateException(getClass().getName() + ".getMetricsNamePrefix() must not return a blank content");
        }
        return metricNamePrefix.endsWith(".") ? metricNamePrefix : metricNamePrefix + ".";
    }

    private List<Tag> buildTags(String resourceName) {
        List<Tag> tags = new LinkedList<>();
        super.tags.forEach(tags::add);
        tags.add(Tag.of("resource", resourceName));
        return tags;
    }

    private void clearResourceMetadataCache() {
        resourceMetadataCache.clear();
    }

    private void clearProcessedResourceClusterNodes() {
        processedResourceClusterNodes.clear();
    }
}
