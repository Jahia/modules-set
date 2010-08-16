/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.latestContent.initializers;

import org.apache.log4j.Logger;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;

import javax.jcr.PropertyType;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: Apr 19, 2010
 * Time: 3:03:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChoiceListLatestContentInitializers implements ModuleChoiceListInitializer {
    private transient static Logger logger = Logger.getLogger(ChoiceListLatestContentInitializers.class);
    private String key;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale,
                                                     Map<String, Object> context) {
        final Set<ChoiceListValue> listValues = new HashSet<ChoiceListValue>();
        String displayName,value;
        ChoiceListValue myChoiceList;
        boolean editorialContent = true;
        NodeTypeIterator iterator = NodeTypeRegistry.getInstance().getAllNodeTypes();
        while(iterator.hasNext())
        {
            ExtendedNodeType node = (ExtendedNodeType) iterator.next();

            editorialContent = node.isNodeType("jmix:editorialContent");

            if(editorialContent)
            {
                displayName = value = node.getName();
                myChoiceList = new ChoiceListValue(displayName, new HashMap<String, Object>(), new ValueImpl(value, PropertyType.STRING, false));
                listValues.add(myChoiceList);
             }
        }

        return new ArrayList<ChoiceListValue>(listValues);
    }
}
