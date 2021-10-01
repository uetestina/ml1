/*******************************************************************************
 * Debrief - the Open Source Maritime Analysis Application
 * http://debrief.info
 *
 * (C) 2000-2020, Deep Blue C Technology Ltd
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html)
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *******************************************************************************/

//
// <copyright>
//
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
//
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
//
// </copyright>
// **********************************************************************
//
// $Source: i:/mwc/coag/asset/cvsroot/util/MWC/GUI/S57/support/StreamInputReader.java,v $
// $RCSfile: StreamInputReader.java,v $
// $Revision: 1.1 $
// $Date: 2007/04/27 09:20:02 $
// $Author: ian.mayo $
//
// **********************************************************************

package MWC.GUI.S57.support;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import com.bbn.openmap.util.Debug;

/**
 * An Abstract InputReader to handle reading data from streams, where seeking to
 * a place in the file in front of the current pointer involves closing the
 * stream, and re-seeking from the beginning.
 *
 * @see com.bbn.openmap.io.InputReader
 * @see com.bbn.openmap.io.BinaryFile
 */
public abstract class StreamInputReader implements InputReader {

	/**
	 * The underlying data input stream, for resource files. Is used if the
	 * inputFile is null.
	 */
	protected InputStream inputStream = null;

	/**
	 * Keep track of how many bytes have been read when using the DataInputStream to
	 * read the file.
	 */
	protected long inputStreamCount = 0;

	/**
	 * The source name.
	 */
	protected String name = null;

	/**
	 * Return how many bytes the input stream thinks are available.
	 *
	 * @return the number of bytes remaining to be read (counted in bytes)
	 * @exception IOException Any IO errors encountered in accessing the file
	 */
	@Override
	public long available() throws IOException {
		return inputStream.available();
	}

