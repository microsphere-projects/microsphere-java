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
package io.microsphere.concurrent;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A delegating implementation of {@link ScheduledExecutorService} that forwards all method calls to a provided
 * delegate instance. This class can be extended or used directly to wrap an existing {@link ScheduledExecutorService},
 * allowing for additional behavior to be added (e.g., monitoring, logging) without modifying the underlying executor.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * ScheduledExecutorService realExecutor = Executors.newScheduledThreadPool(2);
 * DelegatingScheduledExecutorService delegatingExecutor = new DelegatingScheduledExecutorService(realExecutor);
 *
 * // Schedule a task to run after 1 second
 * delegatingExecutor.schedule(() -> System.out.println("Task executed!"), 1, TimeUnit.SECONDS);
 *
 * // Schedule a fixed-rate task
 * delegatingExecutor.scheduleAtFixedRate(() -> System.out.println("Fixed rate task"), 0, 1, TimeUnit.SECONDS);
 *
 * // Shutdown the executor gracefully
 * delegatingExecutor.shutdown();
 * }</pre>
 *
 * <p>This class is thread-safe and allows the delegate to be changed dynamically via
 * {@link #setDelegate(ScheduledExecutorService)}.</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ScheduledExecutorService
 * @since 1.0.0
 */
public class DelegatingScheduledExecutorService implements ScheduledExecutorService {

    private volatile ScheduledExecutorService delegate;

    public DelegatingScheduledExecutorService(@Nonnull ScheduledExecutorService delegate) {
        this.delegate = delegate;
    }

    public void setDelegate(@Nonnull ScheduledExecutorService delegate) {
        this.delegate = delegate;
    }

    @Nonnull
    public ScheduledExecutorService getDelegate() {
        return delegate;
    }

    @Override
    @Nonnull
    public ScheduledFuture<?> schedule(@Nonnull Runnable command, long delay, @Nonnull TimeUnit unit) {
        return getDelegate().schedule(command, delay, unit);
    }

    @Override
    @Nonnull
    public <V> ScheduledFuture<V> schedule(@Nonnull Callable<V> callable, long delay, @Nonnull TimeUnit unit) {
        return getDelegate().schedule(callable, delay, unit);
    }

    @Override
    @Nonnull
    public ScheduledFuture<?> scheduleAtFixedRate(@Nonnull Runnable command, long initialDelay, long period, @Nonnull TimeUnit unit) {
        return getDelegate().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    @Override
    @Nonnull
    public ScheduledFuture<?> scheduleWithFixedDelay(@Nonnull Runnable command, long initialDelay, long delay, @Nonnull TimeUnit unit) {
        return getDelegate().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    @Override
    public void shutdown() {
        getDelegate().shutdown();
    }

    @Override
    @Nonnull
    public List<Runnable> shutdownNow() {
        return getDelegate().shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return getDelegate().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return getDelegate().isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return getDelegate().awaitTermination(timeout, unit);
    }

    @Override
    @Nonnull
    public <T> Future<T> submit(@Nonnull Callable<T> task) {
        return getDelegate().submit(task);
    }

    @Override
    @Nonnull
    public <T> Future<T> submit(@Nonnull Runnable task, @Nullable T result) {
        return getDelegate().submit(task, result);
    }

    @Override
    @Nonnull
    public Future<?> submit(@Nonnull Runnable task) {
        return getDelegate().submit(task);
    }

    @Override
    @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return getDelegate().invokeAll(tasks);
    }

    @Override
    @Nonnull
    public <T> List<Future<T>> invokeAll(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException {
        return getDelegate().invokeAll(tasks, timeout, unit);
    }

    @Override
    @Nullable
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return getDelegate().invokeAny(tasks);
    }

    @Override
    @Nullable
    public <T> T invokeAny(@Nonnull Collection<? extends Callable<T>> tasks, long timeout, @Nonnull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return getDelegate().invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(@Nonnull Runnable command) {
        getDelegate().execute(command);
    }
}
