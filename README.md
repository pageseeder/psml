[![Maven Central](https://img.shields.io/maven-central/v/org.pageseeder/pso-psml.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22org.pageseeder%22%20AND%20a:%22pso-psml%22)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_psml&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pageseeder_psml)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=pageseeder_psml&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=pageseeder_psml)
[![javadoc](https://javadoc.io/badge2/org.pageseeder/pso-psml/javadoc.svg)](https://javadoc.io/doc/org.pageseeder/pso-psml)

# PSML

This library provides an API to manipulate the PageSeeder Markup Language 
(PSML).

**Warning! this API is still incubating and parts of the API are likely to 
change in the near future.** 


## PSML model API

The PSML model API provides a DOM-like structure to represent PSML data. It 
is primarily designed to simplify programming tasks rather than to act as a
true representation of a PSML document.

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

## Parsers

This library also provides a simple pluggable mechanism via a Service Provider
Interface (SPI) to parse various formats into a PSML structure.

Example with markdown:

```java
MarkdownParser parser = new MarkdownParser();
File markdown = new File("sample.md");
FileReader r = new FileReader(markdown);
PSMLElement psml = parser.parse(r);
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
