/*
 *   Copyright (C) 2016 Pawel Badenski
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package test.halik;

import java.io.IOException;
import java.io.InputStream;

public class Some {
    public void indexStream(Object object, StreamAccess streamAccess) throws Exception {
        try {
            InputStream stream = streamAccess.accessInputStream();
            try {
                object.equals( stream );
            }
            finally {
                try {
                    stream.close();
                }
                catch (Exception ignore) {
                }
            }
        }
        catch ( IOException e ) {
            throw exception( "Unable to index from stream " + streamAccess.toString(), e );
        }
    }

    private Exception exception(String message, Exception cause) {
        return new RuntimeException(message, cause);
    }
}
