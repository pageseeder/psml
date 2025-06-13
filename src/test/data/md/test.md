
Title: A simple test
Description: This is a simple Markdown file to test the markdown parser
Author: Christophe Lauret

Simple Test
===========

Let's start with a **single** paragraph.

This paragraph includes a piece of `code`.

First chapter
-------------

Let's have a long paragraph that *continues* on the next line and see if see works
properly 

    this is
    a code
    block

This is a list:
 * First item
 * Second item
   (also part of the second item?)
 * Third item

### A heading level 3


Now we have a numbered list:
 2. First item
 3. Second item
   (also part of the second item?)
 4. Third item

> We're having a some quoted content now
> Quoted line #2
> Quoted line #3
>
> Quoted line #4
break
> Quoted line #5

```javascript
function() {

  // do something
  if (a
    > b) then a = b;
}
```

Second chapter
--------------

Until now, we've only been interested in whether or not a match is found at some location within a particular input string. We never cared about where in the string the match was taking place.
















Information on [PSML elements](https://dev.pageseeder.com/psml/elements.html).

# XML Writer

The XML Writer defines an API and implementations to write XML out to a reading, DOM or SAX.

## Example

```
  XMLWriter xml = new XMLWriterImpl(Reader r);
  xml.openElement("greetings");
  xml.attribute("lang", "en");
  xml.writeText("Hello World");
  xml.closeElement();
```

## Background

We've used this API for over 15 years and it used to be included in Diff-X.
We decided to split it off so that we would not have to include Diff-X in all our projects.

Escape ~ \` \! @ # $ % ^ & \* ( ) \_ - + = { \[ } \] | \\ : ; " ' \< , \> . ? / text

This is \*escaped star\*

This is \[escaped brackets\]

 \- Red
 \- Green
 \- Blue



