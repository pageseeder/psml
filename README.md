# PSML

This library provides an API to manipulate the PageSeeder Markup Language 
(PSML).

**Warning! this API is still incubating and parts of the API are likely to 
change in the near future.** 


## PSML model API

The PSML model API provides a DOM-like structure to represent PSML data. It 
is primarily designed to simplify programming tasks rather than to act as a
true representation of a PSML document.

```
  // Create nodes
  PSMLElement heading = new PSMLElement(Name.Heading);
  heading.setAttribute("level", "1").setText("The title of this fragment");
  PSMLElement fragment = new PSMLElement(Name.Fragment);
  fragment.setAttribute("id", 1).addNode(heading);

  // Serialisation to XML
  XMLStringWriter xml = new XMLStringWriter(false);
  fragment.toXML(xml);
```

This will result in the following PSML:

```xml
  <fragment id="1">
     <heading level="1">The title of this fragment</heading>
  </fragment>
``` 

## Parsers

This library also provides a simple pluggable mechanism via a Service Provider
Interface (SPI) to parse various formats into a PSML structure.

Example with markdown:

```java
  MarkdownParser parser = new MarkdownParser();
  File markdown = new File("sample.md");
  FileReader r = new FileReader(markdown)) {
  PSMLElement psml = parser.parse();
```

## PSML templates

PSML templates or template parts can easily be processed:

```java
  Processor p = new Processor();
  File template = new File("document-template.psml");
  File psml = new File("document-instance.psml");
  
  // Initial properties for template
  Map<String, String> values = new HashMap<String, String>();
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

