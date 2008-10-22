/* (C) Copyright 2008 Hewlett-Packard Development Company, LP

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
package org.smartfrog.sfcore.utils;

/**
 * Simple wrapper class to track timeout intervals; this code gets used often enough
 * that a single place for it eliminates duplication
 */

public class TimeoutInterval {

    private long timeout;
    private long startTime;

    /**
     * Create a new interval
     * @param delay delay in milliseconds before timeout is declared
     */
    public TimeoutInterval(long delay) {
        startTime = System.currentTimeMillis();
        timeout = startTime + delay;
    }

    public long getStartTime() {
        return startTime;
    }

    /**
     * Check for the timeout interval expiring
     * @return
     */
    public boolean hasTimedOut() {
        return System.currentTimeMillis() > timeout;
    }

    /**
     * Sleep for a period of time
     * @param interval interval to sleep
     * @return return true if the sleep completed without interruption
     */
    public boolean sleep(long interval) {
        try {
            Thread.sleep(interval);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }
}
