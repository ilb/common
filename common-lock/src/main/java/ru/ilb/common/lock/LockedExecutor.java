/*
 * Copyright 2020 slavb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ilb.common.lock;

import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * Execute code using read-write lock
 *
 * @author slavb
 */
public class LockedExecutor {

    private final StampedLockFactory<String> lockFactory;

    public LockedExecutor() {
        lockFactory = new StampedLockFactory<>();
    }

    public LockedExecutor(StampedLockFactory<String> lockFactory) {
        this.lockFactory = lockFactory;
    }

    /**
     * Execute code using lock key
     *
     * @param lockKey key for lock
     * @param check function to check validity of result
     * @param execute function to rebuild result
     */
    public void execute(String lockKey, Checker check, Builder execute) {
        execute(() -> lockFactory.getLock(lockKey), check, execute);
    }

    /**
     * Execute code using lock suplier
     *
     * @param lockSupplier supplier of lock
     * @param checker function to check validity of result
     * @param builder function to rebuild result
     */
    public void execute(Supplier<StampedLock> lockSupplier, Checker checker, Builder builder) {
        StampedLock lock = lockSupplier.get();
        long stamp = lock.readLock();
        try {
            if (!checker.valid()) {
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    stamp = lock.writeLock();
                }
                builder.run();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            lock.unlock(stamp);
        }

    }

    @FunctionalInterface
    interface Checker {

        boolean valid() throws Exception;
    }

    @FunctionalInterface
    interface Builder {

        void run() throws Exception;
    }
}
