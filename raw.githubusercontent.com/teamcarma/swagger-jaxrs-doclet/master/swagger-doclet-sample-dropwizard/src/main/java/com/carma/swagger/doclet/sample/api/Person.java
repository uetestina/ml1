package com.carma.swagger.doclet.sample.api;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * The Person represents an entity that uses different jsonviews
 * @version $Id$
 * @author conor.roche
 */
@SuppressWarnings("javadoc")
public class Person {

	// define 2 json views
	public static class SimplePersonView {
		// noop
	} // shows a summary view of a Person

	public static class DetailedPersonView extends SimplePersonView {
		// noop
	}

	String name;
	String address;
	int age;
	int height;

	@JsonView({ SimplePersonView.class })
	public String getName() {
		return this.name;
	}

	@JsonView({ DetailedPersonView.class })
	public String getAddress() {
		return this.address;
	}

	@JsonView({ DetailedPersonView.class })
	public int getAge() {
		return this.age;
	}

	/**
	 * This gets the height
	 * @return the height
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * This sets the height
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * This sets the name
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This sets the address
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * This sets the age
	 * @param age the age to set
	 */
	public void setAge(int age) {
		this.age = age;
	}

}
