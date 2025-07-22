package org.pageseeder.psml.diff;

import org.junit.jupiter.api.Test;
import org.pageseeder.diffx.action.Operation;
import org.pageseeder.diffx.action.OperationsBuffer;
import org.pageseeder.diffx.api.Operator;
import org.pageseeder.diffx.token.XMLToken;
import org.pageseeder.diffx.token.impl.CharactersToken;
import org.pageseeder.diffx.token.impl.XMLAttribute;
import org.pageseeder.diffx.token.impl.XMLEndElement;
import org.pageseeder.diffx.token.impl.XMLStartElement;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ElementDenormalizerTest {

  @Test
  void handle_no_change() {
    assertNoChange("<para>","text","</para>");
    assertNoChange("<row>","<cell>","text","</cell>","</row>");
    assertNoChange("<row>","<cell>","-@hcell=true", "text","</cell>","</row>");
    assertNoChange("<row>","<cell>","+@nlist=true", "text","</cell>","</row>");
    assertNoChange("<row>","<cell>","@nlist=true", "text","</cell>","</row>");
    assertNoChange("<row>","-@hcell=true","<cell>", "text","</cell>","</row>");
    assertNoChange("<list>","-@hcell=true","<item>", "text","</item>","</list>");
    assertNoChange("<list>","@hcell=true","<item>", "text","</item>","</list>");
    assertNoChange("<list>","+@hcell=true","<item>", "text","</item>","</list>");
    assertNoChange("<list>","-@nlist=true","<item>", "text","</item>","</list>");
    assertNoChange("<list>","<item>","+@nlist=true","text","</item>","</list>");
  }

  @Test
  void handle_change_cell1() {
    List<Operation<XMLToken>> in = toOperations("<row>","<cell>","+@hcell=true", "text","</cell>","</row>");
    List<Operation<XMLToken>> exp = toOperations("<row>","<hcell>","+@hcell=true", "text","</hcell>","</row>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_cell2() {
    List<Operation<XMLToken>> in = toOperations("<row>","<cell>","@hcell=true", "text","</cell>","</row>");
    List<Operation<XMLToken>> exp = toOperations("<row>","<hcell>","@hcell=true", "text","</hcell>","</row>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_cell3() {
    List<Operation<XMLToken>> in = toOperations("<row>", "<cell>", "@colspan=2","@hcell=true","text","</cell>", "</row>");
    List<Operation<XMLToken>> exp = toOperations("<row>","<hcell>","@colspan=2","@hcell=true","text","</hcell>","</row>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_list1() {
    List<Operation<XMLToken>> in = toOperations("<list>","+@nlist=true","<item>", "text","</item>","</list>");
    List<Operation<XMLToken>> exp = toOperations("<nlist>","+@nlist=true","<item>", "text","</item>","</nlist>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_list2() {
    List<Operation<XMLToken>> in = toOperations("<list>","@nlist=true","<item>", "text","</item>","</list>");
    List<Operation<XMLToken>> exp = toOperations("<nlist>","@nlist=true","<item>", "text","</item>","</nlist>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_list3() {
    List<Operation<XMLToken>> in = toOperations("<list>","@type=alpha","+@nlist=true","<item>", "text","</item>","</list>");
    List<Operation<XMLToken>> exp = toOperations("<nlist>","@type=alpha","+@nlist=true","<item>", "text","</item>","</nlist>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_list4() {
    List<Operation<XMLToken>> in = toOperations("+<list>","+@nlist=true","<item>", "text","</item>","+</list>");
    List<Operation<XMLToken>> exp = toOperations("+<nlist>","+@nlist=true","<item>", "text","</item>","+</nlist>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_list5() {
    List<Operation<XMLToken>> in = toOperations("<fragment>","<list>","@nlist=true","<item>", "+<list>","+@nlist=true","<item>", "text","</item>","+</list>", "</item>","</list>","</fragment>");
    List<Operation<XMLToken>> exp = toOperations("<fragment>","<nlist>","@nlist=true","<item>", "+<nlist>","+@nlist=true","<item>", "text","</item>","+</nlist>", "</item>","</nlist>", "</fragment>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  @Test
  void handle_change_mix1() {
    List<Operation<XMLToken>> in = toOperations("<table>","<row>","<cell>","@hcell=true","<item>", "+<list>","+@nlist=true","<item>", "text","</item>","+</list>", "</item>","</cell>","</row>","</table>");
    List<Operation<XMLToken>> exp = toOperations("<table>","<row>","<hcell>","@hcell=true","<item>", "+<nlist>","+@nlist=true","<item>", "text","</item>","+</nlist>", "</item>","</hcell>","</row>","</table>");
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(exp, got);
  }

  private void assertNoChange(String... operations) {
    List<Operation<XMLToken>> in = toOperations(operations);
    List<Operation<XMLToken>> got = denormalize(in);
    assertEquals(in, got);
  }

  private static List<Operation<XMLToken>> denormalize(List<Operation<XMLToken>> operations) {
    OperationsBuffer<XMLToken> out = new OperationsBuffer<>();
    ElementDenormalizer denormalizer = new ElementDenormalizer(out);
    for (Operation<XMLToken> operation : operations) {
      denormalizer.handle(operation.operator(), operation.token());
    }
    return out.getOperations();
  }

  private static List<Operation<XMLToken>> toOperations(String... operations) {
    List<Operation<XMLToken>> result = new ArrayList<>(operations.length);
    for (String operation : operations) {
      Operator operator = operation.startsWith("-") ? Operator.DEL : operation.startsWith("+") ? Operator.INS : Operator.MATCH;
      XMLToken token;
      String tokenString = operation.replaceAll("[-+]", "");
      if (tokenString.matches("</[a-z]+>")) {
        token = new XMLEndElement(tokenString.replaceAll("[^a-z]", ""));
      } else if (tokenString.matches("<[a-z]+>")) {
        token = new XMLStartElement(tokenString.replaceAll("[^a-z]", ""));
      } else if (tokenString.matches("@[a-z]+=.*")) {
        token = new XMLAttribute(tokenString.substring(1, tokenString.indexOf('=')), tokenString.substring(tokenString.indexOf('=')+1));
      } else {
        token = new CharactersToken(tokenString);
      }
      result.add(new Operation<>(operator, token));
    }
    return result;
  }

}