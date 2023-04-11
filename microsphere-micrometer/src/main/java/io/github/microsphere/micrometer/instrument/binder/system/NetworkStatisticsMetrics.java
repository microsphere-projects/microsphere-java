package io.github.microsphere.micrometer.instrument.binder.system;

import io.github.microsphere.micrometer.instrument.binder.AbstractMeterBinder;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.BaseUnits;
import io.micrometer.core.lang.NonNullApi;
import io.micrometer.core.lang.NonNullFields;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.util.Collections.emptyList;

/**
 * Network Statistics Metrics
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 */
@NonNullApi
@NonNullFields
public class NetworkStatisticsMetrics extends AbstractMeterBinder {

    public static final Path STATS_FILE_PATH = Paths.get(System.getProperty("network.stats.file", "/proc/net/dev"));

    private MeterRegistry registry;

    public NetworkStatisticsMetrics() {
        this(emptyList());
    }

    public NetworkStatisticsMetrics(Iterable<Tag> tags) {
        super(tags);
    }

    private void bindStatsList() throws IOException {
        List<String> lines = Files.readAllLines(STATS_FILE_PATH, StandardCharsets.US_ASCII);
        int statsLineStartIndex = 2;
        int linesCount = lines.size();

        for (int i = statsLineStartIndex; i < linesCount; i++) {
            String statsLine = lines.get(i);
            Stats stats = parseStats(statsLine);
            bindStats(stats);
        }
    }

    private void bindStats(Stats stats) {
        Iterable<Tag> newTags = Tags.concat(tags, "interface", stats.name);

        Gauge.builder("network.receive.bytes", stats, Stats::getReceiveBytes).tags(newTags).description("Receive bytes").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.receive.packets", stats, Stats::getReceivePackets).tags(newTags).description("Receive packets").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.receive.errors", stats, Stats::getReceiveErrors).tags(newTags).description("Receive errors").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.receive.drop", stats, Stats::getReceiveDrop).tags(newTags).description("Receive drop").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.transmit.bytes", stats, Stats::getTransmitBytes).tags(newTags).description("Transmit bytes").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.transmit.packets", stats, Stats::getTransmitPackets).tags(newTags).description("Transmit packets").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.transmit.errors", stats, Stats::getTransmitErrors).tags(newTags).description("Transmit Errors").baseUnit(BaseUnits.BYTES).register(registry);

        Gauge.builder("network.transmit.drop", stats, Stats::getTransmitDrop).tags(newTags).description("Transmit drop").baseUnit(BaseUnits.BYTES).register(registry);
    }

    private void asyncBindStatsList() {
        Thread thread = new Thread(() -> {
            try {
                WatchService watchService = FileSystems.getDefault().newWatchService();
                Path dir = STATS_FILE_PATH.getParent();
                dir.register(watchService, ENTRY_MODIFY);
                WatchKey key;
                while (true) {
                    key = watchService.poll(1, TimeUnit.SECONDS);
                    if (key == null) {
                        continue;
                    }
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path path = (Path) event.context();
                        if (STATS_FILE_PATH.equals(dir.resolve(path))) {
                            bindStatsList();
                        }
                    }
                    key.reset();
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        thread.setName("Network Statistics Thread");
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    protected boolean supports(MeterRegistry registry) {
        return Files.exists(STATS_FILE_PATH);
    }

    @Override
    protected void doBindTo(MeterRegistry registry) throws Throwable {
        this.registry = registry;
        bindStatsList();
        asyncBindStatsList();
    }

    private Stats parseStats(String statsLine) {
        String[] nameAndData = StringUtils.split(statsLine, ':');
        Stats stats = null;
        if (nameAndData.length == 2) {

            String name = nameAndData[0].trim();
            String data = nameAndData[1].trim();
            int startIndex = -1;
            int endIndex = -1;
            char[] chars = data.toCharArray();
            int length = chars.length;

            List<String> values = new ArrayList<>(16);

            for (int i = 0; i < length; i++) {
                char c = chars[i];
                boolean isDigit = Character.isDigit(c);
                if (isDigit && startIndex == -1) {
                    startIndex = i;
                }
                if (Character.isWhitespace(c)) {
                    endIndex = i;
                }
                if (isDigit && i == length - 1) {
                    endIndex = i + 1;
                }
                if (endIndex > startIndex && startIndex > -1) {
                    String value = new String(chars, startIndex, endIndex - startIndex);
                    values.add(value);
                    startIndex = -1;
                }
            }

            stats = new Stats();
            stats.name = name;
            int index = 0;
            stats.receiveBytes = Long.parseLong(values.get(index++));
            stats.receivePackets = Long.parseLong(values.get(index++));
            stats.receiveErrors = Long.parseLong(values.get(index++));
            stats.receiveDrop = Long.parseLong(values.get(index++));
            index += 4;
            stats.transmitBytes = Long.parseLong(values.get(index++));
            stats.transmitPackets = Long.parseLong(values.get(index++));
            stats.transmitErrors = Long.parseLong(values.get(index++));
            stats.transmitDrop = Long.parseLong(values.get(index++));
        }
        return stats;
    }

    /**
     * Inter-|   Receive                                                |  Transmit
     * face |bytes    packets errs drop fifo frame compressed multicast|bytes    packets errs drop fifo colls carrier compressed
     * lo:       0       0    0    0    0     0          0         0        0       0    0    0    0     0       0          0
     * tunl0:       0       0    0    0    0     0          0         0        0       0    0    0    0     0       0          0
     * ip6tnl0:       0       0    0    0    0     0          0         0        0       0    0    0    0     0       0          0
     * eth0:    1392      16    0    0    0     0          0         0        0       0    0    0    0     0       0          0
     */
    private class Stats {

        private String name;

        private long receiveBytes;

        private long receivePackets;

        private long receiveErrors;

        private long receiveDrop;

        private long transmitBytes;

        private long transmitPackets;

        private long transmitErrors;

        private long transmitDrop;

        public long getReceiveBytes() {
            return receiveBytes;
        }

        public long getReceivePackets() {
            return receivePackets;
        }

        public long getReceiveErrors() {
            return receiveErrors;
        }

        public long getReceiveDrop() {
            return receiveDrop;
        }

        public long getTransmitBytes() {
            return transmitBytes;
        }

        public long getTransmitPackets() {
            return transmitPackets;
        }

        public long getTransmitErrors() {
            return transmitErrors;
        }

        public long getTransmitDrop() {
            return transmitDrop;
        }
    }
}
