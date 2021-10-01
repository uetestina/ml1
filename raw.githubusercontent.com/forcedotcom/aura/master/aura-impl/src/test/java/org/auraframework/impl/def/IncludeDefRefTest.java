/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.auraframework.impl.def;

import javax.inject.Inject;

import org.auraframework.def.DefDescriptor;
import org.auraframework.def.DefDescriptor.DefType;
import org.auraframework.def.IncludeDef;
import org.auraframework.def.IncludeDefRef;
import org.auraframework.def.LibraryDef;
import org.auraframework.impl.AuraImplTestCase;
import org.auraframework.impl.root.library.IncludeDefRefImpl;
import org.auraframework.impl.root.library.IncludeDefRefImpl.Builder;
import org.auraframework.impl.root.parser.handler.IncludeDefRefHandler;
import org.auraframework.impl.util.AuraTestingUtil;
import org.auraframework.impl.util.AuraTestingUtil.BundleEntryInfo;
import org.auraframework.service.CompilerService;
import org.auraframework.system.Source;
import org.auraframework.throwable.quickfix.InvalidDefinitionException;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class IncludeDefRefTest extends AuraImplTestCase {
    @Mock(answer = Answers.RETURNS_MOCKS)
    DefDescriptor<IncludeDefRef> descriptor;

    @Inject
    CompilerService compilerService;

    @Test
    public void testValidateDefintionWithoutDescriptor() throws Exception {
        Builder builder = new IncludeDefRefImpl.Builder();
        IncludeDefRef def = builder.build();

        try {
            def.validateDefinition();
            fail("IncludeDefRef without name not validated");
        } catch (InvalidDefinitionException t) {
            assertExceptionMessageEndsWith(t, InvalidDefinitionException.class,
                    String.format("%s must specify a name", IncludeDefRefHandler.TAG));
        }
    }

    @Test
    public void testValidateDefintionAliasIsInvalidIdentifier() throws Exception {
        AuraTestingUtil util = getAuraTestingUtil();
        Source<LibraryDef> source = util.buildBundleSource(util.getInternalNamespace(), LibraryDef.class,
                Lists.newArrayList(
                        new BundleEntryInfo(DefType.LIBRARY,
                                "<aura:library><aura:include name='test.js' aliases='who/came/up/with/this' /></aura:library>"),
                        new BundleEntryInfo(DefType.INCLUDE, "test", "function(){}")));

        InvalidDefinitionException expected = null;

        try {
            compilerService.compile(source.getDescriptor(), source);
        } catch (InvalidDefinitionException t) {
            expected = t;

            assertNotNull("IncludeDef with invalid aliases not validated", expected);
            assertExceptionMessageEndsWith(expected, InvalidDefinitionException.class, String.format(
                    "%s 'alias' attribute must contain only valid javascript identifiers", IncludeDefRefHandler.TAG));
        }
    }

    @Test
    public void testValidateDefintionAliasesIsJs() throws Exception {
        String source = "function(){}";
        Builder builder = new IncludeDefRefImpl.Builder();

        DefDescriptor<LibraryDef> libDesc = getAuraTestingUtil().createStringSourceDescriptor(null, LibraryDef.class,
                null);
        DefDescriptor<IncludeDef> includeDesc = getAuraTestingUtil().createStringSourceDescriptor("dummy",
                IncludeDef.class, libDesc);
        addSourceAutoCleanup(includeDesc, source);
        builder.setDescriptor(includeDesc);
        builder.setAliases(ImmutableList.of("(function(){alert('boo!')})()"));
        IncludeDefRef def = builder.build();

        try {
            def.validateDefinition();
            fail("IncludeDefRef with invalid export not validated");
        } catch (InvalidDefinitionException t) {
            assertExceptionMessageEndsWith(t, InvalidDefinitionException.class, String.format(
                    "%s 'alias' attribute must contain only valid javascript identifiers", IncludeDefRefHandler.TAG));
        }
    }

    @Test
    public void testValidateDefintionExportIsInvalidIdentifier() throws Exception {
        String source = "function(){}";

        Builder builder = new IncludeDefRefImpl.Builder();
        DefDescriptor<LibraryDef> libDesc = getAuraTestingUtil().createStringSourceDescriptor(null, LibraryDef.class,
                null);
        DefDescriptor<IncludeDef> includeDesc = getAuraTestingUtil().createStringSourceDescriptor("dummy",
                IncludeDef.class, libDesc);
        addSourceAutoCleanup(includeDesc, source);
        builder.setDescriptor(includeDesc);
        builder.setExport("who/came/up/with/this");
        IncludeDefRef def = builder.build();

        try {
            def.validateDefinition();
            fail("IncludeDefRef with invalid export not validated");
        } catch (InvalidDefinitionException t) {
            assertExceptionMessageEndsWith(t, InvalidDefinitionException.class, String
                    .format("%s 'export' attribute must be a valid javascript identifier", IncludeDefRefHandler.TAG));
        }
    }

    @Test
    public void testValidateDefintionExportIsJs() throws Exception {
        String source = "function(){}";

        Builder builder = new IncludeDefRefImpl.Builder();
        DefDescriptor<LibraryDef> libDesc = getAuraTestingUtil().createStringSourceDescriptor(null, LibraryDef.class,
                null);
        DefDescriptor<IncludeDef> includeDesc = getAuraTestingUtil().createStringSourceDescriptor("dummy",
                IncludeDef.class, libDesc);
        addSourceAutoCleanup(includeDesc, source);
        builder.setDescriptor(includeDesc);
        builder.setExport("(function(){alert('boo!')})()");
        IncludeDefRef def = builder.build();

        try {
            def.validateDefinition();
            fail("IncludeDefRef with invalid export not validated");
        } catch (Exception t) {
            String expectedMsg = String.format("%s 'export' attribute must be a valid javascript identifier",
                    IncludeDefRefHandler.TAG);
            assertExceptionMessageEndsWith(t, InvalidDefinitionException.class, expectedMsg);
        }
    }
}
