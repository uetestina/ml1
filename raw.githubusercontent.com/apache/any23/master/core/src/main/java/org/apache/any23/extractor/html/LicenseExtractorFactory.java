/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.any23.extractor.html;

import java.util.Arrays;

import org.apache.any23.extractor.ExtractorDescription;
import org.apache.any23.extractor.ExtractorFactory;
import org.apache.any23.extractor.SimpleExtractorFactory;
import org.apache.any23.rdf.PopularPrefixes;
import org.apache.any23.rdf.Prefixes;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class LicenseExtractorFactory extends SimpleExtractorFactory<LicenseExtractor>
        implements ExtractorFactory<LicenseExtractor> {

    public static final String NAME = "html-mf-license";

    public static final Prefixes PREFIXES = PopularPrefixes.createSubset("xhtml");

    private static final ExtractorDescription descriptionInstance = new LicenseExtractorFactory();

    public LicenseExtractorFactory() {
        super(LicenseExtractorFactory.NAME, LicenseExtractorFactory.PREFIXES,
                Arrays.asList("text/html;q=0.01", "application/xhtml+xml;q=0.01"), "example-mf-license.html");
    }

    @Override
    public LicenseExtractor createExtractor() {
        return new LicenseExtractor();
    }

    public static ExtractorDescription getDescriptionInstance() {
        return descriptionInstance;
    }
}
