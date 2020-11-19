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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author slavb
 */
public class ReadWriteLockFactory<K> {

    private final ConcurrentHashMap<K, ReentrantReadWriteLock> locks = new ConcurrentHashMap<>();

    public ReadWriteLock getLock(K key) {
        ReentrantReadWriteLock lock;

        synchronized (locks) {
            lock = locks.get(key);
            if (lock == null) {
                lock = new ReentrantReadWriteLock(true);
                locks.put(key, lock);
            }
        }
        return lock;
    }
}
