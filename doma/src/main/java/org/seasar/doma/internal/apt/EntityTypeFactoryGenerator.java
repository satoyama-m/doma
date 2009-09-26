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
package org.seasar.doma.internal.apt;

import static org.seasar.doma.internal.util.AssertionUtil.*;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.seasar.doma.internal.apt.meta.ColumnMeta;
import org.seasar.doma.internal.apt.meta.EntityMeta;
import org.seasar.doma.internal.apt.meta.EntityPropertyMeta;
import org.seasar.doma.internal.apt.meta.IdGeneratorMeta;
import org.seasar.doma.internal.apt.meta.IdGeneratorMetaVisitor;
import org.seasar.doma.internal.apt.meta.IdentityIdGeneratorMeta;
import org.seasar.doma.internal.apt.meta.SequenceIdGeneratorMeta;
import org.seasar.doma.internal.apt.meta.TableIdGeneratorMeta;
import org.seasar.doma.internal.apt.type.DomainType;
import org.seasar.doma.internal.apt.type.WrapperType;
import org.seasar.doma.internal.jdbc.entity.AssignedIdPropertyType;
import org.seasar.doma.internal.jdbc.entity.BasicPropertyType;
import org.seasar.doma.internal.jdbc.entity.EntityType;
import org.seasar.doma.internal.jdbc.entity.EntityTypeFactory;
import org.seasar.doma.internal.jdbc.entity.GeneratedIdPropertyType;
import org.seasar.doma.internal.jdbc.entity.VersionPropertyType;
import org.seasar.doma.internal.util.PrimitiveWrapperUtil;

/**
 * 
 * @author taedium
 * 
 */
public class EntityTypeFactoryGenerator extends AbstractGenerator {

    protected final EntityMeta entityMeta;

    public EntityTypeFactoryGenerator(ProcessingEnvironment env,
            TypeElement entityElement, EntityMeta entityMeta)
            throws IOException {
        super(env, entityElement, null, null, Options.getEntitySuffix(env));
        assertNotNull(entityMeta);
        this.entityMeta = entityMeta;
    }

    public void generate() {
        printPackage();
        printClass();
    }

    protected void printPackage() {
        if (!packageName.isEmpty()) {
            iprint("package %1$s;%n", packageName);
            iprint("%n");
        }
    }

    protected void printClass() {
        printGenerated();
        iprint("public class %1$s implements %2$s<%3$s> {%n", simpleName,
                EntityTypeFactory.class.getName(), entityMeta
                        .getEntityTypeName());
        print("%n");
        indent();
        printMethods();
        printEntityTypeClass();
        unindent();
        iprint("}%n");
    }

    protected void printMethods() {
        iprint("@Override%n");
        iprint(
                "public %1$s<%2$s> createEntityType() {%n",
                org.seasar.doma.internal.jdbc.entity.EntityType.class.getName(),
                entityMeta.getEntityTypeName());
        iprint("    return new %1$sType();%n", entityMeta.getEntityName());
        iprint("}%n");
        print("%n");
        iprint("@Override%n");
        iprint(
                "public %1$s<%2$s> createEntityType(%2$s entity) {%n",
                org.seasar.doma.internal.jdbc.entity.EntityType.class.getName(),
                entityMeta.getEntityTypeName());
        iprint("    return new %1$sType(entity);%n", entityMeta.getEntityName());
        iprint("}%n");
        print("%n");
    }

