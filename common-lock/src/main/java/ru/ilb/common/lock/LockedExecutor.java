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
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
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

    public void execute(String lockKey, BooleanSupplier check, Runnable execute) {
        execute(() -> lockFactory.getLock(lockKey), check, execute);
    }

    public void execute(Supplier<StampedLock> lockSupplier, BooleanSupplier check, Runnable execute) {

        StampedLock lock = lockSupplier.get();
        long stamp = lock.readLock();
        try {
            if (!check.getAsBoolean()) {
                stamp = lock.tryConvertToWriteLock(stamp);
                if (stamp == 0L) {
                    stamp = lock.writeLock();
                }
                execute.run();
            }
        } finally {
            lock.unlock(stamp);
        }

    }
}
