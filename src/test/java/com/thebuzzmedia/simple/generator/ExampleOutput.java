package com.thebuzzmedia.simple.generator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thebuzzmedia.simple.generator.Library.Book;
import com.thebuzzmedia.simple.generator.Library.Book.Author;

public class ExampleOutput {
	private static IGenerator json_simplegen = new JSONGenerator();
	private static IGenerator xml_simplegen = new XMLGenerator();

	private static Gson gson_fmt = new GsonBuilder().setPrettyPrinting()
			.create();
	private static Gson gson_nofmt = new Gson();

	private static Marshaller jaxb_fmt;
	private static Marshaller jaxb_nofmt;

	private static Library library;

	static {
		try {
			JAXBContext context = JAXBContext.newInstance(Library.class);

			jaxb_fmt = context.createMarshaller();
			jaxb_fmt.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxb_nofmt = context.createMarshaller();
			jaxb_nofmt.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		setup();

		System.out.println("==== Example Output ====\n");

		System.out.println("[JSON]");
		json_gson();
		json_simplegen();

		System.out.println("[XML]");
		xml_jaxb();
		xml_simplegen();
	}

	private static void setup() {
		Author a1 = new Author("Douglas", "Preston",
				System.currentTimeMillis(),
				"http://www.amazon.com/Douglas-Preston/e/B000AQ0AWW/ref=ntt_athr_dp_pel_1");
		Author a2 = new Author(
				"Lincoln",
				"Child",
				System.currentTimeMillis(),
				"http://www.amazon.com/s/ref=ntt_athr_dp_sr_2?_encoding=UTF8&sort=relevancerank&search-alias=books&field-author=Lincoln%20Child");
		Author a3 = new Author("Nick", "Cook", System.currentTimeMillis(),
				"http://www.amazon.com/Nick-Cook/e/B001HCYOUM/ref=ntt_athr_dp_pel_1");

		Book b1 = new Book(Boolean.FALSE, "Riptide", "0446607177", 496,
				System.currentTimeMillis(), 9.95, a1, a2);
		Book b2 = new Book(Boolean.TRUE, "The Cabinet of Curiosities",
				"0446611239", 656, System.currentTimeMillis(), 5.95, a1);
		Book b3 = new Book(Boolean.TRUE, "The Hunt for Zero Point",
				"0767906284", 320, System.currentTimeMillis(), 11.95, a3);

		library = new Library("Library-a-rama",
				"1681 W Northbrook Dr, Tulsa, Oklahoma", b1, b2, b3);
	}

	private static void json_gson() {
		System.out
				.print("\nGson, Formatted\n===================================\n");
		System.out.println(gson_fmt.toJson(library));

		System.out.print("\nGson, Compact\n===================================\n");
		System.out.println(gson_nofmt.toJson(library));
	}

	private static void json_simplegen() {
		System.out
				.print("\nSimple Gen, Formatted\n===================================\n");
		json_simplegen.setIndenter(JSONIndenter.INSTANCE);
		System.out.println(json_simplegen.generate(library));

		System.out
				.print("\nSimple Gen, Compact\n===================================\n");
		json_simplegen.setIndenter(CompactIndenter.INSTANCE);
		System.out.println(json_simplegen.generate(library));
	}

	private static void xml_jaxb() {
		System.out
				.print("\nJAXB, Formatted\n===================================\n");
		try {
			jaxb_fmt.marshal(library, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.print("\nJAXB, Compact\n===================================\n");
		try {
			jaxb_nofmt.marshal(library, System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void xml_simplegen() {
		System.out
				.print("\nSimple Gen, Formatted\n===================================\n");
		xml_simplegen.setIndenter(XMLIndenter.INSTANCE);
		System.out.println(xml_simplegen.generate(library));

		System.out
				.print("\nSimple Gen, Compact\n===================================\n");
		xml_simplegen.setIndenter(CompactIndenter.INSTANCE);
		System.out.println(xml_simplegen.generate(library));
	}
}