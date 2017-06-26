/*
 * Copyright 2017 Allette Systems (Australia)
 * http://www.allette.com.au
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.pageseeder.psml.process;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.pageseeder.psml.process.config.XRefsTransclude;

public class ProcessTest {

  public ProcessTest() {
  }

  @Test
  public void testProcessXRefs() throws IOException, ProcessException {
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File("src/test/data/process"));
    File dest = new File("temp/process/xrefs");
    if (dest.exists())
      dest.delete();
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes("ref_0.psml");
    p.setXrefs(xrefs);
    p.process();
  }

  @Test(expected=ProcessException.class)
  public void testProcessXRefsAmbiguous() throws IOException, ProcessException {
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File("src/test/data/process"));
    File dest = new File("temp/process/xrefs");
    if (dest.exists())
      dest.delete();
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes("ref_0a.psml");
    p.setXrefs(xrefs);
    p.process();
  }

  @Test(expected=ProcessException.class)
  public void testProcessXRefsAmbiguous2() throws IOException, ProcessException {
    Process p = new Process();
    p.setPreserveSrc(true);
    p.setSrc(new File("src/test/data/process"));
    File dest = new File("temp/process/xrefs");
    if (dest.exists())
      dest.delete();
    dest.mkdirs();
    p.setDest(dest);
    XRefsTransclude xrefs = new XRefsTransclude();
    xrefs.setTypes("embed,transclude");
    xrefs.setIncludes("ref_0a2.psml");
    p.setXrefs(xrefs);
    p.process();
  }

}
