package org.nem.ncc.addressbook;

import org.hamcrest.core.IsEqual;
import org.junit.*;
import org.nem.core.serialization.*;
import org.nem.ncc.test.*;

import java.util.*;
import java.util.function.Consumer;

public class MemoryAddressBookTest extends AddressBookTest {

	//region createAddressBook

	@Override
	protected AddressBook createAddressBook(final AddressBookName name) {
		return new MemoryAddressBook(name);
	}

	@Override
	protected AddressBook createAddressBook(final AddressBookName name, final Collection<AccountLabel> accountLabels) {
		return new MemoryAddressBook(name, accountLabels);
	}

	//endregion

	//region getFileExtension

	@Test
	public void getFileExtensionReturnsAddressBookFileExtension() {
		// Arrange:
		final MemoryAddressBook addressBook = new MemoryAddressBook(new AddressBookName("bar"));

		// Assert:
		Assert.assertThat(addressBook.getFileExtension(), IsEqual.equalTo(new AddressBookFileExtension(".adb")));
	}

	//endregion

	//region deserialization

	@Test
	public void addressBookCanBeRoundTripped() {
		// Act:
		final List<AccountLabel> accountLabels = this.createAccountLabels(3);
		final AddressBook originalAddressBook = this.createAddressBook(new AddressBookName("bar"), accountLabels);

		// Act:
		final MemoryAddressBook addressBook = new MemoryAddressBook(Utils.roundtripSerializableEntity(originalAddressBook, null));

		// Assert:
		Assert.assertThat(addressBook.getName(), IsEqual.equalTo(new AddressBookName("bar")));
		Assert.assertThat(addressBook.getAccountLabels(), IsEquivalent.equivalentTo(originalAddressBook.getAccountLabels()));
	}

	@Test
	public void addressBookCannotBeDeserializedWithInvalidAccountLabels() {
		final List<AccountLabel> accountLabels = this.createAccountLabels(1);
		accountLabels.add(accountLabels.get(0));

		// Assert: that other accounts are validated as part of deserialization
		assertThrowsAddressBookException(
				v -> createAddressBookFromJson("bar", accountLabels),
				AddressBookException.Code.ADDRESS_BOOK_ALREADY_CONTAINS_ADDRESS);
	}

	@Test
	public void walletCannotBeDeserializedWithMissingRequiredParameters() {
		// Arrange:
		final List<AccountLabel> accountLabels = this.createAccountLabels(3);
		final List<Consumer<Void>> actions = Arrays.asList(
				v -> createAddressBookFromJson(null, accountLabels),
				v -> createAddressBookFromJson("bar", null));

		// Assert:
		for (final Consumer<Void> action : actions) {
			ExceptionAssert.assertThrows(v -> action.accept(null), SerializationException.class);
		}
	}

	private static AddressBook createAddressBookFromJson(
			final String name,
			final List<AccountLabel> accountLabels) {
		final JsonSerializer serializer = new JsonSerializer();
		if (null != name) {
			serializer.writeString("addressBook", name);
		}

		if (null != accountLabels) {
			serializer.writeObjectArray("accountLabels", accountLabels);
		}

		return new MemoryAddressBook(Utils.createDeserializer(serializer.getObject()));
	}

	//endregion
}