    protected void printEntityTypeClass() {
        iprint("public static class %1$sType implements %2$s<%3$s> {%n",
                entityMeta.getEntityName(), EntityType.class.getName(),
                entityMeta.getEntityTypeName());
        print("%n");
        indent();
        printTypeClassFields();
        printTypeClassConstructors();
        printTypeClassMethods();
        unindent();
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassFields() {
        printTypeClassGeneratedIdPropertyField();
        printTypeClassListenerField();
        printTypeClassPropertyFields();
        printTypeClassEntityField();
        printTypeClassCatalogNameField();
        printTypeClassSchemaNameField();
        printTypeClassTableNameField();
        printTypeClassDirtyStatesField();
        printTypeClassNameField();
        printTypeClassPropertiesField();
        printTypeClassPropertyMapField();
    }

    protected void printTypeClassGeneratedIdPropertyField() {
        if (entityMeta.hasGeneratedIdPropertyMeta()) {
            EntityPropertyMeta propertyMeta = entityMeta
                    .getGeneratedIdPropertyMeta();
            IdGeneratorMeta idGeneratorMeta = propertyMeta.getIdGeneratorMeta();
            idGeneratorMeta.accept(new IdGeneratorGenerator(), null);
            print("%n");
        }
    }

    protected void printTypeClassListenerField() {
        iprint("private static final %1$s __listener = new %1$s();%n",
                entityMeta.getListenerTypeName());
        print("%n");
    }

    protected void printTypeClassEntityField() {
        iprint("private final %1$s __entity;%n", entityMeta.getEntityTypeName());
        print("%n");
    }

    protected void printTypeClassCatalogNameField() {
        iprint("private final String __catalogName = ");
        String catalogName = entityMeta.getTableMeta().getCatalog();
        if (catalogName != null) {
            print("\"%1$s\";%n", catalogName);
        } else {
            print("null;%n");
        }
        print("%n");
    }

    protected void printTypeClassSchemaNameField() {
        iprint("private final String __schemaName = ");
        String schemaName = entityMeta.getTableMeta().getSchema();
        if (schemaName != null) {
            print("\"%1$s\";%n", schemaName);
        } else {
            print("null;%n");
        }
        print("%n");
    }

    protected void printTypeClassTableNameField() {
        iprint("private final String __tableName = ");
        String tableName = entityMeta.getTableMeta().getName();
        if (tableName != null) {
            print("\"%1$s\";%n", tableName);
        } else {
            print("null;%n");
        }
        print("%n");
    }

    protected void printTypeClassDirtyStatesField() {
        iprint("private final java.util.Set<java.lang.String> __changedProperties;%n");
        print("%n");
    }

    protected void printTypeClassPropertyFields() {
        for (EntityPropertyMeta pm : entityMeta.getAllPropertyMetas()) {
            if (pm.isTrnsient()) {
                continue;
            }
            ColumnMeta cm = pm.getColumnMeta();
            String columnName = cm.getName();
            String quote = "\"";
            if (columnName == null) {
                columnName = "null";
                quote = "";
            }
            WrapperType wrapperType = null;
            if (pm.getDomainType() != null) {
                wrapperType = pm.getDomainType().getValueType()
                        .getWrapperType();
            } else {
                wrapperType = pm.getValueType().getWrapperType();
            }
            if (pm.isId()) {
                if (pm.getIdGeneratorMeta() != null) {
                    iprint(
                            "private final %1$s<%2$s> %3$s = new %1$s<%2$s>(\"%3$s\", %5$s%4$s%5$s, new %2$s(), __idGenerator);%n", /* 1 */
                            GeneratedIdPropertyType.class.getName(), /* 2 */
                            wrapperType.getTypeName(), /* 3 */pm.getName(), /* 4 */
                            columnName, /* 5 */quote);
                } else {
                    iprint(
                            "private final %1$s<%2$s> %3$s = new %1$s<%2$s>(\"%3$s\", %5$s%4$s%5$s, new %2$s());%n", /* 1 */
                            AssignedIdPropertyType.class.getName(), /* 2 */
                            wrapperType.getTypeName(), /* 3 */pm.getName(), /* 4 */
                            columnName, /* 5 */quote);
                }
            } else if (pm.isVersion()) {
                iprint(
                        "private final %1$s<%2$s> %3$s = new %1$s<%2$s>(\"%3$s\", %5$s%4$s%5$s, new %2$s());%n", /* 1 */
                        VersionPropertyType.class.getName(), /* 2 */wrapperType
                                .getTypeName(), /* 3 */pm.getName(), /* 4 */
                        columnName, /* 5 */quote);
            } else {
                iprint(
                        "private final %1$s<%2$s> %3$s = new %1$s<%2$s>(\"%3$s\", %7$s%4$s%7$s, new %2$s(), %5$s, %6$s);%n", /* 1 */
                        BasicPropertyType.class.getName(), /* 2 */wrapperType
                                .getTypeName(), /* 3 */pm.getName(), /* 4 */
                        columnName, /* 5 */cm.isInsertable(), /* 6 */cm
                                .isUpdatable(), /* 7 */quote);
            }
            print("%n");
        }
    }

    protected void printTypeClassNameField() {
        iprint("private final String __name = \"%1$s\";%n", entityMeta
                .getEntityName());
        print("%n");
    }

    protected void printTypeClassPropertiesField() {
        iprint("private java.util.List<%1$s<?>> __entityProperties;%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        print("%n");
    }

    protected void printTypeClassPropertyMapField() {
        iprint("private java.util.Map<String, %1$s<?>> __entityPropertyMap;%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        print("%n");
    }

    protected void printTypeClassConstructors() {
        iprint("private %1$sType() {%n", entityMeta.getEntityName());
        iprint("    this(new %1$s());%n", entityMeta.getEntityTypeName());
        iprint("}%n");
        print("%n");
        iprint("private %1$sType(%2$s entity) {%n", entityMeta.getEntityName(),
                entityMeta.getEntityTypeName());
        iprint("    __entity = entity;%n");
        String changedPropertyFieldName = entityMeta
                .getChangedPropertiesFieldName();
        if (changedPropertyFieldName != null) {
            iprint("    __changedProperties = entity.%1$s;%n",
                    changedPropertyFieldName);
        } else {
            iprint("    __changedProperties = null;%n");
        }
        for (EntityPropertyMeta pm : entityMeta.getAllPropertyMetas()) {
            if (pm.isTrnsient()) {
                continue;
            }
            DomainType domainType = pm.getDomainType();
            if (domainType != null) {
                iprint(
                        "    %1$s.getWrapper().set(entity.%1$s != null ? entity.%1$s.%2$s() : null);%n",
                        pm.getName(), domainType.getAccessorMetod());
            } else {
                iprint("    %1$s.getWrapper().set(entity.%1$s);%n", pm
                        .getName());
            }
        }
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassMethods() {
        printTypeClassGetNameMethod();
        printTypeClassGetCatalogNameMethod();
        printTypeClassGetSchemaNameMethod();
        printTypeClassGetTableNameMethod();
        printTypeClassPreInsertMethod();
        printTypeClassPreUpdateMethod();
        printTypeClassPreDeleteMethod();
        printTypeClassGetPropertyMetasMethod();
        printTypeClassGetPropertyMetaMethod();
        printTypeClassGetGeneratedIdPropertyMethod();
        printTypeClassGetVersionPropertyMethod();
        printTypeClassRefreshEntityMethod();
        printTypeClassRefreshEntityInternalMethod();
        printTypeClassGetEntityMethod();
        printTypeClassGetEntityClassMethod();
        printTypeClassGetChangedPropertiesMethod();
    }

    protected void printTypeClassGetNameMethod() {
        iprint("@Override%n");
        iprint("public String getName() {%n");
        iprint("    return __name;%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetCatalogNameMethod() {
        iprint("@Override%n");
        iprint("public String getCatalogName() {%n");
        iprint("    return __catalogName;%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetSchemaNameMethod() {
        iprint("@Override%n");
        iprint("public String getSchemaName() {%n");
        iprint("    return __schemaName;%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetTableNameMethod() {
        iprint("@Override%n");
        iprint("public String getTableName() {%n");
        iprint("    return __tableName;%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassPreInsertMethod() {
        iprint("@Override%n");
        iprint("public void preInsert() {%n");
        iprint("    __listener.preInsert(__entity);%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassPreUpdateMethod() {
        iprint("@Override%n");
        iprint("public void preUpdate() {%n");
        iprint("    __listener.preUpdate(__entity);%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassPreDeleteMethod() {
        iprint("@Override%n");
        iprint("public void preDelete() {%n");
        iprint("    __listener.preDelete(__entity);%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetPropertyMetasMethod() {
        iprint("@Override%n");
        iprint("public java.util.List<%1$s<?>> getEntityPropertyTypes() {%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        indent();
        iprint("if (__entityProperties == null) {%n");
        indent();
        iprint(
                "java.util.List<%1$s<?>> __list = new java.util.ArrayList<%1$s<?>>();%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        for (EntityPropertyMeta pm : entityMeta.getAllPropertyMetas()) {
            if (pm.isTrnsient()) {
                continue;
            }
            iprint("__list.add(%1$s);%n", pm.getName());
        }
        iprint("__entityProperties = java.util.Collections.unmodifiableList(__list);%n");
        unindent();
        iprint("}%n");
        iprint("return __entityProperties;%n");
        unindent();
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetPropertyMetaMethod() {
        iprint("@Override%n");
        iprint("public %1$s<?> getEntityPropertyType(String __name) {%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        indent();
        iprint("if (__entityPropertyMap == null) {%n");
        indent();
        iprint(
                "java.util.Map<String, %1$s<?>> __map = new java.util.HashMap<String, %1$s<?>>();%n",
                org.seasar.doma.internal.jdbc.entity.EntityPropertyType.class
                        .getName());
        for (EntityPropertyMeta pm : entityMeta.getAllPropertyMetas()) {
            if (pm.isTrnsient()) {
                continue;
            }
            iprint("__map.put(\"%1$s\", %1$s);%n", pm.getName());
        }
        iprint("__entityPropertyMap = java.util.Collections.unmodifiableMap(__map);%n");
        unindent();
        iprint("}%n");
        iprint("return __entityPropertyMap.get(__name);%n");
        unindent();
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetGeneratedIdPropertyMethod() {
        iprint("@Override%n");
        iprint("public %1$s<?> getGeneratedIdPropertyType() {%n",
                GeneratedIdPropertyType.class.getName());
        String idName = "null";
        if (entityMeta.hasGeneratedIdPropertyMeta()) {
            EntityPropertyMeta pm = entityMeta.getGeneratedIdPropertyMeta();
            idName = pm.getName();
        }
        iprint("    return %1$s;%n", idName);
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetVersionPropertyMethod() {
        iprint("@Override%n");
        iprint("public %1$s<?> getVersionPropertyType() {%n",
                VersionPropertyType.class.getName());
        String versionName = "null";
        if (entityMeta.hasVersionPropertyMeta()) {
            EntityPropertyMeta pm = entityMeta.getVersionPropertyMeta();
            versionName = pm.getName();
        }
        iprint("    return %1$s;%n", versionName);
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassRefreshEntityMethod() {
        iprint("@Override%n");
        iprint("public void refreshEntity() {%n");
        iprint("    refreshEntityInternal();%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassRefreshEntityInternalMethod() {
        iprint("public void refreshEntityInternal() {%n");
        for (EntityPropertyMeta pm : entityMeta.getAllPropertyMetas()) {
            if (pm.isTrnsient()) {
                continue;
            }
            DomainType domainType = pm.getDomainType();
            if (domainType != null) {
                if (domainType.getValueType().getType().getKind().isPrimitive()) {
                    iprint(
                            "    __entity.%1$s = new %2$s(%3$s.toPrimitive(%1$s.getWrapper().get()));%n",
                            pm.getName(), pm.getTypeName(),
                            PrimitiveWrapperUtil.class.getName());
                } else {
                    iprint(
                            "    __entity.%1$s = new %2$s(%1$s.getWrapper().get());%n",
                            pm.getName(), pm.getTypeName());
                }
            } else {
                if (pm.getValueType().getType().getKind().isPrimitive()) {
                    iprint(
                            "    __entity.%1$s = %2$s.toPrimitive(%1$s.getWrapper().get());%n",
                            pm.getName(), PrimitiveWrapperUtil.class.getName());
                } else {
                    iprint("    __entity.%1$s = %1$s.getWrapper().get();%n", pm
                            .getName());
                }
            }
        }
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetEntityMethod() {
        iprint("@Override%n");
        iprint("public %1$s getEntity() {%n", entityMeta.getEntityTypeName());
        iprint("    refreshEntityInternal();%n");
        iprint("    return __entity;%n");
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetEntityClassMethod() {
        iprint("@Override%n");
        iprint("public Class<%1$s> getEntityClass() {%n", entityMeta
                .getEntityTypeName());
        iprint("    return %1$s.class;%n", entityMeta.getEntityTypeName());
        iprint("}%n");
        print("%n");
    }

    protected void printTypeClassGetChangedPropertiesMethod() {
        iprint("@Override%n");
        iprint("public java.util.Set<String> getChangedProperties() {%n");
        iprint("    return __changedProperties;%n");
        iprint("}%n");
        print("%n");
    }

    protected class IdGeneratorGenerator implements
            IdGeneratorMetaVisitor<Void, Void> {

        @Override
        public Void visistIdentityIdGeneratorMeta(IdentityIdGeneratorMeta m,
                Void p) {
            iprint("private static final %1$s __idGenerator = new %1$s();%n", m
                    .getIdGeneratorClassName());
            return null;
        }

        @Override
        public Void visistSequenceIdGeneratorMeta(SequenceIdGeneratorMeta m,
                Void p) {
            iprint("private static final %1$s __idGenerator = new %1$s();%n", m
                    .getIdGeneratorClassName());
            iprint("static {%n");
            indent();
            iprint("__idGenerator.setQualifiedSequenceName(\"%1$s\");%n", m
                    .getQualifiedSequenceName());
            iprint("__idGenerator.setInitialValue(%1$s);%n", m
                    .getInitialValue());
            iprint("__idGenerator.setAllocationSize(%1$s);%n", m
                    .getAllocationSize());
            iprint("__idGenerator.initialize();%n");
            unindent();
            iprint("}%n");
            return null;
        }

        @Override
        public Void visistTableIdGeneratorMeta(TableIdGeneratorMeta m, Void p) {
            iprint("private static final %1$s __idGenerator = new %1$s();%n", m
                    .getIdGeneratorClassName());
            iprint("static {%n");
            indent();
            iprint("__idGenerator.setQualifiedTableName(\"%1$s\");%n", m
                    .getQualifiedTableName());
            iprint("__idGenerator.setInitialValue(%1$s);%n", m
                    .getInitialValue());
            iprint("__idGenerator.setAllocationSize(%1$s);%n", m
                    .getAllocationSize());
            iprint("__idGenerator.setPkColumnName(\"%1$s\");%n", m
                    .getPkColumnName());
            iprint("__idGenerator.setPkColumnValue(\"%1$s\");%n", m
                    .getPkColumnValue());
            iprint("__idGenerator.setValueColumnName(\"%1$s\");%n", m
                    .getValueColumnName());
            iprint("__idGenerator.initialize();%n");
            unindent();
            iprint("}%n");
            return null;
        }
    }

}
