/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */
package com.gemstone.gemfire.internal.cache.persistence.soplog;

import java.io.File;
import java.io.IOException;
import java.util.EnumMap;

import org.apache.hadoop.hbase.io.hfile.BlockCache;

import com.gemstone.gemfire.internal.cache.persistence.soplog.Compactor.MetadataCompactor;
import com.gemstone.gemfire.internal.cache.persistence.soplog.SortedReader.Metadata;
import com.gemstone.gemfire.internal.cache.persistence.soplog.SortedReader.SerializedComparator;

/**
 * Provides a means to construct a soplog.
 */
public interface SortedOplogFactory {
  /**
   * Configures a <code>SortedOplog</code>.
   * 
   * @author bakera
   */
  public class SortedOplogConfiguration {
    /** the default metadata compactor */
    public static MetadataCompactor DEFAULT_METADATA_COMPACTOR = new MetadataCompactor() {
      @Override
      public byte[] compact(byte[] metadata1, byte[] metadata2) {
        return metadata1;
      }
    };
    
    /**
     * Defines the available checksum algorithms.
     */
    public enum Checksum {
      NONE,
      CRC32
    }
    
    /**
     * Defines the available compression algorithms.
     */
    public enum Compression { 
      NONE, 
    }
    
    /**
     * Defines the available key encodings.
     */
    public enum KeyEncoding { 
      NONE, 
    }

    /** the soplog name */
    private final String name;
    
    /** the statistics */
    private final SortedOplogStatistics stats;
    
    private final HFileStoreStatistics storeStats;
    
    /** true if bloom filters are enabled */
    private boolean bloom;
    
    /** the soplog block size */
    private int blockSize;
    
    /** the number of bytes for each checksum */
    private int bytesPerChecksum;
    
    /** the checksum type */
    private Checksum checksum;
    
    /** the compression type */
    private Compression compression;
    
    /** the key encoding type */
    private KeyEncoding keyEncoding;
    
    /** the comparator */
    private SerializedComparator comparator;

    /** metadata comparers */
    private EnumMap<Metadata, MetadataCompactor> metaCompactors;

    private BlockCache blockCache;

    private boolean cacheDataBlocksOnRead;
    
    public SortedOplogConfiguration(String name) {
      this(name, null, new SortedOplogStatistics("GridDBRegionStatistics", name), new HFileStoreStatistics("GridDBStoreStatistics", name));
    }
    
    public SortedOplogConfiguration(String name, BlockCache blockCache, SortedOplogStatistics stats, HFileStoreStatistics storeStats) {
      this.name = name;
      this.stats = stats;
      
      // defaults
      bloom = true;
      blockSize = 1 << 16;
      bytesPerChecksum = 1 << 14;
      checksum = Checksum.NONE;
      compression = Compression.NONE;
      keyEncoding = KeyEncoding.NONE;
      comparator = new ByteComparator();
      this.cacheDataBlocksOnRead = true;
      this.storeStats = storeStats;
      this.blockCache = blockCache;
    }
    
    public SortedOplogConfiguration setBloomFilterEnabled(boolean enabled) {
      this.bloom = enabled;
      return this;
    }
    
    public SortedOplogConfiguration setBlockSize(int size) {
      this.blockSize = size;
      return this;
    }
    
    public SortedOplogConfiguration setBytesPerChecksum(int bytes) {
      this.bytesPerChecksum = bytes;
      return this;
    }
    
    public SortedOplogConfiguration setChecksum(Checksum type) {
      this.checksum = type;
      return this;
    }
    
    public SortedOplogConfiguration setCompression(Compression type) {
      this.compression = type;
      return this;
    }
    
    public SortedOplogConfiguration setKeyEncoding(KeyEncoding type) {
      this.keyEncoding = type;
      return this;
    }
    
    public SortedOplogConfiguration setComparator(SerializedComparator comp) {
      this.comparator = comp;
      return this;
    }
    
    public SortedOplogConfiguration addMetadataCompactor(Metadata name, MetadataCompactor compactor) {
      metaCompactors.put(name, compactor);
      return this;
    }
    
    /**
     * Returns the soplog name.
     * @return the name
     */
    public String getName() {
      return name;
    }

    /**
     * Returns the statistics.
     * @return the statistics
     */
    public SortedOplogStatistics getStatistics() {
      return stats;
    }
    
    public HFileStoreStatistics getStoreStatistics() {
      return storeStats;
    }
    
    /**
     * Returns true if the bloom filter is enabled.
     * @return true if enabled
     */
    public boolean isBloomFilterEnabled() {
      return bloom;
    }

    /**
     * Returns the block size in bytes.
     * @return the block size
     */
    public int getBlockSize() {
      return blockSize;
    }

    /**
     * Returns the number of bytes per checksum.
     * @return the bytes
     */
    public int getBytesPerChecksum() {
      return bytesPerChecksum;
    }

    /**
     * Returns the checksum type.
     * @return the checksum
     */
    public Checksum getChecksum() {
      return checksum;
    }

    /**
     * Returns the compression type.
     * @return the compression
     */
    public Compression getCompression() {
      return compression;
    }

    /**
     * Returns the key encoding type.
     * @return the key encoding
     */
    public KeyEncoding getKeyEncoding() {
      return keyEncoding;
    }

    /**
     * Returns the comparator.
     * @return the comparator
     */
    public SerializedComparator getComparator() {
      return comparator;
    }
    
    /**
     * Returns the metadata compactor for the given name. 
     * @param name the metadata name
     * @return the compactor
     */
    public MetadataCompactor getMetadataCompactor(Metadata name) {
      MetadataCompactor mc = metaCompactors.get(name);
      if (mc != null) {
        return mc;
      }
      return DEFAULT_METADATA_COMPACTOR;
    }

    public BlockCache getBlockCache() {
      return this.blockCache;
    }

    public boolean getCacheDataBlocksOnRead() {
      return cacheDataBlocksOnRead ;
    }
  }
  
  /**
   * Returns the configuration.
   * @return the configuration
   */
  SortedOplogConfiguration getConfiguration();
  
  /**
   * Creates a new soplog.
   * 
   * @param name the filename
   * @return the soplog
   * @throws IOException error creating soplog
   */
  SortedOplog createSortedOplog(File name) throws IOException;
}
