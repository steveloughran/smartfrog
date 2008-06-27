/** (C) Copyright 1998-2004 Hewlett-Packard Development Company, LP

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

package org.smartfrog.examples.pong;

import java.rmi.RemoteException;

import org.smartfrog.sfcore.prim.Prim;

/**
 * Allows remote communication between opponents.
 */ 
interface Linker extends Prim {
    /**
     * Inform the component that the ball has entered the game arena.
     *
     * @param xPos Position in x axis
     * @param yPos Position in y axis
     * @param xSpeed Speed in x axis
     * @param ySpeed Speed in y axis
     *
     * @throws Exception error while informing the component
     */
    public void incomingBall(double xPos, double yPos, double xSpeed,
        double ySpeed) throws Exception;

    /**
     * Set your opponent for the game.
     *
     * @param opponent opponent
     *
     * @return true if opponent is set else false
     *
     * @throws RemoteException In case of network/rmi error
     */
    public boolean setOpponent(Linker opponent) throws RemoteException;
}