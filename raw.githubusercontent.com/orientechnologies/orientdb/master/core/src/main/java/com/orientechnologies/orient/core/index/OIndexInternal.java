/*
 *
 *  *  Copyright 2014 Orient Technologies LTD (info(at)orientechnologies.com)
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *       http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *  *
 *  * For more information: http://www.orientechnologies.com
 *
 */
package com.orientechnologies.orient.core.index;

import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.tx.OTransactionIndexChanges;

import java.util.Collection;
import java.util.concurrent.locks.Lock;

/**
 * Interface to handle index.
 *
 * @author Luca Garulli (l.garulli--at--orientechnologies.com)
 */
public interface OIndexInternal<T> extends OIndex<T> {

  String CONFIG_KEYTYPE            = "keyType";
  String CONFIG_AUTOMATIC          = "automatic";
  String CONFIG_TYPE               = "type";
  String ALGORITHM                 = "algorithm";
  String VALUE_CONTAINER_ALGORITHM = "valueContainerAlgorithm";
  String CONFIG_NAME               = "name";
  String INDEX_DEFINITION          = "indexDefinition";
  String INDEX_DEFINITION_CLASS    = "indexDefinitionClass";
  String INDEX_VERSION             = "indexVersion";
  String METADATA                  = "metadata";

  Object getCollatingValue(final Object key);

  /**
   * Loads the index giving the configuration.
   *
   * @param iConfig ODocument instance containing the configuration
   */
  boolean loadFromConfiguration(ODocument iConfig);

  /**
   * Saves the index configuration to disk.
   *
   * @return The configuration as ODocument instance
   *
   * @see #getConfiguration()
   */
  ODocument updateConfiguration();

  /**
   * Add given cluster to the list of clusters that should be automatically indexed.
   *
   * @param iClusterName Cluster to add.
   *
   * @return Current index instance.
   */
  OIndex<T> addCluster(final String iClusterName);

  /**
   * Remove given cluster from the list of clusters that should be automatically indexed.
   *
   * @param iClusterName Cluster to remove.
   *
   * @return Current index instance.
   */
  OIndex<T> removeCluster(final String iClusterName);

  /**
   * Indicates whether given index can be used to calculate result of
   * {@link com.orientechnologies.orient.core.sql.operator.OQueryOperatorEquality} operators.
   *
   * @return {@code true} if given index can be used to calculate result of
   * {@link com.orientechnologies.orient.core.sql.operator.OQueryOperatorEquality} operators.
   */
  boolean canBeUsedInEqualityOperators();

  boolean hasRangeQuerySupport();

  /**
   * Applies exclusive lock on keys which prevents read/modification of this keys in following methods:
   *
   * <ol>
   * <li>{@link #put(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #checkEntry(com.orientechnologies.orient.core.db.record.OIdentifiable, Object)}</li>
   * <li>{@link #remove(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #remove(Object)}</li>
   * </ol>
   *
   * <p>
   * If you want to lock several keys in single thread, you should pass all those keys in single method call. Several calls of this
   * method in single thread are not allowed because it may lead to deadlocks.
   * </p>
   *
   * This is internal method and cannot be used by end users.
   *
   * @param key Keys to lock.
   */
  void lockKeysForUpdate(Object... key);

  /**
   * Applies exclusive lock on keys which prevents read/modification of this keys in following methods:
   *
   * <ol>
   * <li>{@link #put(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #checkEntry(com.orientechnologies.orient.core.db.record.OIdentifiable, Object)}</li>
   * <li>{@link #remove(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #remove(Object)}</li>
   * </ol>
   *
   * <p>
   * If you want to lock several keys in single thread, you should pass all those keys in single method call. Several calls of this
   * method in single thread are not allowed because it may lead to deadlocks.
   * </p>
   *
   * This is internal method and cannot be used by end users.
   *
   * @param keys Keys to lock.
   *
   * @return the array of locks which should be unlocked when done.
   */
  Lock[] lockKeysForUpdate(Collection<Object> keys);

  /**
   * Release exclusive lock on keys which prevents read/modification of this keys in following methods:
   *
   * <ol>
   * <li>{@link #put(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #checkEntry(com.orientechnologies.orient.core.db.record.OIdentifiable, Object)}</li>
   * <li>{@link #remove(Object, com.orientechnologies.orient.core.db.record.OIdentifiable)}</li>
   * <li>{@link #remove(Object)}</li>
   * </ol>
   *
   * This is internal method and cannot be used by end users.
   *
   * @param key Keys to unlock.
   */
  void releaseKeysForUpdate(Object... key);

  OIndexMetadata loadMetadata(ODocument iConfig);

  void setRebuildingFlag();

  void close();

  void preCommit();

  void addTxOperation(final OTransactionIndexChanges changes);

  void commit();

  void postCommit();

  void setType(OType type);

  /**
   * <p>
   * Returns the index name for a key. The name is always the current index name, but in cases where the index supports key-based
   * sharding.
   *
   * @param key the index key.
   *
   * @return The index name involved
   */
  String getIndexNameByKey(Object key);

  /**
   * <p>
   * Acquires exclusive lock in the active atomic operation running on the current thread for this index.
   *
   * <p>
   * If this index supports a more narrow locking, for example key-based sharding, it may use the provided {@code key} to infer a
   * more narrow lock scope, but that is not a requirement.
   *
   * @param key the index key to lock.
   *
   * @return {@code true} if this index was locked entirely, {@code false} if this index locking is sensitive to the provided {@code
   * key} and only some subset of this index was locked.
   */
  boolean acquireAtomicExclusiveLock(Object key);
}
