/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug 8025505
 * @summary Constant folding deficiency
 * @library /tools/javac/lib
 * @build ToolBox
 * @run main ConstFoldTest
 */

import java.net.URL;
import java.util.List;

public class ConstFoldTest {
    public static void main(String... args) throws Exception {
        new ConstFoldTest().run();
    }

    // This is the test case. This class should end up
    // as straight-line code with no conditionals
    class CFTest {
        void m() {
            int x;
            if (1 != 2)       x=1; else x=0;
            if (1 == 2)       x=1; else x=0;
            if ("" != null) x=1; else x=0;
            if ("" == null) x=1; else x=0;
            if (null == null) x=1; else x=0;
            if (null != null) x=1; else x=0;

            x = 1 != 2        ? 1 : 0;
            x = 1 == 2        ? 1 : 0;
            x = "" != null  ? 1 : 0;
            x = "" == null  ? 1 : 0;
            x = null == null  ? 1 : 0;
            x = null != null  ? 1 : 0;

            boolean b;
            b = 1 != 2         && true;
            b = 1 == 2         || true;
            b = ("" != null) && true;
            b = ("" == null) || true;
            b = (null == null) && true;
            b = (null != null) || true;
        }
    }

    // All of the conditionals above should be eliminated.
    // these if* bytecodes should not be seen
    final String regex = "\\sif(?:null|nonnull|eq|ne){1}\\s";

    void run() throws Exception {
        URL url = ConstFoldTest.class.getResource("ConstFoldTest$CFTest.class");
        String result = ToolBox.javap(new ToolBox.JavaToolArgs().setAllArgs("-c", url.getFile()));
        System.out.println(result);

        List<String> bad_codes = ToolBox.grep(regex, result, "\n");
        if (!bad_codes.isEmpty()) {
            for (String code : bad_codes)
                System.out.println("Bad OpCode Found: " + code);
            throw new Exception("constant folding failed");
        }
    }
}
