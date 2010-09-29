/*
 * Copyright 2010 akquinet
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.akquinet.android.rindirect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Modelization of R.
 */
public class RModel {

    /**
     * The qualified class name.
     */
    private final String m_clazz;

    /**
     * The package name.
     */
    private final String m_package;

    /**
     * The R structure.
     * Each entry is a R subclass (array, id ...)
     * Each values is the set of resources contained in the subclass.
     */
    private Map<String, List<ResourceModel>> m_structure = new HashMap<String, List<ResourceModel>>();


    /**
     * Creates a RModel.
     * @param pack the package.
     */
    public RModel(String pack) {
        RIndirect.LOGGER.fine("Creating R modelization " + pack);
        m_package = pack;
        m_clazz = m_package + ".R";
    }

    /**
     * Gets the package
     * @return the package
     */
    public String getPackage() {
        return m_package;
    }

    /**
     * Gets the qualified class name
     * @return the class name
     */
    public String getRClass() {
        return m_clazz;
    }

    /**
     * Adds a resource to the 'owner' subclass.
     * @param owner the subclass name
     * @param name the resource name
     * @param type the type (int or int[])
     */
    public void addResource(String owner, String name, String type) {
        RIndirect.LOGGER.fine("Adding resource to model : " + name);
        List<ResourceModel> resources = m_structure.get(owner);
        if (resources == null) {
            RIndirect.LOGGER.fine("New resource type : " + type);
            resources = new ArrayList<ResourceModel>();
            m_structure.put(owner, resources);
        }
        RIndirect.LOGGER.info("Adding resource " + name + " of type " + type + " to " + owner);
        resources.add(new ResourceModel(name, type, owner));
    }

    /**
     * Gets the structure.
     * @return the structure
     */
    public Map<String, List<ResourceModel>> getResources() {
        return m_structure;
    }

}
