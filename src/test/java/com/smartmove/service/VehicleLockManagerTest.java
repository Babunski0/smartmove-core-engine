package com.smartmove.service;

import com.smartmove.exception.SmartMoveException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.*;

class VehicleLockManagerTest {

    private final VehicleLockManager manager = new VehicleLockManager();

    @AfterEach
    void tearDown() {
        manager.clearAllLocks();
    }

    @Test
    void successfulLockAcquisition_andUnlock() {
        manager.lock("v1");
        assertTrue(manager.isLocked("v1"));
        manager.unlock("v1");
        assertFalse(manager.isLocked("v1"));
    }

    @Test
    void lockUnlockSequence() {
        assertFalse(manager.isLocked("v-seq"));
        manager.lock("v-seq");
        assertTrue(manager.isLocked("v-seq"));
        manager.unlock("v-seq");
        assertFalse(manager.isLocked("v-seq"));
    }

    @Test
    void lockTimeoutException_sameVehicle() throws Exception {
        ReflectionTestUtils.setField(manager, "lockTimeoutMs", 100L);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch locked = new CountDownLatch(1);

        Future<?> holder = pool.submit(() -> {
            manager.lock("v-timeout");
            locked.countDown();
            sleep(300);
            manager.unlock("v-timeout");
        });

        assertTrue(locked.await(1, TimeUnit.SECONDS));

        Future<?> waiter = pool.submit(() -> assertThrows(
                SmartMoveException.class,
                () -> manager.lock("v-timeout")
        ));

        holder.get(2, TimeUnit.SECONDS);
        waiter.get(2, TimeUnit.SECONDS);
        pool.shutdownNow();
    }

    @Test
    void concurrentLockAttempts_sameVehicle_onlyOneSucceeds() throws Exception {
        ReflectionTestUtils.setField(manager, "lockTimeoutMs", 100L);

        ExecutorService pool = Executors.newFixedThreadPool(2);

        CountDownLatch t1Locked = new CountDownLatch(1);
        CountDownLatch releaseT1 = new CountDownLatch(1);

        Future<?> t1 = pool.submit(() -> {
            manager.lock("v-same");
            t1Locked.countDown();          
            awaitLatch(releaseT1, 1, TimeUnit.SECONDS);
            manager.unlock("v-same");
        });

        assertTrue(t1Locked.await(1, TimeUnit.SECONDS), "t1 didn't acquire lock in time");

        Future<?> t2 = pool.submit(() ->
                assertThrows(SmartMoveException.class, () -> manager.lock("v-same"))
        );

        t2.get(1, TimeUnit.SECONDS);
        releaseT1.countDown();
        t1.get(1, TimeUnit.SECONDS);

        pool.shutdownNow();
    }

    private static void awaitLatch(CountDownLatch latch, long time, TimeUnit unit) {
        try {
            latch.await(time, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    @Test
    void concurrentOperations_differentVehicles_bothSucceed() throws Exception {
        ReflectionTestUtils.setField(manager, "lockTimeoutMs", 500L);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CyclicBarrier barrier = new CyclicBarrier(2);

        Future<?> t1 = pool.submit(() -> {
            await(barrier);
            manager.lock("v-a");
            sleep(150);
            manager.unlock("v-a");
        });

        Future<?> t2 = pool.submit(() -> {
            await(barrier);
            manager.lock("v-b");
            sleep(150);
            manager.unlock("v-b");
        });

        t1.get(2, TimeUnit.SECONDS);
        t2.get(2, TimeUnit.SECONDS);
        pool.shutdownNow();

        assertFalse(manager.isLocked("v-a"));
        assertFalse(manager.isLocked("v-b"));
    }

    @Test
    void exceptionHandling_releasesLock_withWithLock() {
        ReflectionTestUtils.setField(manager, "lockTimeoutMs", 500L);

        assertThrows(RuntimeException.class, () ->
                manager.withLock("v-ex", () -> { throw new RuntimeException("boom"); })
        );

        assertFalse(manager.isLocked("v-ex"));
    }

    @Test
    void configurableTimeoutValues_affectsBehavior() throws Exception {
        ReflectionTestUtils.setField(manager, "lockTimeoutMs", 50L);

        ExecutorService pool = Executors.newSingleThreadExecutor();
        manager.lock("v-config");

        Future<?> f = pool.submit(() -> assertThrows(SmartMoveException.class, () -> manager.lock("v-config")));
        f.get(1, TimeUnit.SECONDS);

        manager.unlock("v-config");
        pool.shutdownNow();
    }

    @Test
    void lockCounter_referenceTracking() {
        manager.lock("v1");
        manager.lock("v2");

        assertEquals(2, manager.getLockMapSize());
        assertEquals(1, manager.getWriteHoldCount("v1"));
        assertEquals(1, manager.getWriteHoldCount("v2"));

        manager.unlock("v1");
        manager.unlock("v2");

        assertEquals(0, manager.getWriteHoldCount("v1"));
        assertEquals(0, manager.getWriteHoldCount("v2"));
    }

    private static void sleep(long ms) {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ms);
        while (true) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) break;
            LockSupport.parkNanos(remaining);
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
        }
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
