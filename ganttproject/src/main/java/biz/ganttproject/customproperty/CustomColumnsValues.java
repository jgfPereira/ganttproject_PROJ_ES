/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2005-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package biz.ganttproject.customproperty;

import biz.ganttproject.core.time.GanttCalendar;
import net.sourceforge.ganttproject.language.GanttLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.Map.Entry;

/**
 * Keeps a map of custom property ID to value.
 *
 * @author bbaranne Mar 2, 2005 -- initial code
 * @auuthor dbarashev (Dmitry Barashev) -- complete rewrite
 */
public class CustomColumnsValues implements CustomPropertyHolder, Cloneable {
  /**
   * CustomColumnName(String) -> Value (Object)
   */
  private final Map<String, Object> mapCustomColumnValue = new HashMap<String, Object>();
  private final CustomPropertyManager myManager;

  /**
   * Creates an instance of CustomColumnsValues.
   */
  public CustomColumnsValues(CustomPropertyManager customPropertyManager) {
    myManager = customPropertyManager;
  }

  public void setValue(CustomPropertyDefinition def, Object value) throws CustomColumnsException {
    if (value == null) {
      mapCustomColumnValue.remove(def.getId());
      return;
    }
    Class<?> c1 = def.getType();
    Class<?> c2 = value.getClass();
    if (!c1.isAssignableFrom(c2)) {
      throw new CustomColumnsException(CustomColumnsException.CLASS_MISMATCH, "Failed to set value=" + value
          + ". value class=" + c2 + ", column class=" + c1);
    }
    mapCustomColumnValue.put(def.getId(), value);
  }

  public Object getValue(CustomPropertyDefinition def) {
    Object result = mapCustomColumnValue.get(def.getId());
    return (result == null) ? def.getDefaultValue() : result;
  }

  public boolean hasOwnValue(CustomPropertyDefinition def) {
    return mapCustomColumnValue.containsKey(def.getId());
  }

  public void removeCustomColumn(CustomPropertyDefinition definition) {
    mapCustomColumnValue.remove(definition.getId());
  }

  public CustomColumnsValues copyOf() {
    CustomColumnsValues res = new CustomColumnsValues(myManager);
    res.mapCustomColumnValue.putAll(this.mapCustomColumnValue);
    return res;
  }

  public void importFrom(@NotNull CustomPropertyHolder value) throws CustomColumnsException {
    mapCustomColumnValue.clear();
    for (var prop: value.getCustomProperties()) {
      setValue(prop.getDefinition(), prop.getValue());
    }
  }

  @Override
  public String toString() {
    return mapCustomColumnValue.toString();
  }

  @Override
  public List<CustomProperty> getCustomProperties() {
    List<CustomProperty> result = new ArrayList<CustomProperty>(mapCustomColumnValue.size());
    for (Entry<String, Object> entry : mapCustomColumnValue.entrySet()) {
      String id = entry.getKey();
      Object value = entry.getValue();
      CustomPropertyDefinition def = getCustomPropertyDefinition(myManager, id);
      if (def != null) {
        result.add(new CustomPropertyImpl(def, value));
      }
    }
    return result;
  }

  private static CustomPropertyDefinition getCustomPropertyDefinition(CustomPropertyManager manager, String id) {
    return manager.getCustomPropertyDefinition(id);
  }

  @Override
  public CustomProperty addCustomProperty(CustomPropertyDefinition definition, String valueAsString) {
    CustomPropertyDefinition defStub = PropertyTypeEncoder.INSTANCE.decodeTypeAndDefaultValue(
        definition.getTypeAsString(), valueAsString);
    try {
      setValue(definition, defStub.getDefaultValue());
    } catch (CustomColumnsException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return new CustomPropertyImpl(definition, defStub.getDefaultValue());
  }

  private static class CustomPropertyImpl implements CustomProperty {
    private CustomPropertyDefinition myDefinition;
    private Object myValue;

    public CustomPropertyImpl(CustomPropertyDefinition definition, Object value) {
      myDefinition = definition;
      myValue = value;
    }

    @Override
    public CustomPropertyDefinition getDefinition() {
      return myDefinition;
    }

    @Override
    public Object getValue() {
      return myValue;
    }

    @Override
    public String getValueAsString() {
      return CustomColumnsValues.getValueAsString(myValue);
    }
  }

  static String getValueAsString(Object value) {
    String result = null;
    if (value != null) {
      if (value instanceof GanttCalendar) {
        result = GanttLanguage.getInstance().formatShortDate((GanttCalendar) value);
      } else {
        result = String.valueOf(value);
      }
    }
    return result;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object) return true;
    if (!(object instanceof CustomColumnsValues)) return false;
    CustomColumnsValues columnsValues = (CustomColumnsValues) object;
    return mapCustomColumnValue.equals(columnsValues.mapCustomColumnValue) && myManager.equals(columnsValues.myManager);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mapCustomColumnValue, myManager);
  }
}
