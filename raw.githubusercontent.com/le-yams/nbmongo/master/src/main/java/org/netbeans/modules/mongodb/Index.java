/* 
 * Copyright (C) 2015 Thomas Werner
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.netbeans.modules.mongodb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.Document;

/**
 * POJO that contains the data of an index.
 *
 * @author thomaswerner35
 */
@EqualsAndHashCode @NoArgsConstructor @ToString
public class Index {

    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_KEYS = "keys";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_NAMESPACE = "nameSpace";
    public static final String PROPERTY_SPARSE = "sparse";
    public static final String PROPERTY_UNIQUE = "unique";
    public static final String PROPERTY_DROPDUPLICATES = "dropDuplicates";

    @AllArgsConstructor @EqualsAndHashCode @ToString
    public class Key {

        public static final String PROPERTY_ORDER = "orderAscending";

        private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

        @Getter private final String column;
        @Getter private boolean orderedAscending;

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propSupport.removePropertyChangeListener(listener);
        }

        public void setOrderAscending(boolean oAscending) {
            final boolean old = this.orderedAscending;
            this.orderedAscending = oAscending;
            propSupport.firePropertyChange(PROPERTY_ORDER, old, orderedAscending);
        }

    }

    @Getter private String name;
    @Getter private String nameSpace;
    private final List<Key> keys = new ArrayList<>();
    @Getter private boolean sparse = true;
    @Getter private boolean unique = false;
    @Getter private boolean dropDuplicates = false;

    private final PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private final PropertyChangeListener keyChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            propSupport.firePropertyChange(PROPERTY_KEY, false, true);
        }
    };

    public Index(Document indexInfo) {
        name = indexInfo.getString("name");
        nameSpace = indexInfo.getString("ns");
        final Document keyObj = indexInfo.get("key", Document.class);
        for(String keyObjProperty: keyObj.keySet()) {
            keys.add(new Key(keyObjProperty, Integer.valueOf(1).equals(keyObj.get(keyObjProperty))));
        }
        sparse = Boolean.TRUE.equals(indexInfo.get("sparse"));
        unique = Boolean.TRUE.equals(indexInfo.get("unique"));
        dropDuplicates = Boolean.TRUE.equals(indexInfo.get("dropDups"));
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties. The same listener object may be added more than once, and will be
     * called as many times as it is added. If <code>listener</code> is null, no exception is thrown and no action is
     * taken.
     *
     * @param listener  The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered for all properties. If <code>listener</code> was added
     * more than once to the same event source, it will be notified one less time after being removed. If
     * <code>listener</code> is null, or was never added, no exception is thrown and no action is taken.
     *
     * @param listener  The PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propSupport.removePropertyChangeListener(listener);
    }

    public List<Key> getKeys() {
        return Collections.unmodifiableList(keys);
    }

    public void addKey(String column, boolean ascending) {
        final List<Key> old = Collections.unmodifiableList(new ArrayList<>(getKeys()));
        final Key key = new Key(column, ascending);
        key.addPropertyChangeListener(keyChangeListener);
        keys.add(key);
        propSupport.firePropertyChange(PROPERTY_KEYS, old, getKeys());
    }

    public void removeKey(Key key) {
        final List<Key> old = Collections.unmodifiableList(new ArrayList<>(getKeys()));
        key.removePropertyChangeListener(keyChangeListener);
        keys.remove(key);
        propSupport.firePropertyChange(PROPERTY_KEYS, old, getKeys());
    }

    public void setName(String name) {
        final String old = this.name;
        this.name = name;
        propSupport.firePropertyChange(PROPERTY_NAME, old, name);
    }

    public void setNameSpace(String nameSpace) {
        final String old = this.nameSpace;
        this.nameSpace = nameSpace;
        propSupport.firePropertyChange(PROPERTY_NAMESPACE, old, nameSpace);
    }

    public void setSparse(boolean sparse) {
        final boolean old = this.sparse;
        this.sparse = sparse;
        propSupport.firePropertyChange(PROPERTY_SPARSE, old, sparse);
    }

    public void setUnqiue(boolean unique) {
        final boolean old = this.unique;
        this.unique = unique;
        propSupport.firePropertyChange(PROPERTY_UNIQUE, old, unique);
    }

    public void setDropDuplicates(boolean dropDuplicates) {
        final boolean old = this.dropDuplicates;
        this.dropDuplicates = dropDuplicates;
        propSupport.firePropertyChange(PROPERTY_DROPDUPLICATES, old, dropDuplicates);
    }

}
