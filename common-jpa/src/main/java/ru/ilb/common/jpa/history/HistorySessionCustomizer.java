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

import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.history.HistoryPolicy;
import org.eclipse.persistence.sessions.Session;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Calls HistoryCustomizer.customize on all entities with @AutoHistory annotation
 * Usage: persistance.xml
 * &lt;property name="eclipselink.session.customizer" value="ru.ilb.common.jpa.history.HistorySessionCustomizer"/>
 *
 * @author slavb
 */
public class HistorySessionCustomizer implements SessionCustomizer {

    @Override
    public void customize(Session session) throws Exception {

        List<ClassDescriptor> descriptors = session.getDescriptors().values().stream()
                .filter(d -> AnnotationUtils.getAnnotation(d.getJavaClass(), AutoHistory.class) != null)
                .collect(Collectors.toList());
        descriptors.stream().forEach(d -> customize(session, d));
    }

    private void customize(Session session, ClassDescriptor descriptor) {
        HistoryPolicy policy = new HistoryPolicy();
        String primaryTable = descriptor.getTableName();
        if (primaryTable == null) {
            Class parentClass = descriptor.getInheritancePolicy().getParentClass();
            ClassDescriptor parentDescriptor = session.getClassDescriptor(parentClass);
            primaryTable = parentDescriptor.getTableName();
        }
        policy.addStartFieldName(primaryTable + ".ROWSTART");
        policy.addEndFieldName(primaryTable + ".ROWEND");
        policy.addHistoryTableName(primaryTable, primaryTable + "HIST");
        descriptor.setHistoryPolicy(policy);
    }

}
