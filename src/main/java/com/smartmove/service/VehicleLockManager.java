package com.smartmove.service;

import com.smartmove.exception.SmartMoveException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jniyi
 * @project smartmove-core-engine - 2026
 * @created 16.02.2026
 */

@Component
@Slf4j
public class VehicleLockManager {

    /**
     * Map of locks per vehicle ID
     */
    private final ConcurrentHashMap<String, ReadWriteLock> locks = new ConcurrentHashMap<>();

    /**
     * Lock acquisition timeout in milliseconds
     */
    @Value("${smartmove.concurrency.lock-timeout-ms:5000}")
    private long lockTimeoutMs;

    /**
     * Get or create a lock for the vehicle
     */
    private ReadWriteLock getLock(String vehicleId) {
        return locks.computeIfAbsent(vehicleId, key -> new ReentrantReadWriteLock());
    }

    /**
     * Lock vehicle for exclusive access (for create/update/delete operations)
     */
    public void lock(String vehicleId) {
        log.debug("Locking vehicle: {}", vehicleId);

        ReadWriteLock lock = getLock(vehicleId);
        try {
            boolean acquired = lock.writeLock().tryLock();
            if (!acquired) {
                log.warn("Failed to lock vehicle: {}", vehicleId);
                throw new SmartMoveException("Could not lock vehicle: " + vehicleId);
            }
            log.debug("Vehicle locked: {}", vehicleId);
        } catch (Exception e) {
            log.error("Error locking vehicle: {}", e.getMessage());
            throw new SmartMoveException("Failed to lock vehicle", e);
        }
    }

    /**
     * Unlock vehicle
     */
    public void unlock(String vehicleId) {
        log.debug("Unlocking vehicle: {}", vehicleId);

        ReadWriteLock lock = locks.get(vehicleId);
        if (lock != null) {
            try {
                lock.writeLock().unlock();
                log.debug("Vehicle unlocked: {}", vehicleId);
            } catch (Exception e) {
                log.error("Error unlocking vehicle: {}", e.getMessage());
            }
        }
    }

    /**
     * Check if vehicle is locked
     */
    public boolean isLocked(String vehicleId) {
        ReadWriteLock lock = locks.get(vehicleId);
        if (lock instanceof ReentrantReadWriteLock) {
            ReentrantReadWriteLock rwLock = (ReentrantReadWriteLock) lock;
            return rwLock.getReadLockCount() > 0 || rwLock.isWriteLocked();
        }
        return false;
    }

    /**
     * Clear all locks
     */
    public void clearAllLocks() {
        log.warn("Clearing all locks");
        locks.clear();
    }
}
