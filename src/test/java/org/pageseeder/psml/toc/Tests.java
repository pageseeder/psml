package org.pageseeder.psml.toc;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.pageseeder.xmlwriter.XML.NamespaceAware;
import org.pageseeder.xmlwriter.XMLStringWriter;
import org.pageseeder.xmlwriter.XMLWritable;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.Input;
import org.xmlunit.validation.Languages;
import org.xmlunit.validation.ValidationProblem;
import org.xmlunit.validation.ValidationResult;
import org.xmlunit.validation.Validator;

import javax.xml.parsers.*;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Tests {


  public static List<Part<?>> treeify(@NonNull Element... elements) {
    TreeExpander expander = new TreeExpander();
    for (Element e : elements) {
      expander.add(e);
    }
    return expander.parts();
  }

  public static <T extends Element> Part<T> part(T element, @NonNull Part<?>... parts) {
    return new Part<>(element, parts);
  }

  public static Part<Toc> toc() {
    return new Part<>(new Toc());
  }

  public static Part<Reference> ref(int level, String title, long uri) {
    return new Part<>(new Reference(level, title, "", "", uri));
  }

  public static Part<Reference> ref(int level, String title, String fragment, long uri) {
    return new Part<>(new Reference(level, title, fragment, fragment, uri));
  }

  public static Part<Reference> ref(int level, String title, long uri, @NonNull Part<?>... parts) {
    return new Part<>(new Reference(level, title, "", "", uri), parts);
  }

  public static Part<Reference> ref(int level, String title, String fragment, long uri, String documenttype, String targetfrag, @NonNull Part<?>... parts) {
    return new Part<>(new Reference(level, title, fragment, fragment, uri, Reference.Type.EMBED, documenttype, targetfrag, Boolean.FALSE), parts);
  }

  public static Part<Reference> ref(int level, String title, String fragment, long uri, Reference.Type type, String documenttype, String targetfrag, @NonNull Part<?>... parts) {
    return new Part<>(new Reference(level, title, fragment, fragment, uri, type, documenttype, targetfrag, Boolean.FALSE), parts);
  }

  public static Part<Heading> h1(String title, String fragment, int index) {
    return heading(1, title, fragment, index);
  }

  public static Part<Heading> h1(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(1, title, fragment, index, parts);
  }

  public static Part<Heading> h2(String title, String fragment, int index) {
    return heading(2, title, fragment, index);
  }

  public static Part<Heading> h2(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(2, title, fragment, index, parts);
  }

  public static Part<Heading> h3(String title, String fragment, int index) {
    return heading(3, title, fragment, index);
  }

  public static Part<Heading> h3(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(3, title, fragment, index, parts);
  }

  public static Part<Heading> h4(String title, String fragment, int index) {
    return heading(4, title, fragment, index);
  }

  public static Part<Heading> h4(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(4, title, fragment, index, parts);
  }

  public static Part<Heading> h5(String title, String fragment, int index) {
    return heading(5, title, fragment, index);
  }

  public static Part<Heading> h5(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(5, title, fragment, index, parts);
  }

  public static Part<Heading> h6(String title, String fragment, int index) {
    return heading(6, title, fragment, index);
  }

  public static Part<Heading> h6(String title, String fragment, int index, @NonNull Part<?>... parts) {
    return heading(6, title, fragment, index, parts);
  }

  public static Part<Heading> heading(int level, String title, String fragment, int index) {
    return new Part<>(new Heading(level, title, fragment, fragment, index));
  }

  public static Part<Heading> heading(int level, String title, String fragment, int index, @NonNull Part<?>... parts) {
    return new Part<>(new Heading(level, title, fragment, fragment, index), parts);
  }

  public static Part<Heading> heading(int level, String title, String fragment, int index, String blocklabel, @NonNull Part<?>... parts) {
    return new Part<>(new Heading(level, title, fragment, fragment, index).blocklabel(blocklabel), parts);
  }

  public static Part<Phantom> phantom(int level, @NonNull Part<?>... parts) {
    return new Part<>(new Phantom(level, Phantom.NO_FRAGMENT, Phantom.NO_FRAGMENT), parts);
  }

  public static Part<Phantom> phantom(int level, String fragment, @NonNull Part<?>... parts) {
    return new Part<>(new Phantom(level, fragment, fragment), parts);
  }

  public static Part<TransclusionEnd> tend(String fragment) {
    return new Part<>(new TransclusionEnd(fragment, fragment));
  }

  public static Part<Heading> h1(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(1, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h1(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(1, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> h1(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(1, title, fragment, origfrag, index, numbered, prefix, parts);
  }

  public static Part<Heading> h1(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, String blocklabel, @NonNull Part<?>... parts) {
    return heading(1, title, fragment, origfrag, index, numbered, prefix, blocklabel, parts);
  }

  public static Part<Heading> h2(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(2, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h2(String title, String fragment, String origfrag, int index, boolean numbered, String prefix) {
    return heading(2, title, fragment, origfrag, index, numbered, prefix);
  }

  public static Part<Heading> h2(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(2, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> h2(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(2, title, fragment, origfrag, index, numbered, prefix, parts);
  }

  public static Part<Heading> h2(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, String blocklabel, @NonNull Part<?>... parts) {
    return heading(2, title, fragment, origfrag, index, numbered, prefix, blocklabel, parts);
  }

  public static Part<Heading> h3(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(3, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h3(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(3, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> h3(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(3, title, fragment, origfrag, index, numbered, prefix, parts);
  }

  public static Part<Heading> h3(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, String blocklabel, @NonNull Part<?>... parts) {
    return heading(3, title, fragment, origfrag, index, numbered, prefix, blocklabel, parts);
  }

  public static Part<Heading> h4(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(4, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h4(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(4, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> h4(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(4, title, fragment, origfrag, index, numbered, prefix, parts);
  }

  public static Part<Heading> h5(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(5, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h5(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(5, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> h5(String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(5, title, fragment, origfrag, index, numbered, prefix, parts);
  }

  public static Part<Heading> h6(String title, String fragment, int index, boolean numbered, String prefix) {
    return heading(6, title, fragment, index, numbered, prefix);
  }

  public static Part<Heading> h6(String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return heading(6, title, fragment, index, numbered, prefix, parts);
  }

  public static Part<Heading> heading(int level, String title, String fragment, int index, boolean numbered, String prefix) {
    return new Part<>(new Heading(level, title, fragment, fragment, index).numbered(numbered).prefix(prefix));
  }

  public static Part<Heading> heading(int level, String title, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return new Part<>(new Heading(level, title, fragment, fragment, index).numbered(numbered).prefix(prefix), parts);
  }

  public static Part<Heading> heading(int level, String title, String fragment, String origfrag, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return new Part<>(new Heading(level, title, fragment, origfrag, index).numbered(numbered).prefix(prefix), parts);
  }

  public static Part<Heading> heading(int level, String title, String fragment, String origfrag, int index, boolean numbered, String prefix, String blocklabel, @NonNull Part<?>... parts) {
    return new Part<>(new Heading(level, title, fragment, origfrag, index).numbered(numbered).prefix(prefix).blocklabel(blocklabel), parts);
  }

  public static Part<Paragraph> p(int level, String fragment, int index, boolean numbered, String prefix) {
    return new Part<>(new Paragraph(level, fragment, fragment, index).numbered(numbered).prefix(prefix));
  }

  public static Part<Paragraph> p(int level, String fragment, int index, boolean numbered, String prefix, String blocklabel) {
    return new Part<>(new Paragraph(level, fragment, fragment, index).numbered(numbered).prefix(prefix).blocklabel(blocklabel));
  }

  public static Part<Paragraph> p(int level, String title, String fragment, int index, boolean numbered, String prefix, String blocklabel) {
    return new Part<>(new Paragraph(level, fragment, fragment, index).title(title).numbered(numbered).prefix(prefix).blocklabel(blocklabel));
  }

  public static Part<Paragraph> p(int level, String fragment, int index, boolean numbered, String prefix, @NonNull Part<?>... parts) {
    return new Part<>(new Paragraph(level, fragment, fragment, index).numbered(numbered).prefix(prefix), parts);
  }

  public static void assertElementEquals(Element e, Element f) {
    assertEquals(e.getClass(), f.getClass());
    if (e instanceof Heading) {
      assertHeadingEquals((Heading) e, (Heading) f);
    } else if (e instanceof Reference) {
      assertEmbedEquals((Reference) e, (Reference) f);
    } else if (e instanceof Phantom) {
      assertEquals(e.level(), f.level());
      assertEquals(e.title(), f.title());
    }
  }

  public static void assertHeadingEquals(Heading e, Heading f) {
    assertEquals(e.level(), f.level(), "Heading levels don't match");
    assertEquals(e.title(), f.title(), "Heading titles don't match");
    assertEquals(e.fragment(), f.fragment(), "Heading fragments don't match");
    assertEquals(e.originalFragment(), f.originalFragment(), "Heading original fragments don't match");
    assertEquals(e.prefix(), f.prefix(), "Heading prefixes don't match");
    assertEquals(e.index(), f.index(), "Heading indexes don't match");
  }

  public static void assertEmbedEquals(Reference e, Reference f) {
    assertEquals(e.level(), f.level(), "Reference levels don't match");
    assertEquals(e.title(), f.title(), "Reference titles don't match");
    assertEquals(e.documenttype(), f.documenttype(), "Reference types don't match");
    assertEquals(e.uri(), f.uri(), "Reference URIs don't match");
    assertEquals(e.targetfragment(), f.targetfragment(), "Reference targetfragments don't match");
  }


  public static void assertPartEquals(Part<?> p, Part<?> q) {
    try {
      assertElementEquals(p.element(), q.element());
      assertEquals(p.getClass(), q.getClass());
      assertEquals(p.size(), q.size());
      for (int i = 0; i < p.size(); i++) {
        assertPartEquals(p.parts().get(i), q.parts().get(i));
      }
    } catch (AssertionError ex) {
      System.err.println("expected:");
      p.print(System.err);
      System.err.println("got:");
      q.print(System.err);
      throw ex;
    }
  }

  public static void assertDocumentTreeEquals(DocumentTree p, DocumentTree q) {
    try {
      assertEquals(p.getClass(), q.getClass());
      assertEquals(p.title(), q.title(), "Document titles don't match");
      assertEquals(p.level(), q.level(), "Document levels don't match");
      assertEquals(p.prefix(), q.prefix(), "Document prefixes don't match");
      assertEquals(p.numbered(), q.numbered(), "Document numbereds don't match");
      assertEquals(p.labels(), q.labels(), "Document labels don't match");
      assertEquals(p.parts().size(), q.parts().size(), "Document parts size don't match");
      assertEquals(p.listForwardReferences(), q.listForwardReferences(), "Forward references don't match");
      assertEquals(p.listReverseReferences(), q.listReverseReferences(), "Reverse references don't match");
      for (int i = 0; i < p.parts().size(); i++) {
        assertPartEquals(p.parts().get(i), q.parts().get(i));
      }
    } catch (AssertionError ex) {
      System.err.println("expected:");
      p.print(System.err);
      System.err.println("got:");
      q.print(System.err);
      throw ex;
    }
  }


  public static DocumentTree parse(long id, String filename) throws SAXException {
    InputStream in = DocumentTreeHandlerTest.class.getResourceAsStream("/org/pageseeder/psml/toc/" + filename);
    return parse(id, in);
  }

  public static PublicationConfig parseConfig(String filename) throws IOException {
    InputStream in = DocumentTreeHandlerTest.class.getResourceAsStream("/org/pageseeder/psml/toc/" + filename);
    return PublicationConfig.loadPublicationConfig(in);
  }

  public static DocumentTree parse(long id, @Nullable InputStream in) throws SAXException {
    DocumentTree tree = null;
    try {
      DocumentTreeHandler handler = new DocumentTreeHandler(id);
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser parser = factory.newSAXParser();
      parser.parse(in, handler);
      tree = handler.get();
    } catch (ParserConfigurationException | IOException ex) {
      throw new SAXException(ex);
    }
    if (tree == null) throw new SAXException("Unable to generate tree instance from parse!");
    return tree;
  }


  /**
   * Generate the DOM Source instance from the response content.
   *
   * @param o The writable object
   * @return The corresponding DOM source
   */
  public static DOMSource toDOMSource(XMLWritable o) {
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    try {
      o.toXML(xml);
    } catch (IOException ex) {
      // Won't happen
    }
    xml.flush();
    return toDOMSource(new StringReader(xml.toString()));
  }

  /**
   * Generate the DOM Source instance from the specified reader
   *
   * @param reader The reader to parse as DOM
   * @return The corresponding DOM source
   */
  public static DOMSource toDOMSource(Reader reader) {
    return new DOMSource(toNode(reader));
  }

  /**
   * Generate the DOM Source instance from the specified reader
   *
   * @param reader The reader to parse as DOM
   * @return The corresponding DOM source
   */
  public static Node toNode(Reader reader) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new InputSource(reader));
    } catch (ParserConfigurationException | SAXException | IOException ex) {
      throw new IllegalStateException("Unable to generate DOM Source", ex);
    }
  }

  public static Source getSchema(String filename) {
    try {
      String pathToSchema = "/org/pageseeder/psml/toc/" + filename;
      URL url = Tests.class.getResource(pathToSchema);
      StreamSource schema = new StreamSource(url.openStream());
      schema.setSystemId(url.toURI().toString());
      return schema;
    } catch (URISyntaxException | IOException ex) {
      throw new IllegalStateException("Unable to open schema source", ex);
    }
  }

  public static Validates validates(String spec) {
    Source schema = getSchema(spec);
    return new Validates(schema);
  }

  public static void print(XMLWritable o) {
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    try {
      o.toXML(xml);
    } catch (IOException ex) {
      // Won't happen
    }
    xml.flush();
    System.out.println(xml.toString());
  }

  public static void print(PublicationTree o, long cid, int cposition, @Nullable FragmentNumbering number,
                           @Nullable PublicationConfig config, boolean externalrefs) {
    XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
    xml.setIndentChars("  ");
    try {
      o.toXML(xml, cid, cposition, number, config, externalrefs);
    } catch (IOException ex) {
      // Won't happen
    }
    xml.flush();
    System.out.println(xml.toString());
  }

  // Matches for assertThat
  // --------------------------------------------------------------------------

  public static class Validates extends BaseMatcher<Object> {
    private final Source _schema;

    private Iterable<ValidationProblem> problems = Collections.emptyList();

    public Validates(Source schema) {
      this._schema = schema;
    }

    @Override
    public boolean matches(Object object) {
      Validator v = Validator.forLanguage(Languages.W3C_XML_SCHEMA_NS_URI);
      v.setSchemaSource(this._schema);
      Source s = Input.from(object).build();
      ValidationResult result = v.validateInstance(s);
      this.problems = result.getProblems();
      return result.isValid();
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("validates schema=").appendText(this._schema.getSystemId());
    }

    @Override
    public void describeMismatch(Object item, Description description) {
      description.appendText("found the following validation problems:\n");
      for (ValidationProblem p : this.problems) {
        description.appendText(p.getType().toString());
        if (p.getLine() != -1) {
          description.appendText(":").appendText(Integer.toString(p.getLine()));
          if (p.getColumn() != -1) {
            description.appendText(":").appendText(Integer.toString(p.getColumn()));
          }
        }
        description.appendText(":").appendText(p.getMessage());
      }
    }

  }

}
