/*
 * Copyright 2014 the original author or authors.
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
package leap.htpl.processor;

import leap.htpl.ast.Attr;
import leap.htpl.ast.Element;
import leap.lang.Named;

public abstract class AbstractNamedAttrProcessor extends AbstractAttrProcessor implements Named {
	
	protected final String name;
	
	public AbstractNamedAttrProcessor(String name) {
		this.name = name;
	}

	@Override
    public final String getName() {
	    return name;
    }

	@Override
    public boolean supports(Element e, Attr a) {
	    return a.getLocalName().equalsIgnoreCase(name);
    }
	
}