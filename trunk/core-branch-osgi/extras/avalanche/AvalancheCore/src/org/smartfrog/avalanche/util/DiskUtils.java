/**
(C) Copyright 1998-2007 Hewlett-Packard Development Company, LP

This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information: www.smartfrog.org
*/
/*
 * Created on Jan 30, 2005
 *
 */
package org.smartfrog.avalanche.util;
import java.io.*; 
import java.nio.channels.*;


/**
 * @author sanjay, Jan 30, 2005
 *
 * TODO 
 */
public class DiskUtils {

	public static final int BUF_SIZE = 50000 ;
    private static byte[] BUF = new byte[BUF_SIZE];
    
	/**
	 * 
	 */
	public DiskUtils() {
		super();
	}

	public static void fCopy(FileInputStream src, File dest)
		throws IOException{
		if( null == src || null == dest ){
			//TODO
		}
		
        FileChannel srcChannel = src.getChannel();
        FileChannel dstChannel = new FileOutputStream(dest).getChannel();
        dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
    
        // Close the channels
        srcChannel.close();
        dstChannel.close();
	}
	
    /**
     * Copies an <code>InputStream</code> to a file 
     * 
     * @param in stream to copy from 
     * @param outputFile file to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     */
    public static long fCopy(InputStream in, File outputFile) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(outputFile);
            return fCopy(in, out);
        } finally {
            if (out != null) {
                try {
                    in.close();
                    out.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using a local internal buffer for performance.
     * Compared to {@link #globalBufferCopy(InputStream, OutputStream)} this method allows for better
     * concurrency, but each time it is called generates a buffer which will be garbage.
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #globalBufferCopy(InputStream, OutputStream)
     */
    public static long fCopy(InputStream in, OutputStream out) throws IOException {
        // we need a buffer of our own, so no one else interferes
        byte[] buf = new byte[BUF_SIZE];
        return copy(in, out, buf);
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using a global internal buffer for performance.
     * Compared to {@link #copy(InputStream, OutputStream)} this method generated no garbage,
     * but decreases concurrency.
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #copy(InputStream, OutputStream)
     */
    public static long globalBufferCopy(InputStream in, OutputStream out) throws IOException {
        synchronized (BUF) {
            return copy(in, out, BUF);
        }
    }

    /**
     * Copies an <code>InputStream</code> to an <code>OutputStream</code> using the specified buffer. 
     * 
     * @param in stream to copy from 
     * @param out stream to copy to
     * @param copyBuffer buffer used for copying
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs (may result in partially done work)  
     * @see #globalBufferCopy(InputStream, OutputStream)
     * @see #copy(InputStream, OutputStream)
     */
    public static long copy(InputStream in, OutputStream out, byte[] copyBuffer) throws IOException {
        long bytesCopied = 0;
        int read = -1;

        while ((read = in.read(copyBuffer, 0, copyBuffer.length)) != -1) {
            out.write(copyBuffer, 0, read);
            bytesCopied += read;
        }
        return bytesCopied;
    }	
    
    /**
     * Read a file fully into a string buffer.
     * @param file
     * @return
     * @throws IOException
     */
    public static StringBuffer readFile(File file) throws IOException{
    	StringBuffer buf = new StringBuffer();
    	
    	if( null == file){
    		return null;
    	}
    	
    	if( !file.exists() || !file.canRead()){
    		throw new IOException("Error! File is not accessible : " + file);
    	}
    	
    	BufferedReader reader = new BufferedReader(new FileReader(file));
    	String str = null; 
    	while(null != (str=reader.readLine())){
    		buf.append(str + "\n");
    	}
    	reader.close();
    	return buf;
    }
    /**
     * Reads last (numLines) lines from end of a file. 
     * @param file
     * @param numLines
     * @return
     * @throws IOException
     */
    public static StringBuffer tail(File file, int numLines) throws IOException{
    	StringBuffer buf = new StringBuffer();
    	String[] strings = new String[numLines];
    	
    	if( null == file){
    		return null;
    	}
    	//  dont even open the file if no lines need to be read. 
    	//  following algo doesnt work for zero lines. 
    	if( 0 == numLines){
    		return buf;
    	}
    	
    	if( !file.exists() || !file.canRead()){
    		throw new IOException("Error! File is not accessible : " + file);
    	}
    	if( file.isDirectory()){
    		throw new IOException("Error! File is a directory : " + file);
    	}
    	
    	BufferedReader reader = new BufferedReader(new FileReader(file));    	
    	String str = null;
    	int totalLines = 0;
    	for(int lineCtr = 0 ; null != (str = reader.readLine() ); totalLines++){
    		
    		// first check if end of array has reached.
    		// makes lines to rotate in array, 
    		if( lineCtr == strings.length){
    			lineCtr = 0;
    		}
    		// this wont work if strings.length == 0, but that ok since we already checked  
    		// for that.  
    		strings[lineCtr++] = str + "\n"; 
    	}
    	
    	// this is where we need to start reading the (circular) array from 
    	int firstLineIndex = ( totalLines < strings.length)? 0: totalLines % strings.length;
    	// this is where we stop, useful if file is smaller than number of lines .
    	int endMarker = ( totalLines < strings.length)?totalLines:strings.length ;
    	
    	for( int i=0, ctr = firstLineIndex; i<endMarker; i++){
    		buf.append(strings[ctr++]);
    		if( ctr == strings.length){
    			ctr = 0;
    		}
    	}
    	
    	return buf;
    }
    public static StringBuffer tail(String filepath, int numLines) throws IOException{
    	return tail(new File(filepath), numLines);
    }
    
    public static void main(String []args) throws Exception{
    	String file = "e:\\sanjay\\test.txt";
    	
    	System.out.println(" tail(file, 0).......  ");
    	System.out.println(""+ tail(file, 0));
    	
    	System.out.println(" tail(file, 1000).......  ");
    	System.out.println(""+ tail(file, 1000));

/*
    	System.out.println(" tail(file, 461).......  ");
    	System.out.println(""+ tail(file, 461));

    	System.out.println(" tail(file, 61).......  ");
    	System.out.println(""+ tail(file, 61));

    	System.out.println(" tail(file, 10).......  ");
    	System.out.println(""+ tail(file, 10));
    	
*/    	
    }
	
}
