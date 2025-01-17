/*
 * Copyright (C) 2007-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.struct;

import jnr.ffi.Runtime;
import jnr.ffi.Struct;
import jnr.ffi.TstUtil;
import jnr.ffi.annotations.In;
import jnr.ffi.annotations.Out;
import jnr.ffi.annotations.Pinned;
import jnr.ffi.annotations.Transient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UTF8StringFieldTest {
    public UTF8StringFieldTest() {
    }

    public static class StringFieldStruct extends Struct {
        public final UTF8String string = new UTF8String(32);

        public StringFieldStruct() {
            super(runtime);
        }

    }
    public static interface TestLib {
        // This makes use of the string being the first field in the struct
        int string_equals(@Pinned @In @Transient StringFieldStruct s1, String s2);
        int copyByteBuffer(@Pinned @Out StringFieldStruct dst, @In byte[] src, int len);
        int copyByteBuffer(@Pinned @Out byte[] dst, @Pinned @In @Transient StringFieldStruct src, int len);
        int copyByteBuffer(@Pinned @Out StringBuilder dst, @Pinned @In @Transient StringFieldStruct src, int len);
    }

    static TestLib testlib;
    static Runtime runtime;

    @BeforeAll
    public static void setUpClass() throws Exception {
        testlib = TstUtil.loadTestLib(TestLib.class);
        runtime = Runtime.getRuntime(testlib);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }
    @Test public void stringFieldFirstInStruct() {
        StringFieldStruct s = new StringFieldStruct();
        final String MAGIC = "test";
        s.string.set(MAGIC);
        StringBuilder tmp = new StringBuilder(s.string.length());
        testlib.copyByteBuffer(tmp, s, s.string.length());
        assertEquals(MAGIC, tmp.toString(), "String not put into struct correctly");
    }

    public static final class SockaddrUnix extends Struct {
        private final Signed8 sun_len = new Signed8();
        private final Signed8 sun_family = new Signed8();
        private final UTF8String sun_path = new UTF8String(100);

        public SockaddrUnix() {
            super(runtime);
        }
    }

    @Test public void testSockaddrUnix() {
        final int SUN_LEN = 1;
        final int SUN_FAM = 2;
        final String SUN_PATH = "test";

        SockaddrUnix s = new SockaddrUnix();
        s.sun_len.set(SUN_LEN);
        s.sun_family.set(SUN_FAM);
        s.sun_path.set(SUN_PATH);
        assertEquals(SUN_LEN, s.sun_len.intValue(), "Incorrect sun_len value");
        assertEquals(SUN_FAM, s.sun_family.intValue(), "Incorrect sun_fam value");
        assertEquals(SUN_PATH, s.sun_path.toString(), "Incorrect sun_path value");
    }

    public static final class StructWithStringByRef extends Struct {
        private final UTF8StringRef stringValue = new UTF8StringRef();

        public StructWithStringByRef() {
            super(runtime);
        }
    }

    @Test public void testStringByRef() {
        final String testValue = "some test string";
        final StructWithStringByRef struct = new StructWithStringByRef();

        struct.stringValue.set(testValue);

        assertEquals(testValue, struct.stringValue.get());
    }
}
