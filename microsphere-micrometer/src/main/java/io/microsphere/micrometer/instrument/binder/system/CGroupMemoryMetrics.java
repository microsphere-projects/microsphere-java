package io.microsphere.micrometer.instrument.binder.system;

import io.microsphere.micrometer.instrument.binder.AbstractMeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import org.apache.commons.lang3.math.NumberUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * CGroup Memory Metrics
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see <a href="https://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt">https://www.kernel.org/doc/Documentation/cgroup-v1/memory.txt</a>
 * @see JvmMemoryMetrics
 */
@NonNullApi
@NonNullFields
public class CGroupMemoryMetrics extends AbstractMeterBinder {

    private static final String METRIC_PREFIX = "cgroup.";

    private static final Path ROOT_DIRECTORY_PATH = Paths.get(System.getProperty("cgroup.memory.dir", "/sys/fs/cgroup/memory/"));

    private static final Path MEMORY_STAT_FILE_PATH = ROOT_DIRECTORY_PATH.resolve("memory.stat");

    public CGroupMemoryMetrics() {
        super();
    }

    public CGroupMemoryMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    @Override
    protected boolean supports(MeterRegistry registry) {
        if (!Files.exists(ROOT_DIRECTORY_PATH)) {
            logger.info("The CGroup memory directory[path: '{}'] does not exist!", ROOT_DIRECTORY_PATH);
            return false;
        }
        return true;
    }

    @Override
    protected void doBindTo(MeterRegistry registry) throws Throwable {

        buildBytesGauge("memory.usage_in_bytes", registry);
        buildBytesGauge("memory.max_usage_in_bytes", registry);
        buildBytesGauge("memory.memsw.usage_in_bytes", registry);
        buildBytesGauge("memory.memsw.max_usage_in_bytes", registry);
        buildBytesGauge("memory.limit_in_bytes", registry);
        buildMemoryStatsGauge(registry);
    }

    private void buildBytesGauge(String fileName, MeterRegistry registry) {
        String metricName = METRIC_PREFIX + fileName;
        buildBytesGauge(metricName, () -> readFileAsLong(fileName), registry);
    }

    /**
     * cache 1462194176
     * rss 912412672
     * rss_huge 0
     * mapped_file 32768
     * swap 0
     *
     * @param registry
     */
    private void buildMemoryStatsGauge(MeterRegistry registry) {
        Map<String, String> memoryStatistics = loadMemoryStatistics();
        buildMemoryStatsGauge("cache", memoryStatistics, registry);
        buildMemoryStatsGauge("rss", memoryStatistics, registry);
        buildMemoryStatsGauge("mapped_file", memoryStatistics, registry);
        buildMemoryStatsGauge("swap", memoryStatistics, registry);
        buildMemoryStatsGauge("active_anon", memoryStatistics, registry);
        buildMemoryStatsGauge("inactive_anon", memoryStatistics, registry);
        buildMemoryStatsGauge("active_file", memoryStatistics, registry);
        buildMemoryStatsGauge("inactive_file", memoryStatistics, registry);
        buildMemoryStatsGauge("unevictable", memoryStatistics, registry);
        buildMemoryStatsGauge("hierarchical_memory_limit", memoryStatistics, registry);
        buildMemoryStatsGauge("hierarchical_memsw_limit", memoryStatistics, registry);
    }

    private Map<String, String> loadMemoryStatistics() {
        List<String> lines = readAllLines(MEMORY_STAT_FILE_PATH);
        int length = lines.size();
        Map<String, String> memoryStatistics = new LinkedHashMap<>(length);
        for (int i = 0; i < length; i++) {
            String line = lines.get(i);
            String[] keyAndValue = line.split(" ");
            if (keyAndValue.length == 2) {
                memoryStatistics.put(keyAndValue[0], keyAndValue[1]);
            }
        }
        return memoryStatistics;
    }

    private void buildMemoryStatsGauge(String statistic, Map<String, String> memoryStatistics, MeterRegistry registry) {
        String value = memoryStatistics.get(statistic);
        if (value == null) {
            logger.warn("memory.stat Statistics : {} was not found", statistic);
            return;
        }
        if (!NumberUtils.isDigits(value)) {
            logger.warn("memory.stat Statistics : {} is not numeric", statistic);
            return;
        }
        String metricName = METRIC_PREFIX + "memory.stat." + statistic;
        buildBytesGauge(metricName, () -> Long.parseLong(value), registry);
    }

    private void buildBytesGauge(String name, Supplier<Number> supplier, MeterRegistry registry) {
        Gauge.builder(name, supplier).tags(tags).baseUnit(BaseUnits.BYTES).register(registry);
    }

    private Long readFileAsLong(String fileName) {
        return readFileAsLong(ROOT_DIRECTORY_PATH.resolve(fileName));
    }

    private Long readFileAsLong(Path file) {
        if (!Files.exists(file) || !Files.isReadable(file)) {
            logger.debug("File[path : {}] does not exist !", file);
            return Long.valueOf(-1L);
        }
        return Long.parseLong(readFileContent(file));
    }

    private String readFileContent(Path filePath) {
        List<String> lines = readAllLines(filePath);
        return lines.isEmpty() ? null : lines.get(0).trim();
    }

    private List<String> readAllLines(Path filePath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(filePath);
        } catch (Throwable e) {
            logger.warn("File [path : {}] can't be read", filePath, e);
            lines = Collections.emptyList();
        }
        return lines;
    }
}
