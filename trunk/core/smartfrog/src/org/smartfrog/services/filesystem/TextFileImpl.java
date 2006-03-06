/** (C) Copyright 2005 Hewlett-Packard Development Company, LP

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
package org.smartfrog.services.filesystem;

import org.smartfrog.sfcore.common.SmartFrogException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.File;
import java.rmi.RemoteException;

/**
 * A text file. By extending {@link SelfDeletingFileImpl}, we ensure that
 * created 30-Mar-2005 16:37:56
 */

public class TextFileImpl extends SelfDeletingFileImpl implements TextFile {

    public TextFileImpl() throws RemoteException {
    }



    /**
     * when we deploy, we write out our text stream
     * @throws SmartFrogException
     * @throws RemoteException
     */
    public synchronized void sfStart() throws SmartFrogException,
            RemoteException {
        //now write the string value if needed.
        String text = sfResolve(ATTR_TEXT, (String) null, false);
        String encoding = null;
        if (text != null) {
            encoding = sfResolve(ATTR_TEXT_ENCODING, (String)null, true);
            Writer wout=null;
            File file = getFile();
            assert file!=null;
            try {
                OutputStream fout;
                fout = new FileOutputStream(file);
                wout = new OutputStreamWriter(fout, encoding);
                wout.write(text);
                wout.flush();
                wout.close();
            } catch (IOException ioe) {
                if (wout != null) {
                    try {
                        wout.close();
                    } catch (IOException ignored) {
                        //ignore this
                    }
                }
                throw SmartFrogException.forward("When trying to write to " +
                        file,
                        ioe);
            }
        }
    }

}
