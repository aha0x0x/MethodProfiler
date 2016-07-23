package org.aha.profiler;

import java.lang.instrument.Instrumentation;
/**
 * This class is marked as premain and register the main agent
 * @author aha
 */
public class PreMain
{
    public static void premain( String agentArguments, Instrumentation instrumentation )
    {
        instrumentation.addTransformer( new MethodProfiler( agentArguments ) );
    }
}
