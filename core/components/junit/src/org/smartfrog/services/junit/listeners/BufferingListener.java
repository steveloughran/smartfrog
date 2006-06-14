/** (C) Copyright 2004 Hewlett-Packard Development Company, LP

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 For more information: www.smartfrog.org

 */


package org.smartfrog.services.junit.listeners;

import org.smartfrog.services.junit.TestListenerFactory;
import org.smartfrog.services.junit.data.TestInfo;

import java.rmi.RemoteException;

/**
 * extend buffering listener with some operations to get at
 * the errors. It is over-chatty for a long-haul connection, but
 * since its goal is for unit testing of the junit components, we
 * can absorb that hit.
 * Remember that the count of errors/failures/starts and ends increase
 * monotically, and can do so between calls to, say,
 * getErrorCount and getErrorInfo.
 * Date: 07-Jul-2004
 * Time: 17:08:37
 */

public interface BufferingListener extends TestListenerFactory {

    /**
     * get the number of errors
     * @return
     * @throws RemoteException
     */
    int getErrorCount() throws RemoteException;

    /**
     * get the error at that point in the list
     * @param entry
     * @return a copy of the error
     * @throws RemoteException
     * @throws IndexOutOfBoundsException if the entry is out of range
     */
    TestInfo getErrorInfo(int entry) throws RemoteException, IndexOutOfBoundsException;

    /**
     * get the number of starts
     *
     * @return
     * @throws RemoteException
     */
    int getStartCount() throws RemoteException;

    /**
     * get the starts at that point in the list
     *
     * @param entry the list entry beginning at zero
     * @return a copy of the info
     * @throws RemoteException
     * @throws IndexOutOfBoundsException if the entry is out of range
     */
    TestInfo getStartInfo(int entry) throws RemoteException, IndexOutOfBoundsException;


    /**
     * get the number of end
     *
     * @return
     * @throws RemoteException
     */
    int getEndCount() throws RemoteException;

    /**
     * get the end at that point in the list
     *
     * @param entry the list entry beginning at zero
     * @return a copy of the info
     * @throws RemoteException
     * @throws IndexOutOfBoundsException if the entry is out of range
     */
    TestInfo getEndInfo(int entry) throws RemoteException, IndexOutOfBoundsException;


    /**
     * get the number of failures
     *
     * @return
     * @throws RemoteException
     */
    int getFailureCount() throws RemoteException;

    /**
     * get the failures at that point in the list
     *
     * @param entry the list entry beginning at zero
     * @return a copy of the info
     * @throws RemoteException
     * @throws IndexOutOfBoundsException if the entry is out of range
     */
    TestInfo getFailureInfo(int entry) throws RemoteException, IndexOutOfBoundsException;

    /**
     * returns true iff all tests passed
     * @return test success flag
     * @throws RemoteException
     */
    boolean testsWereSuccessful() throws RemoteException;

    /**
     * get the number of times that callers started listening
     *
     * @return and interface that should have events reported to it
     *
     * @throws RemoteException
     */
    int getSessionStartCount() throws RemoteException;

    /**
     * get the number of times that callers ended listening
     * @return
     * @throws RemoteException
     */
    int getSessionEndCount() throws RemoteException;
}
