# Diff output in Markdown

Since 1.8.6, `MarkdownSerializer` can serialize combined Diff-X PSML, as produced by `PSMLDiffer`, as Markdown with `<ins>` and `<del>` markers. The output is meant as evidence for describing changes, for example as input to an AI model. It keeps the changes the differ found and doesn't compute another diff.

## Usage

```java
MarkdownSerializer serializer = new MarkdownSerializer();
serializer.setOptions(MarkdownOutputOptions.defaultOptions().diff(DiffFormat.INS_DEL));
serializer.serialize(PSML.load(reader), out);
```

`DiffFormat` options:

| Value | Output |
|---|---|
| `IGNORE` (default) | No markers. Output is the same as without the option. Inserted and deleted text both appear unmarked. |
| `INS_DEL` | Literal HTML-style `<ins>` and `<del>` tags. |

The serialization root can be a `<diff>` element, a diffed fragment or block, or any normal PSML. A `<compare>` element is ignored in both modes, so the caller has to select the comparison subtree.

Input with no diff markup produces the same output in both modes. The only exceptions are literal `<ins`/`<del` text and `|` in table cells, which `INS_DEL` escapes.

## Input

Both Diff-X namespace families are recognised, whatever prefix the source uses:

| Role | Current (pso-diffx 1.3 default) | Legacy |
|---|---|---|
| Text and element operations | `https://www.pageseeder.org/diffx` | `http://www.topologi.com/2005/Diff-X` |
| Inserted attributes | `https://www.pageseeder.org/diffx/insert` | `http://www.topologi.com/2005/Diff-X/Insert` |
| Deleted attributes | `https://www.pageseeder.org/diffx/delete` | `http://www.topologi.com/2005/Diff-X/Delete` |

In the model, they use the prefixes `diff:`, `ins:` and `del:`:
- `<diff:ins>` and `<diff:del>` load as `Name.DIFF_INS` and `Name.DIFF_DEL`.
- `PSMLElement.getDiffOperation()` reads `diff:insert`/`diff:delete`.
- `getDeletedAttribute(name)` returns the old value of a changed or deleted attribute.
- `isInsertedAttribute(name)` says whether an attribute was inserted or changed.

An unqualified `<ins>` or an element from another namespace is never treated as diff markup.

`PSMLDiffer.diff(xml1, xml2, out)` reverses its arguments internally: pass the **current** version first and the **old** version second, as `DiffHandler` does. `<ins>` then marks the new version and `<del>` the old one.

## Examples

Namespace declarations are left out of the input examples.

**Inline change**

```xml
<para>Payment within<diff:del> 30</diff:del><diff:ins> 14</diff:ins> days.</para>
```
```markdown
Payment within<del> 30</del><ins> 14</ins> days.
```

**Inserted or deleted elements.** There is one marker per block. Markdown prefixes and block separators stay outside the markers, and inner markers for the same operation are not repeated.

```xml
<heading diff:insert="true" level="2" ins:level="true"><diff:ins>New heading</diff:ins></heading>
<para diff:delete="true"><diff:del>Old paragraph.</diff:del></para>
<list><item>One</item><item diff:insert="true"><diff:ins>Three</diff:ins></item></list>
```
```markdown
## <ins>New heading</ins>

<del>Old paragraph.</del>

- One
- <ins>Three</ins>
```

A deleted element only has its attributes in the delete namespace (`del:level="2"`). They are used for its visible structure, such as the heading level, prefix or table spans.

**Properties.** The deletion always comes first.

```xml
<property name="effective" title="Effective date" del:value="1 July 2026" value="1 August 2026" ins:value="true"/>
<property diff:delete="true" del:name="b" del:title="B" del:value="beta"/>
<property name="tags" title="Tags" multiple="true"><value>red</value><value><diff:del>green</diff:del><diff:ins>blue</diff:ins></value></property>
```
```markdown
| Effective date | <del>1 July 2026</del><ins>1 August 2026</ins> |
| <del>B</del> | <del>beta</del> |
| Tags | [red, <del>green</del><ins>blue</ins>] |
```

With `PropertiesFormat.VALUE_PAIRS`: `Effective date: <del>1 July 2026</del><ins>1 August 2026</ins>`. Enabling diff output doesn't include document metadata; that still depends on `metadata()`.

**Tables.** Markers go on each cell's text, never on delimiters or separator rows. Header/footer styling stays outside the markers, and literal `|` in a cell is escaped as `\|`. In the HTML table format, markers go inside `<td>`/`<th>` and `<caption>`.

```markdown
| H1           | H2           |
|--------------|--------------|
| a            | b            |
| <ins>c</ins> | <ins>d</ins> |
```

**Cross-references and links.** The syntax stays outside the markers: `**Doc<del> A</del><ins> B</ins>**` and `[<del>old</del><ins>new</ins>](https://example.org)`. Changes to `href`, image `src` and other attributes alone produce no markers. Diff output never turns on URLs or images that the options leave out. An inserted or deleted image is wrapped as a whole: `<ins>![A](a.png)</ins>`.

**Code.** Markers can't go inside code syntax, so changed code is written as the old version followed by the new one, each in its own code syntax:

```markdown
Call <del>`foo()`</del><ins>`bar()`</ins> now.
```

````markdown
<del>
```java
int x = 1;
```
</del>
<ins>
```java
int x = 2;
```
</ins>
````

Embedded Markdown property values work the same way: `<del>Some *old* text</del><ins>Some *new* text</ins>`.

**Superscript and subscript.** With `SuperSubFormat.UNICODE`, each piece is converted separately: `mc<del>²</del><ins>³</ins>`.

## Escaping and diagnostics

In `INS_DEL` mode, literal text that looks like a marker (`<ins`, `</ins`, `<del`, `</del`, in any case, followed by whitespace, `/`, `>` or the end of the text) is written as `&lt;ins`, and so on, so it can't pass for a marker. This applies to the whole output, including code, attribute values such as URLs, and text that only looks like a marker once its pieces are joined. Other markup is left alone, such as `<insert>` or `<div>`, as is code such as `a<del)`. In code, the escaped text appears literally as `&lt;del>`, and it can't be told apart from a source that already contained `&lt;del>`. The serializer uses the characters U+E000 to U+E003 internally; if they appear in the source, they are removed with a warning.

Unsupported or contradictory annotations are reported through the `DiagnosticCollector` passed to `serialize(element, out, collector)`:

| Case | Fallback |
|---|---|
| `diff:insert` and `diff:delete` both `true` | No operation is inherited from that element; only the text markers inside it apply |
| `diff:insert`/`diff:delete` not `true` or `false` | Ignored |
| Another element or attribute in the Diff-X namespace | Ignored; the content is serialized without markers of its own |
| Characters U+E000 to U+E003 in the source | Removed |

Inner text markers take priority over structural annotations. For example, `<xref diff:delete="true"><diff:ins>content.</diff:ins></xref>` produces `**<ins>content.</ins>**` without a warning.

## Limitations

- The output is a Markdown-based input format. Markdown renderers don't all treat Markdown inside HTML tags the same way.
- Adjacent markers for the same operation aren't merged, except within table cells, flattened inline text and code.
- A changed property title shows only the current title. Only value changes are marked.
