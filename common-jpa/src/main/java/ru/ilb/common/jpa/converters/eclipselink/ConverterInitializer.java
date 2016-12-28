/*
 * Copyright 2016 slavb.
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
package ru.ilb.common.jpa.converters.eclipselink;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.internal.helper.DatabaseField;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.DirectToFieldMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.mappings.converters.SerializedObjectConverter;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.SessionEvent;
import org.eclipse.persistence.sessions.SessionEventListener;

/**
 *
 * @author slavb
 * @see
 * https://github.com/ancoron/pg-inet-maven/wiki/Support-custom-data-types-in-EclipseLink
 */
public class ConverterInitializer implements SessionEventListener {

    @Override
    public void missingDescriptor(SessionEvent event) {
        // no-op
    }

    @Override
    public void moreRowsDetected(SessionEvent event) {
        // no-op
    }

    @Override
    public void noRowsModified(SessionEvent event) {
        // no-op
    }

    @Override
    public void outputParametersDetected(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireExclusiveConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void postAcquireUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postBeginTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCalculateUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCalculateUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCommitTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void postCommitUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postConnect(SessionEvent event) {
        // no-op
    }

    @Override
    public void postExecuteQuery(SessionEvent event) {
        // no-op
    }

    @Override
    public void postReleaseClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void postReleaseUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postResumeUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void postRollbackTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void postDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void postMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void preBeginTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCommitTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preCommitUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preExecuteQuery(SessionEvent event) {
        // no-op
    }

    @Override
    public void prepareUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseClientSession(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseExclusiveConnection(SessionEvent event) {
        // no-op
    }

    @Override
    public void preReleaseUnitOfWork(SessionEvent event) {
        // no-op
    }

    @Override
    public void preRollbackTransaction(SessionEvent event) {
        // no-op
    }

    @Override
    public void preDistributedMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    @Override
    public void preMergeUnitOfWorkChangeSet(SessionEvent event) {
        // no-op
    }

    private Class getFieldType(final Class c, final String attributeName) {
        for (Field f : c.getDeclaredFields()) {
            if (f.getName().equals(attributeName)) {
                return f.getType();
            }
        }

        return null;
    }

    protected Converter getConverter(final Class c, final String attributeName) {
        Converter conv = null;
        Class type = getFieldType(c, attributeName);

        if (type != null) {
            // set converters as appropriate...
            if (UUID.class.isAssignableFrom(type)) {
                //TODO: platform dependent
                conv = new UUIDConverterMysql();
            } else {
                conv = null;
            }
        }

        return conv;
    }

    @Override
    public void preLogin(SessionEvent event) {
        Session s = event.getSession();
        s.getSessionLog().log(4, "ConverterInitializer: fixing database descriptor mappings...");
        Map<Class, ClassDescriptor> descriptorMap = s.getDescriptors();

        // we just walk through all descriptors...
        for (Map.Entry<Class, ClassDescriptor> entry : descriptorMap.entrySet()) {
            Class cls = entry.getKey();
            ClassDescriptor desc = entry.getValue();
            // walk through all mappings for some class...
            for (DatabaseMapping mapping : desc.getMappings()) {

                DirectToFieldMapping dfm = null;
                Converter conv = null;
                DatabaseField f = null;
                String attributeName = mapping.getAttributeName();

                if (mapping instanceof DirectToFieldMapping) {
                    dfm = (DirectToFieldMapping) mapping;
                    f = dfm.getField();
                    conv = dfm.getConverter();
                    // only consider mappings that are deemed to produce
                    // byte[] database fields from objects...
                    if (conv != null && conv instanceof SerializedObjectConverter) {
                        conv = getConverter(cls, attributeName);
                        if (conv != null) {
                            s.getSessionLog().log(4, "ConverterInitializer: using converter "
                                    + conv.getClass().getName()
                                    + " for field " + f.getTableName()
                                    + "." + f.getName());

                            dfm.setConverter(conv);
                        }
                    }
                }

            }
        }
    }

    @Override
    public void postLogin(SessionEvent event) {
        // no-op
    }

    @Override
    public void preLogout(SessionEvent se) {
        // no-op
    }

    @Override
    public void postLogout(SessionEvent se) {
        // no-op
    }
}
