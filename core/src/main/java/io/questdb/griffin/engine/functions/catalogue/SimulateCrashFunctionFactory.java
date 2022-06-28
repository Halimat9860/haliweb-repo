/*******************************************************************************
 *     ___                  _   ____  ____
 *    / _ \ _   _  ___  ___| |_|  _ \| __ )
 *   | | | | | | |/ _ \/ __| __| | | |  _ \
 *   | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *    \__\_\\__,_|\___||___/\__|____/|____/
 *
 *  Copyright (c) 2014-2019 Appsicle
 *  Copyright (c) 2019-2022 QuestDB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/

package io.questdb.griffin.engine.functions.catalogue;

import io.questdb.cairo.CairoConfiguration;
import io.questdb.cairo.sql.Function;
import io.questdb.cairo.sql.Record;
import io.questdb.griffin.FunctionFactory;
import io.questdb.griffin.SqlExecutionContext;
import io.questdb.griffin.engine.functions.BooleanFunction;
import io.questdb.std.IntList;
import io.questdb.std.ObjList;
import io.questdb.std.Unsafe;

public class SimulateCrashFunctionFactory implements FunctionFactory {

    private static final SimulateCrashFunction CrashInstance = new SimulateCrashFunction();
    private static final DoNothingInstance Dummy = new DoNothingInstance();
    private static final OutOfMemoryFunction OutOfMemoryInstance = new OutOfMemoryFunction();

    @Override
    public String getSignature() {
        return "simulate_crash(a)";
    }

    @Override
    public Function newInstance(int position,
                                ObjList<Function> args,
                                IntList argPositions,
                                CairoConfiguration configuration,
                                SqlExecutionContext sqlExecutionContext
    ) {

        if (configuration.getSimulateCrashEnabled())  {
            char killType = args.get(0).getChar(null);
            switch (killType) {
                case '0':
                    return CrashInstance;
                case 'M':
                    return OutOfMemoryInstance;
            }
        }
        return Dummy;
    }

    private static class SimulateCrashFunction extends BooleanFunction {
        @Override
        public boolean getBool(Record rec) {
            Unsafe.getUnsafe().getLong(0L);
            return true;
        }
    }

    private static class OutOfMemoryFunction extends BooleanFunction {
        @Override
        public boolean getBool(Record rec) {
            throw new OutOfMemoryError("simulate_crash('M')");
        }
    }

    private static class DoNothingInstance extends BooleanFunction {
        @Override
        public boolean getBool(Record rec) {
            return false;
        }

        @Override
        public boolean isReadThreadSafe() {
            return true;
        }
    }
}
