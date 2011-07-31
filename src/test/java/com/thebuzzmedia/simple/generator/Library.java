package com.thebuzzmedia.simple.generator;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

// Added for JAXB comparison benchmark.
@XmlRootElement
public class Library implements IRecursable {
	public String name;
	public String address;
	public List<Book> books;

	public Library() {
		// no-arg default constructor.
	}

	public Library(String name, String address, Book... books) {
		this.name = name;
		this.address = address;
		this.books = Arrays.asList(books);
	}

	public static class Book implements IRecursable {
		public Boolean checkedOut;
		public String title;
		public String isbn;
		public Integer pageCount;
		public Long printDate;
		public List<Author> authors;
		public Double replacementCost;

		public Book() {
			// no-arg default constructor.
		}

		public Book(Boolean checkedOut, String title, String isbn,
				Integer pageCount, long printDateMillis,
				Double replacementCost, Author... authors) {
			this.checkedOut = checkedOut;
			this.title = title;
			this.isbn = isbn;
			this.pageCount = pageCount;
			this.printDate = printDateMillis;
			this.replacementCost = replacementCost;
			this.authors = Arrays.asList(authors);
		}

		public static class Author implements IRecursable {
			public String firstName;
			public String lastName;
			public Long dob;
			public String amazonURL;

			public Author() {
				// no-arg default constructor.
			}

			public Author(String fName, String lName, long dobMillis,
					String amazonURL) {
				this.firstName = fName;
				this.lastName = lName;
				this.dob = dobMillis;
				this.amazonURL = amazonURL;
			}
		}
	}
}