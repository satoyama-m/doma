/*
 * Copyright 2004-2009 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.doma.entity;

import org.seasar.doma.domain.NumberDomain;

/**
 * @author taedium
 * 
 */
public class VersionProperty<D extends NumberDomain<?, ?>> extends
        BasicProperty<D> {

    public VersionProperty(String name, String columnName, D domain,
            boolean insertable, boolean updatable) {
        super(name, columnName, domain, insertable, updatable);
    }

    @Override
    public boolean isVersion() {
        return true;
    }

    public void setIfNecessary(Number value) {
        if (domain.isNull() || domain.get().intValue() < 0) {
            domain.set(value);
        }
    }

    public void increment() {
        if (domain.isNotNull()) {
            int i = domain.get().intValue();
            domain.set(i + 1);
        }
    }

}