/**
 *
 * The MIT License
 *
 * Copyright 2018, 2019 Paul Conti
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package builder.codegen.pipes;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.StringBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import builder.codegen.CodeGenException;
import builder.codegen.CodeGenerator;
import builder.codegen.TemplateManager;
import builder.common.EnumFactory;
import builder.models.CheckBoxModel;
import builder.models.RadioButtonModel;
import builder.models.WidgetModel;

/**
 * The Class CheckboxCbPipe handles code generation
 * within the "Checkbox Callback" tag of our source code.
 * 
 * This section creates callbacks for XCheckbox API calls. 
 * 
 * @author Paul Conti
 * 
 */
public class CheckboxCbPipe extends WorkFlowPipe {

  /** The Constants for tags. */
  private final static String CHECKBOXCB_TAG           = "//<Checkbox Callback !Start!>";
  private final static String CHECKBOXCB_END_TAG       = "//<Checkbox Callback !End!>";
  private final static String CHECKBOX_ENUMS_TAG       = "//<Checkbox Enums !Start!>";
  private final static String CHECKBOX_ENUMS_END_TAG   = "//<Checkbox Enums !End!>";
  
  /** The Constants for templates. */
  private final static String CHECKBOX_CB_TEMPLATE     = "<CHECKBOX_CB>";
  private final static String CHECKBOX_CASE_TEMPLATE   = "<CHECKBOX_CB_CASE>";
  
  /** The Constants for macros. */
  private final static String CALLBACK_MACRO         = "CALLBACK";
  private final static String ENUM_MACRO             = "COM-002";
  
  /** The template manager. */
  TemplateManager tm = null;
  
  /** Our code generator. */
  private CodeGenerator cg = null;
  
  /**
   * Instantiates a new pipe.
   *
   * @param cg
   *          the cg
   */
  public CheckboxCbPipe(CodeGenerator cg) {
    this.cg = cg;
  }
  
  /**
   * process
   *
   * For our Checkbox Callback we have this modified process routine.
   * This version is to support removing the CHECKBOXCB_TAG and CHECKBOXCB_END_TAG
   * once we write out any checkbox callbacks.
   *   
   * If this occurs we then place inside the newly generated callback the 
   * CHECKBOX_ENUMS_TAG and CHECKBOX_ENUMS_END_TAG between the "switch()" and "default"
   * statements so next time we only handle the individual enum CASE Statements.
   * This means we can detect existing enums and thus prevent us from
   * modifying them.  
   * This allows users to edit the callback with additional code
   * safely from us deleting it on a round trip editing session.
   *
   * NOTE: Notice that we also do not output our end tag by calling
   *       CodeUtils.readPassString(). 
   *
   * @see builder.codegen.pipes.Pipe#process(java.lang.Object, java.lang.Object)
   */
  @Override
  public StringBuilder process(StringBuilder input) throws CodeGenException {
    MY_TAG = CHECKBOXCB_TAG;
    MY_END_TAG = CHECKBOXCB_END_TAG;
    MY_ENUM_TAG = CHECKBOX_ENUMS_TAG;
    MY_ENUM_END_TAG = CHECKBOX_ENUMS_END_TAG;
    
    return super.processCB(input);
        
  }

  /**
   * doCbCommon
   *  Builds up a list of models that require a callback then calls 
   *  outputButtonCB with this list for the actual code generation.
   *
   * @param br
   *          the buffered reader of our project template input
   * @param sBd
   *          the StringBuilder object containing our project template
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  @Override
  public void doCbCommon(BufferedReader br, StringBuilder sBd) throws IOException {
    
    // build up a list of widget models that have button callbacks enabled
    callbackList = new ArrayList<WidgetModel>();
    for (WidgetModel m : cg.getModels()) {
      if (m.getType().equals(EnumFactory.CHECKBOX) && ((CheckBoxModel) m).isCallbackEn()) {
        callbackList.add(m);
      } else if (m.getType().equals(EnumFactory.RADIOBUTTON) && ((RadioButtonModel) m).isCallbackEn()) {
        callbackList.add(m);
      }
    }
    super.doCbCommon(br, sBd);
  }
  
   /**
    * outputButtonCB.
    *  This routine outputs the  Common checkbox callback
    *  bool CbCheckbox() and the first set of button callbacks.
    *
    * @param sBd
   *          the StringBuilder object containing our project template
    * @param callbackList
    *          the model list to process for callback buttons
    */
  @Override
  public void outputCB(StringBuilder sBd) {
    tm = cg.getTemplateManager();
    // create a temporary string buffer to hold the case statements
    // we will add them all at once before we leave this routine
    StringBuilder sTemp = new StringBuilder();
    // create our callback section - start by opening our templates
    tm = cg.getTemplateManager();
    List<String> templateStandard = tm.loadTemplate(CHECKBOX_CASE_TEMPLATE);
    List<String> outputLines;
    Map<String, String> map = new HashMap<String, String>();
    for (WidgetModel m : callbackList) {
      map.put(ENUM_MACRO, m.getEnum());
      outputLines = tm.expandMacros(templateStandard, map);
      tm.codeWriter(sTemp, outputLines);
    }

    // now we place all of our new case statements inside our callback template
    map.clear();
    String sButtons = sTemp.toString();
    map.put(CALLBACK_MACRO, sButtons);
    List<String> templateLines = tm.loadTemplate(CHECKBOX_CB_TEMPLATE);
    outputLines = tm.expandMacros(templateLines, map);
    tm.codeWriter(sBd, outputLines);

  }

  /**
   * doEnums.
   *  The callback already exists so we just process what is  
   *  between the CHECKBOX_ENUMS_TAG and CHECKBOX_ENUMS_END_TAG here.
   *
   * @param br
   *          the buffered reader of our project template input
   * @param sBd
   *          the StringBuilder object containing our project template output
   */
  @Override
  public void doEnums(BufferedReader br, StringBuilder sBd) throws IOException {
    tm = cg.getTemplateManager();

    // build up a list of widget models that have button callbacks enabled
    // also, save the enums into our enumMap for easier checking for existence.
    callbackList = new ArrayList<WidgetModel>();
    List<String> enumList = new ArrayList<String>();
    for (WidgetModel m : cg.getModels()) {
      if (m.getType().equals(EnumFactory.CHECKBOX) && ((CheckBoxModel) m).isCallbackEn()) {
        callbackList.add(m);
        enumList.add(m.getEnum());
      } else if (m.getType().equals(EnumFactory.RADIOBUTTON) && ((RadioButtonModel) m).isCallbackEn()) {
        callbackList.add(m);
        enumList.add(m.getEnum());
      }
    }
    if (callbackList.size() == 0)
      return;
    
    // this removes duplicates and detects and removes deleted ui elements
    Map<String, String> enumMap = super.mapEnums(br, sBd, enumList);
    
    // now deal with our new enums    
    List<String> templateStandard = tm.loadTemplate(CHECKBOX_CASE_TEMPLATE);
    List<String> outputLines;
    Map<String, String> map = new HashMap<String,String>();
    for (WidgetModel m : callbackList) {
      /* search our enumMap for this enum. 
       * if found to have been dealt with already; skip it (value == "1"),      
       * otherwise expand the macros
       */
      if (enumMap.get(m.getEnum()).equals("0")) {
        map.clear();
        map.put(ENUM_MACRO, m.getEnum());
        outputLines = tm.expandMacros(templateStandard, map);
        tm.codeWriter(sBd, outputLines);
      }
    }
  }    

}
  