package com.sissi.protocol.starttls;

import com.sissi.io.read.Metadata;
import com.sissi.protocol.Protocol;

/**
 * @author kim 2013年12月17日
 */
@Metadata(uri = Starttls.XMLNS, localName = Starttls.NAME)
public class Starttls extends Protocol {

	public final static String NAME = "starttls";

	public final static String XMLNS = "urn:ietf:params:xml:ns:xmpp-tls";
}
