/*
 * Copyright 2004-2010 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.hyk.proxy.common.rpc.extension.compress.lzf;

/**
 * Each data compression algorithm must implement this interface.
 */
public interface Compressor {

	/**
     * Compress a number of bytes.
     *
     * @param in the input data
     * @param inLen the number of bytes to compress
     * @param out the output area
     * @param outPos the offset at the output array
     * @return the end position
     */
    int compress(byte[] in, int inLen, byte[] out, int outPos);

    /**
     * Expand a number of compressed bytes.
     *
     * @param in the compressed data
     * @param inPos the offset at the input array
     * @param inLen the number of bytes to read
     * @param out the output area
     * @param outPos the offset at the output array
     * @param outLen the size of the uncompressed data
     */
    void expand(byte[] in, int inPos, int inLen, byte[] out, int outPos, int outLen);


}
