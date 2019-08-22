/*
 * Copyright 2017 slavb.
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
package ru.ilb.common.jpa.history;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.history.HistoryPolicy;
import org.eclipse.persistence.config.DescriptorCustomizer;

/**
 * Configure history on descriptor. See also HistorySessionCustomizer
 * Usage: annotate entity with
 * @Customizer(ru.ilb.common.jpa.history.HistoryCustomizer.class)
 * @author slavb
 */
public class HistoryCustomizer implements DescriptorCustomizer {

    @Override
    public void customize(ClassDescriptor descriptor) {
        HistoryPolicy policy = new HistoryPolicy();
        String primaryTable = descriptor.getTableName();
        policy.addStartFieldName(primaryTable + ".ROWSTART");
        policy.addEndFieldName(primaryTable + ".ROWEND");
        policy.addHistoryTableName(primaryTable, primaryTable + "HIST");
        descriptor.setHistoryPolicy(policy);
    }
}
