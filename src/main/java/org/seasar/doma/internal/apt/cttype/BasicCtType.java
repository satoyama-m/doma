/*
 * Copyright 2004-2010 the Seasar Foundation and the Others.
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
package org.seasar.doma.internal.apt.cttype;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

public class BasicCtType extends AbstractCtType {

    protected WrapperCtType wrapperCtType;

    public BasicCtType(TypeMirror type, ProcessingEnvironment env) {
        super(type, env);
    }

    public WrapperCtType getWrapperCtType() {
        return wrapperCtType;
    }

    public String getDefaultValue() {
        switch (typeMirror.getKind()) {
        case BOOLEAN:
            return String.valueOf(false);
        case BYTE:
        case SHORT:
        case INT:
        case LONG:
        case FLOAT:
        case DOUBLE:
        case CHAR:
            return "0";
        default:
            return "null";
        }
    }

    public static BasicCtType newInstance(TypeMirror type,
            ProcessingEnvironment env) {
        assertNotNull(type, env);
        BasicCtType basicCtType = new BasicCtType(type, env);
        WrapperCtType wrapperCtType = WrapperCtType.newInstance(basicCtType, env);
        if (wrapperCtType == null) {
            return null;
        }
        basicCtType.wrapperCtType = wrapperCtType;
        return basicCtType;
    }

    @Override
    public <R, P, TH extends Throwable> R accept(
            CtTypeVisitor<R, P, TH> visitor, P p) throws TH {
        return visitor.visitBasicCtType(this, p);
    }

}
