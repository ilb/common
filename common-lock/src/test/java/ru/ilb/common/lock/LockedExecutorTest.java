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
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author slavb
 */
public class LockedExecutorTest {

    private static final StampedLockFactory<String> LOCK_FACTORY = new StampedLockFactory<>();

    private int testvar = 0;

    public LockedExecutorTest() {
    }

    /**
     * Test of execute method, of class LockedExecutor.
     */
    @Test
    public void testExecute() {
        System.out.println("execute");
        String lockKey = "test";
        Supplier<StampedLock> lockSupplier = () -> LOCK_FACTORY.getLock(lockKey);
        boolean valid = true;
        testvar = 0;
        LockedExecutor instance = new LockedExecutor();
        instance.execute(lockSupplier,
                () -> valid,
                () -> testvar = 2);
        assertEquals(0, testvar);
        instance.execute(lockSupplier,
                () -> false,
                () -> testvar = 2);
        assertEquals(2, testvar);
    }

    @Test
    public void testExecuteString() {
        System.out.println("execute");
        String lockKey = "test";
        boolean valid = true;
        testvar = 0;
        LockedExecutor instance = new LockedExecutor();
        instance.execute(lockKey,
                () -> valid,
                () -> testvar = 2);
        assertEquals(0, testvar);
        instance.execute(lockKey,
                () -> false,
                () -> testvar = 2);
        assertEquals(2, testvar);
    }
}
