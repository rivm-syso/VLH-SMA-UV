/*
 * Copyright 2002-2016 jamod & j2mod development teams
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.rivm.uvsg.modbus.adapter;

/**
 * Interface defining an input register.
 */
public interface InputRegister {

    /**
     * Returns the content of this <tt>Register</tt> as signed 16-bit integer value
     * (signed short).
     *
     * @return the content as signed short (<tt>int</tt>).
     */
    int toShort();

    /**
     * Returns the content of this <tt>Register</tt> as unsigned 16-bit integer value
     * (unsigned short).
     *
     * @return the content as unsigned short (<tt>int</tt>).
     */
    int toUnsignedShort();

    /**
     * Returns the content of this <tt>Register</tt> as a pair of bytes.
     *
     * @return a <tt>byte[]</tt> with length 2.
     */
    byte[] getBytePair();

}
