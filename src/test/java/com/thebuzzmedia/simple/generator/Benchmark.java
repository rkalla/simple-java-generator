package com.thebuzzmedia.simple.generator;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thebuzzmedia.simple.generator.Library.Book;
import com.thebuzzmedia.simple.generator.Library.Book.Author;

public class Benchmark {
	private static final int ITERS = 50000;
	private static OutputStream NULL = new NullOutputStream();

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

		System.out.println("==== Benchmarking " + ITERS + " iterations ====\n");

		System.out.println("[JSON]");
		json_gson();
		json_simplegen();

		System.out.println("\n[XML]");
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
		System.out.print("\t Gson, Formatted... ");
		long start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			gson_fmt.toJson(library);

		start = System.currentTimeMillis() - start;
		double secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");

		System.out.print("\t Gson, Compact... ");
		start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			gson_nofmt.toJson(library);

		start = System.currentTimeMillis() - start;
		secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");

	}

	private static void json_simplegen() {
		System.out.print("\t Simple Gen, Formatted... ");
		json_simplegen.setIndenter(JSONIndenter.INSTANCE);
		long start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			json_simplegen.generate(library);

		start = System.currentTimeMillis() - start;
		double secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");

		System.out.print("\t Simple Gen, Compact... ");
		json_simplegen.setIndenter(CompactIndenter.INSTANCE);
		start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			json_simplegen.generate(library);

		start = System.currentTimeMillis() - start;
		secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");
	}

	private static void xml_jaxb() {
		System.out.print("\t JAXB, Formatted... ");
		long start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++) {
			try {
				jaxb_fmt.marshal(library, NULL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		start = System.currentTimeMillis() - start;
		double secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");

		System.out.print("\t JAXB, Compact... ");
		start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			try {
				jaxb_nofmt.marshal(library, NULL);
			} catch (Exception e) {
				e.printStackTrace();
			}

		start = System.currentTimeMillis() - start;
		secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");
	}

	private static void xml_simplegen() {
		System.out.print("\t Simple Gen, Formatted... ");
		xml_simplegen.setIndenter(XMLIndenter.INSTANCE);
		long start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			xml_simplegen.generate(library);

		start = System.currentTimeMillis() - start;
		double secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");

		System.out.print("\t Simple Gen, Compact... ");
		xml_simplegen.setIndenter(CompactIndenter.INSTANCE);
		start = System.currentTimeMillis();

		for (int i = 0; i < ITERS; i++)
			xml_simplegen.generate(library);

		start = System.currentTimeMillis() - start;
		secs = (double) start / 1000;
		System.out.println(start + " ms (" + secs + " secs - "
				+ (int) (ITERS / secs) + " ops/sec)");
	}
}