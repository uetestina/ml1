package com.sissi.protocol.iq.vcard;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.sissi.field.Field;
import com.sissi.field.Fields;
import com.sissi.field.impl.BeanFields;
import com.sissi.io.read.Collector;
import com.sissi.io.read.Metadata;
import com.sissi.protocol.Protocol;
import com.sissi.protocol.iq.data.XInput;
import com.sissi.protocol.iq.data.XNickname;
import com.sissi.protocol.iq.vcard.field.Photo;
import com.sissi.protocol.iq.vcard.field.muc.Activate;

/**
 * @author kim 2013年12月5日
 */
@Metadata(uri = VCard.XMLNS, localName = VCard.NAME)
@XmlType(namespace = VCard.XMLNS)
@XmlRootElement(name = VCard.NAME)
public class VCard extends Protocol implements Fields, Collector {

	public final static String XMLNS = "vcard-temp";

	public final static String NAME = "vCard";

	private final BeanFields fields = new BeanFields(false);

	@XmlAttribute
	public String getXmlns() {
		return XMLNS;
	}

	@XmlElements({ @XmlElement(name = Activate.NAME, type = Activate.class),@XmlElement(name = XInput.NAME, type = XInput.class), @XmlElement(name = XNickname.NAME, type = XNickname.class), @XmlElement(name = Photo.NAME, type = Photo.class) })
	public List<Field<?>> getFields() {
		return this.fields.getFields();
	}

	public VCard add(Field<?> field) {
		this.fields.add(field);
		return this;
	}

	@Override
	public void set(String localName, Object ob) {
		this.fields.add(Field.class.cast(ob));
	}

	public Fields addField(Field<?> field) {
		this.fields.add(field);
		return this;
	}

	@Override
	public Iterator<Field<?>> iterator() {
		return this.fields.iterator();
	}

	@Override
	public boolean isEmbed() {
		return this.fields.isEmbed();
	}

	public boolean isEmpty() {
		return this.fields.isEmpty();
	}

	public Fields findFields(String name) {
		return this.fields.findFields(name);
	}

	@Override
	public <T extends Field<?>> T findField(String name, Class<T> clazz) {
		return this.fields.findField(name, clazz);
	}
}
