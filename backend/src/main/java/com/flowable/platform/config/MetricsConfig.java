package com.flowable.platform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Prometheus metrics in the Flowable platform.
 *
 * This class registers custom Flowable-related metrics that are exposed
 * via the /actuator/prometheus endpoint for monitoring and observability.
 */
@Configuration
public class MetricsConfig {

    public static final String FLOWABLE_PROCESS_STARTED = "flowable.process.started";
    public static final String FLOWABLE_TASK_COMPLETED = "flowable.task.completed";
    public static final String FLOWABLE_PROCESS_ACTIVE = "flowable.process.active";

    private final AtomicInteger activeProcessInstances = new AtomicInteger(0);

    /**
     * Registers a counter for process instances started.
     *
     * This counter tracks the total number of process instances that have been
     * started in the Flowable platform.
     *
     * @param meterRegistry the MeterRegistry to register metrics
     * @return a Counter for process instances started
     */
    @Bean
    public Counter processStartedCounter(MeterRegistry meterRegistry) {
        return Counter.builder(FLOWABLE_PROCESS_STARTED)
                .description("Total number of process instances started")
                .tag("component", "flowable")
                .register(meterRegistry);
    }

    /**
     * Registers a counter for tasks completed.
     *
     * This counter tracks the total number of tasks that have been completed
     * in the Flowable platform.
     *
     * @param meterRegistry the MeterRegistry to register metrics
     * @return a Counter for tasks completed
     */
    @Bean
    public Counter taskCompletedCounter(MeterRegistry meterRegistry) {
        return Counter.builder(FLOWABLE_TASK_COMPLETED)
                .description("Total number of tasks completed")
                .tag("component", "flowable")
                .register(meterRegistry);
    }

    /**
     * Registers a gauge for active process instances.
     *
     * This gauge tracks the current number of active (running) process instances
     * in the Flowable platform at any given time.
     *
     * @param meterRegistry the MeterRegistry to register metrics
     * @return the AtomicInteger holding the active process instance count
     */
    @Bean
    public AtomicInteger activeProcessInstancesGauge(MeterRegistry meterRegistry) {
        Gauge.builder(FLOWABLE_PROCESS_ACTIVE, activeProcessInstances, AtomicInteger::get)
                .description("Current number of active process instances")
                .tag("component", "flowable")
                .register(meterRegistry);
        return activeProcessInstances;
    }

    /**
     * Gets the counter for tracking started process instances.
     * This should be used to increment the counter when a process starts.
     *
     * @param meterRegistry the MeterRegistry
     * @return the Counter for process started
     */
    public Counter getProcessStartedCounter(MeterRegistry meterRegistry) {
        return meterRegistry.find(FLOWABLE_PROCESS_STARTED).counter();
    }

    /**
     * Gets the counter for tracking completed tasks.
     * This should be used to increment the counter when a task completes.
     *
     * @param meterRegistry the MeterRegistry
     * @return the Counter for task completed
     */
    public Counter getTaskCompletedCounter(MeterRegistry meterRegistry) {
        return meterRegistry.find(FLOWABLE_TASK_COMPLETED).counter();
    }

    /**
     * Increments the active process instance count.
     * This should be called when a new process instance is created.
     */
    public void incrementActiveProcessInstances() {
        activeProcessInstances.incrementAndGet();
    }

    /**
     * Decrements the active process instance count.
     * This should be called when a process instance completes or is terminated.
     */
    public void decrementActiveProcessInstances() {
        activeProcessInstances.decrementAndGet();
    }

    /**
     * Gets the current count of active process instances.
     *
     * @return the current number of active process instances
     */
    public int getActiveProcessInstancesCount() {
        return activeProcessInstances.get();
    }
}
