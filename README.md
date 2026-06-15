[![Maven Central](https://img.shields.io/maven-central/v/org.pageseeder/pso-psml.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.pageseeder%22%20AND%20a:%22pso-psml%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_psml&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pageseeder_psml)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_psml&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=pageseeder_psml)
[![javadoc](https://javadoc.io/badge2/org.pageseeder/pso-psml/javadoc.svg)](https://javadoc.io/doc/org.pageseeder/pso-psml)

# PSML

A Java 11 library for manipulating the [PageSeeder Markup Language](https://dev.pageseeder.com/api/psml.html) (PSML).
It provides a DOM-like model, format parsers (Markdown, HTML), template processing, document diffing, TOC generation,
and document splitting.

## Packages

| Package | Purpose | Main entry points |
|---------|---------|-------------------|
| `org.pageseeder.psml` | Top-level convenience class | `PSML.load(Reader)` |
| `org.pageseeder.psml.model` | DOM-like PSML model | `PSMLElement`, `PSMLNode`, `PSMLText`, `Loader` |
| `org.pageseeder.psml.md` | Markdown ↔ PSML | `MarkdownParser`, `MarkdownSerializer`, `Configuration` |
| `org.pageseeder.psml.html` | HTML → PSML | `HTMLParser` |
| `org.pageseeder.psml.spi` | Pluggable parser SPI | `Parser` |
| `org.pageseeder.psml.template` | Template processing | `Processor`, `TemplateFactory`, `Template` |
| `org.pageseeder.psml.process` | Document transformation pipeline | `Process` |
| `org.pageseeder.psml.process.config` | Pipeline configuration | `XRefsTransclude`, `Images`, `Strip`, `XSLTTransformation` |
| `org.pageseeder.psml.process.math` | Math rendering (TeX / AsciiMath) | `TexConverter`, `AsciiMathConverter` |
| `org.pageseeder.psml.diff` | Document diffing | `Diff`, `PSMLDiffer` |
| `org.pageseeder.psml.toc` | Table of contents / publication trees | `DocumentTree`, `PublicationTree`, `TreeExpander` |
| `org.pageseeder.psml.split` | Document splitting by fragment | `PSMLSplitter` |
| `org.pageseeder.psml.util` | Cross-cutting utilities | `XSLT`, `RelativePaths`, `DiagnosticCollector` |
| `org.pageseeder.psml.xml` | XML parsing helpers | `BasicHandler`, `Handler` |

## PSML model

The PSML model provides a DOM-like structure for building and serializing PSML programmatically.
`PSMLElement.Name` enumerates every element in the PSML schema.

```java
// Create nodes
PSMLElement heading = new PSMLElement(Name.HEADING);
heading.setAttribute("level", "1").setText("The title of this fragment");
PSMLElement para = new PSMLElement(Name.PARA);
para.setText("Some sample paragraph.");
PSMLElement fragment = new PSMLElement(Name.FRAGMENT);
fragment.setAttribute("id", 1).addNodes(heading, para);

// Serialisation to XML
XMLStringWriter xml = new XMLStringWriter(NamespaceAware.No);
fragment.toXML(xml);
```

This will result in the following PSML:

```xml
<fragment id="1">
  <heading level="1">The title of this fragment</heading>
  <para>Some sample paragraph.</para>
</fragment>
```

To load existing PSML from a reader:

```java
PSMLElement root = PSML.load(reader);
```

## Parsers

The library provides parsers for converting external formats into a PSML model, and a pluggable
SPI (`org.pageseeder.psml.spi.Parser`) for adding further formats.

**Markdown → PSML:**

```java
MarkdownParser parser = new MarkdownParser();
File markdown = new File("sample.md");
FileReader r = new FileReader(markdown);
PSMLElement psml = parser.parse(r);
```

**PSML → Markdown:**

```java
MarkdownSerializer serializer = new MarkdownSerializer();
serializer.serialize(psml, System.out);
```

**HTML → PSML:**

```java
HTMLParser parser = new HTMLParser();
PSMLElement psml = parser.parse(reader);
```

## PSML templates

PSML templates or template parts can easily be processed:

```java
Processor p = new Processor();
File template = new File("document-template.psml");
File psml = new File("document-instance.psml");

// Initial properties for template
Map<String, String> values = new HashMap<>();
values.put("title", "sample");
values.put("description", "A sample document for demo");

// process template with initial properties
p.process(template, psml, values);
```

To parse a template into memory:

```java
TemplateFactory factory = new TemplateFactory();
File file = new File("document-template.psml");
Template template = factory.parse(file);
```

## Document processing

`Process` applies a configurable transformation pipeline to a folder of PSML files: transclusion of
XRefs, image URI rewriting, math rendering (TeX/AsciiMath via Rhino), TOC numbering, and optional
XSLT post-processing.

```java
Process process = new Process();
process.setSrc(new File("src/psml"));
process.setDest(new File("out/psml"));
process.process();
```

## Document diffing

`Diff` compares two folders of PSML files and annotates the output with `<diff>` elements.
`PSMLDiffer` operates on individual documents.

## Table of contents

`DocumentTree` models the internal heading hierarchy of a single PSML document.
`PublicationTree` aggregates multiple `DocumentTree` instances into a deep TOC across a publication.
`TreeExpander` resolves XRef inclusions and detects loops.

## Document splitting

`PSMLSplitter` splits a PSML document into separate files by fragment using a builder API:

```java
PSMLSplitter splitter = new PSMLSplitter.Builder()
    .source(new File("document.psml"))
    .destination(new File("out/"))
    .build();
splitter.process();
```
