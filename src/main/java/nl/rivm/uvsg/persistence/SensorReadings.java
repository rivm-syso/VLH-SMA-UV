package nl.rivm.uvsg.persistence;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
TODO MMo: document the purpose of toImmutable() and the use of a cumulativeAverage
 */
@Slf4j
@RequiredArgsConstructor
public class SensorReadings {

    @Getter
    private final ZonedDateTime utcDateTime;
    @Getter
    private final String deviceName;

    @Setter
    @Getter
    private Integer scaleFactor;

    private Integer size;

    // TODO MMo - check the need for an initial capacity
    private List<Integer> readings = new ArrayList<>();
    private double cumulativeAverage;
    private Double average;

    public void addReading(int reading) {
        if(average != null) {
            throw new IllegalStateException("addReading() is not allowed after calling toImmutable()");
        }
        readings.add(reading);
        int newSize = readings.size();
        cumulativeAverage += (reading - cumulativeAverage) / newSize;
        log.trace(">>>>{}: addReading(reading={}) => newSize={}, cumulativeAverage={}", deviceName, reading, newSize, cumulativeAverage);
    }

    public int getSize() {
        if (size == null) {
            throw new IllegalStateException("getSize() can be called only after calling toImmutable()");
        }
        return size;
    }

    public double getAverage() {
        if(average == null) {
            throw new IllegalStateException("getAverage() can be called only after calling toImmutable()");
        }
        if(readings.size() == 0) {
            return 0.0;
        }
        return average;
    }

    public double getStandardDeviation() {
        if(average == null) {
            throw new IllegalStateException("getStandardDeviation() can be called only after calling toImmutable()");
        }
        if(readings.size() == 0) {
            return 0.0;
        }
        double sumOfSquaredDiffs = 0;
        for (int reading : readings) {
            double diff = reading - cumulativeAverage;
            sumOfSquaredDiffs += (diff * diff);
        }
        double variance = sumOfSquaredDiffs / readings.size();
        return Math.sqrt(variance);
    }

    // TODO !!!MMo - Consider making reading-size immutable and omit readings.size()-calls in getters, that are called after toImmutable()
    // TODO !!!MMo - ????Consider reducing visibility of some getters???
    public SensorReadings toImmutable() {
        readings = Collections.unmodifiableList(readings);
        average = cumulativeAverage;
        size = readings.size();
        return this;
    }
}
