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

/**
 * Modelization of a resource.
 */
public class ResourceModel {
    /**
     * Resource name.
     */
    private final String m_name;
    /**
     * Resource type.
     */
    private final String m_type;
    /**
     * Resource owner.
     */
    private final String m_owner;

    /**
     * Creates a ResourceModel.
     * @param name
     * @param type
     * @param owner
     */
    public ResourceModel(String name, String type, String owner) {
        super();
        m_name = name;
        m_type = type;
        m_owner = owner;
    }

    public String getName() {
        return m_name;
    }

    public String getType() {
        return m_type;
    }

    public String getOwner() {
        return m_owner;
    }

}