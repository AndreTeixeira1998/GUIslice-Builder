/**
 *
 * The MIT License
 *
 * Copyright 2018-2020 Paul Conti
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
package builder.commands;

import java.util.ArrayList;
import java.util.List;

import builder.common.EnumFactory;
import builder.mementos.GroupMemento;
//import builder.models.CheckBoxModel;
import builder.models.ImgButtonModel;
import builder.models.RadioButtonModel;
import builder.models.ToggleButtonModel;
import builder.models.WidgetModel;
import builder.views.PagePane;
import builder.widgets.Widget;

/**
 * The Class GroupCommand will
 * set the Group Enum for radio and check box controls.
 * 
 * @author Paul Conti
 * 
 */
public class GroupCommand extends Command {
  
  /** The page that holds the selected widgets. */
  private PagePane page;
  
  /** The group list contains the models of all the selected widgets that will be grouped. */
  private List<WidgetModel> groupList = new ArrayList<WidgetModel>();

  /** The groupID assigned */
  String groupID;
  
  /**
   * Instantiates a new group command.
   *
   * @param page
   *          the <code>page</code> is the object that holds the widgets
   */
  public GroupCommand(PagePane page) {
    this.page = page;
  }
  
  /**
   * Group will setup the group command for later execution
   * and creates the required Memento object for undo/redo.
   *
   * @return <code>true</code>, if successful
   */
  public boolean group() {
    for (Widget w : page.getSelectedList()) {
/*
 * reserve for future use
      if (w.getType().equals(EnumFactory.RADIOBUTTON)  ||
          w.getType().equals(EnumFactory.TOGGLEBUTTON) ||
          w.getType().equals(EnumFactory.CHECKBOX)) {
        groupList.add(w.getModel());
      }
*/
      WidgetModel m = w.getModel();
      if ((m.getType().equals(EnumFactory.RADIOBUTTON))  ||
          (m.getType().equals(EnumFactory.IMAGEBUTTON) && m.isToggle()) ||
          (m.getType().equals(EnumFactory.TOGGLEBUTTON))) {
        groupList.add(m);
      }
    }
    if (groupList.size() < 2) return false;
    memento = new GroupMemento(page, groupList);
    return true;  // success
  }

  /**
   * execute - perform the group operation.
   *
   * @see builder.commands.Command#execute()
   */
  @Override
  public void execute() {
    // We need to create a key first or ENUM will always be 0
    @SuppressWarnings("unused")
    String groupKey = EnumFactory.getInstance().createKey(EnumFactory.GROUPID);
    groupID = EnumFactory.getInstance().createEnum(EnumFactory.GROUPID);
    for(WidgetModel m : groupList) {
      if (m.getType().equals(EnumFactory.RADIOBUTTON)) {
        m.changeValueAt(groupID, RadioButtonModel.PROP_GROUP);
      } else if (m.getType().equals(EnumFactory.TOGGLEBUTTON)) {
       m.changeValueAt(groupID, ToggleButtonModel.PROP_GROUP); 
      } else {
        m.changeValueAt(groupID, ImgButtonModel.PROP_GROUP);
      }
    }
  }

  /**
   * toString - convert group command to a string for debugging.
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String myEnums = "";
    WidgetModel m = null;
    for(int i=0; i<groupList.size(); i++) {
      m = groupList.get(i);
      if (i > 0) myEnums = myEnums + ",";
      myEnums = myEnums + m.getEnum();
    }
    return String.format("Group Radio Buttons ID:%s widgets:%s",groupID,myEnums);
  }

}
