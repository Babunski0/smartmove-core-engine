package com.smartmove.service;

import com.smartmove.exception.SmartMoveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Component
@Slf4j
public class VehicleLockManager {

    private final ConcurrentHashMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    @Value("${smartmove.concurrency.lock-timeout-ms:5000}")
    private long lockTimeoutMs;

    private ReadWriteLock getLock(String vehicleId) {
        return locks.computeIfAbsent(vehicleId, key -> new ReentrantReadWriteLock());
    }

    public void lock(String vehicleId) {
        log.debug("Locking vehicle: {}", vehicleId);

        ReadWriteLock lock = getLock(vehicleId);
        ReentrantReadWriteLock rw = (ReentrantReadWriteLock) lock;

        try {
            boolean acquired = rw.writeLock().tryLock(lockTimeoutMs, TimeUnit.MILLISECONDS);
            if (!acquired) {
                log.warn("Lock timeout for vehicle: {}", vehicleId);
                throw new SmartMoveException("Lock timeout for vehicle: " + vehicleId);
            }
            log.debug("Vehicle locked: {}", vehicleId);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new SmartMoveException("Interrupted while locking vehicle: " + vehicleId, ie);
        } catch (SmartMoveException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error locking vehicle: {}", e.getMessage(), e);
            throw new SmartMoveException("Failed to lock vehicle: " + vehicleId, e);
        }
    }

    public void unlock(String vehicleId) {
        log.debug("Unlocking vehicle: {}", vehicleId);

        ReadWriteLock lock = locks.get(vehicleId);
        if (!(lock instanceof ReentrantReadWriteLock rw)) {
            return;
        }

        try {
            if (rw.isWriteLockedByCurrentThread()) {
                rw.writeLock().unlock();
                log.debug("Vehicle unlocked: {}", vehicleId);
            } else {
                log.warn("Unlock called but current thread does not hold write lock: {}", vehicleId);
            }
        } catch (Exception e) {
            log.error("Error unlocking vehicle {}: {}", vehicleId, e.getMessage(), e);
        }
    }

    public boolean isLocked(String vehicleId) {
        ReadWriteLock lock = locks.get(vehicleId);
        if (lock instanceof ReentrantReadWriteLock rwLock) {
            return rwLock.getReadLockCount() > 0 || rwLock.isWriteLocked();
        }
        return false;
    }

    public void clearAllLocks() {
        log.warn("Clearing all locks");
        locks.clear();
    }

    public <T> T withLock(String vehicleId, Supplier<T> action) {
        lock(vehicleId);
        try {
            return action.get();
        } finally {
            unlock(vehicleId);
        }
    }

    int getLockMapSize() {
        return locks.size();
    }

    int getWriteHoldCount(String vehicleId) {
        ReadWriteLock lock = locks.get(vehicleId);
        if (lock instanceof ReentrantReadWriteLock rw) {
            return rw.getWriteHoldCount();
        }
        return 0;
    }
}