	/**
	 * Closes the underlying file
	 *
	 * @exception IOException Any IO errors encountered in accessing the file
	 */
	@Override
	public void close() throws IOException {
		try {
			Debug.message("binaryfile", "StreamInputReader.close()");

			// From the Sun Network Programming Guide for 1.4, if
			// there are
			// problems with Connection reset by peer, then you should
			// do this before closing the stream, giving all the data
			// a chance to be read. Haven't decided to do this by
			// default, but put it in here for easy access if people
			// decided they need it.
			if (Debug.debugging("connection_problems")) {
				Thread.sleep(1000);
			}

			if (inputStream != null)
				inputStream.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		inputStream = null;
	}

	/**
	 * Add the number of bytes to the inputStreamCount.
	 *
	 * @return number of bytes added in this call, to pass along to anything else
	 *         that will be interested.
	 */
	protected int count(final int add) {
		inputStreamCount += add;
		return add;
	}

	/**
	 * Get the index of the next character to be read
	 *
	 * @return the index
	 * @exception IOException Any IO errors that occur in accessing the underlying
	 *                        file
	 */
	@Override
	public long getFilePointer() throws IOException {
		return inputStreamCount;
	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * Return how many bytes the input stream thinks make up the file. This is
	 * calculated by adding the number of bytes read to the number of bytes
	 * available. May not be reliable.
	 *
	 * @return the number of bytes remaining to be read (counted in bytes)
	 * @exception IOException Any IO errors encountered in accessing the file
	 */
	@Override
	public long length() throws IOException {
		return inputStreamCount + inputStream.available();
	}

	/**
	 * Read from the file.
	 *
	 * @return one byte from the file. -1 for EOF
	 * @exception IOException Any IO errors encountered in reading from the file
	 */
	@Override
	public int read() throws IOException {
		count(1);
		return inputStream.read();
	}

	/**
	 * Read from the file.
	 *
	 * @param b the byte array to read into. Equivelent to
	 *          <code>read(b, 0, b.length)</code>
	 * @return the number of bytes read
	 * @exception IOException Any IO errors encountered in reading from the file
	 * @see java.io.RandomAccessFile#read(byte[])
	 */
	@Override
	public int read(final byte b[]) throws IOException {
		return inputStream.read(b, 0, b.length);
	}

	/**
	 * Read from the file
	 *
	 * @param b   The byte array to read into
	 * @param off the first array position to read into
	 * @param len the number of bytes to read
	 * @return the number of bytes read
	 * @exception IOException Any IO errors encountered in reading from the file
	 */
	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {

		int gotsofar = 0;
		while (gotsofar < len) {
			final int read = inputStream.read(b, off + gotsofar, len - gotsofar);
			if (read == -1) {
				if (gotsofar > 0) {
					// Hit the EOF in the middle of the loop.
					return gotsofar;
				} else {
					return read;
				}
			} else {
				gotsofar += read;
			}
		}

		count(gotsofar);
		return gotsofar;
	}

	/**
	 * Read from the file.
	 *
	 * @param howmany   the number of bytes to read
	 * @param allowless if we can return fewer bytes than requested
	 * @return the array of bytes read.
	 * @exception FormatException Any IO Exceptions, plus an end-of-file encountered
	 *                            after reading some, but now enough, bytes when
	 *                            allowless was <code>false</code>
	 * @exception EOFException    Encountered an end-of-file while allowless was
	 *                            <code>false</code>, but NO bytes had been read.
	 */
	@Override
	public byte[] readBytes(final int howmany, final boolean allowless) throws EOFException, FormatException {

		final byte foo[] = new byte[howmany];
		int gotsofar = 0;
		int err = 0;
		try {
			while (gotsofar < howmany) {
				err = inputStream.read(foo, gotsofar, howmany - gotsofar);

				if (err == -1) {
					if (allowless) {
						// return a smaller array, so the caller can
						// tell how much
						// they really got
						final byte retval[] = new byte[gotsofar];
						System.arraycopy(foo, 0, retval, 0, gotsofar);
						count(gotsofar);
						return retval;
					} else { // some kind of failure...
						if (gotsofar > 0) {
							throw new FormatException("StreamInputReader: EOF while reading data");
						} else {
							throw new EOFException();
						}
					}
				}
				gotsofar += err;
			}
		} catch (final IOException i) {
			throw new FormatException("StreamInputReader: readBytes IOException: " + i.getMessage());
		}
		count(howmany);
		return foo;
	}

	/**
	 * Reset the DataInputStream to the beginning, by closing the current connection
	 * and reopening it. The StreamInputReader method simply closes the input stream
	 * and resets the input stream count, so the implementation of this class needs
	 * to reopen the stream at the beginning of the source file.
	 */
	protected void reopen() throws IOException {
		if (inputStream != null) {
			Debug.message("binaryfile", "StreamInputReader: Closing inputStream");
			inputStream.close();
		}

		inputStreamCount = 0;
	}

	/**
	 * Set the index of the next character to be read.
	 *
	 * @param pos the position to seek to.
	 * @exception IOException Any IO Errors that occur in seeking the underlying
	 *                        file.
	 */
	@Override
	public void seek(final long pos) throws IOException {
		boolean seekComments = false;
		long skipped;
		if (Debug.debugging("binaryfileseek")) {
			seekComments = true;
		}

		final long curPosition = inputStreamCount;
		if (pos >= curPosition) {
			if (seekComments) {
				Debug.output("StreamInputReader - seeking to " + pos + " from " + curPosition);
			}

			skipped = skipBytes(pos - curPosition);

			if (seekComments) {
				Debug.output("   now at: " + inputStreamCount + ", having skipped " + skipped);
			}
		} else {
			if (seekComments) {
				Debug.output("StreamInputReader - having to start over for seek - " + pos + " from " + curPosition);
			}
			reopen();
			if (seekComments)
				Debug.output("   skipping to: " + pos);
			skipped = skipBytes(pos);
			if (seekComments) {
				Debug.output("   now at: " + inputStreamCount + ", having skipped " + skipped);
			}
		}
	}

	/**
	 * Skip over n bytes in the input file
	 *
	 * @param n the number of bytes to skip
	 * @return the actual number of bytes skipped. annoying, isn't it?
	 * @exception IOException Any IO errors that occur in skipping bytes in the
	 *                        underlying file
	 */
	@Override
	public long skipBytes(final long n) throws IOException {

		long count = 0;
		long gotsofar = 0;

		while (count < n) {
			gotsofar = inputStream.skip(n - count);
			if (gotsofar == 0) { // added from david marklund
				Debug.error("StreamInputReader can't skip " + n + " bytes as instructed");
				break;
			}
			count += gotsofar;
		}

		count((int) count);
		return count;
	}

}