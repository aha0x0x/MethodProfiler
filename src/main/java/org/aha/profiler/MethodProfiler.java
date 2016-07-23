package org.aha.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Optional;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
/**
 * Main profiler class which will insert profiling statements at the start of the 
 * given class and methods.Profiler can accept commandline arguments in the form of "class:<method>"
 * If no method name is provided, profiling statements will be added to all methods of the given class. 
 * @author aha
 */
class MethodProfiler implements ClassFileTransformer
{
    private final Optional<String> mClassName;
    private final Optional<String> mMethodName;
    
    public MethodProfiler( String cmdlineArguments  )
    {
        // initialize className and methodName. WildCards are not supported. 
        
        if( cmdlineArguments == null )
        {
            // no input , no work 
            mClassName = Optional.empty();
            mMethodName = Optional.empty();
        }
        else
        {
            String[] args = cmdlineArguments.split( ":" );
            if( args.length == 1 )
            {
                // all methods of the given class will be profiled 
                mClassName = Optional.of( args[0].replaceAll("\\.", "/") );
                mMethodName = Optional.empty();
            }
            else if( args.length == 2 )
            {
                // given method of the given class will be profiled 
                mClassName = Optional.of( args[0].replaceAll( "\\.", "/") );
                mMethodName = Optional.of( args[1].replaceAll( "\\.", "/") );
            }
            else
            {
                // input we do not understand ; default to no-input
                mClassName = Optional.empty();
                mMethodName = Optional.empty();
            }
        }
    }

    @Override
    public byte[] transform( ClassLoader loader, 
                             String className, 
                             Class<?> classBeingRedefined, 
                             ProtectionDomain protectionDomain, 
                             byte[] classfileBuffer ) throws IllegalClassFormatException
    {
        // if profiler was not initialized with a classname no profiling will be done. 
        if( !mClassName.isPresent() )
        {
            return null;
        }
        
        // check and log when the given class is found and insert profiling statement
        if( mClassName.get().equalsIgnoreCase( className ) )
        {
            // debug statement - redirect to logs etc.
            System.out.println("Found Class=" + mClassName.get() );
            return insertProfilingStatement( classfileBuffer );
        }
        return null;
    }
    
    private byte[] insertProfilingStatement( byte[] classBuffer ) 
    {
        // for the given classbuffer, get the list of declared methods. 
        // insert profiling statements for the given method(s)
        
        ClassPool pool = ClassPool.getDefault();
        CtClass cl = null;
        
        try 
        {
            cl = pool.makeClass(new java.io.ByteArrayInputStream( classBuffer ) );
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            
            for (int i = 0; i < methods.length; i++) 
            {
                if( !mMethodName.isPresent() )
                {
                    changeMethod(methods[i]);
                }
                else
                {
                    if ( mMethodName.get().equalsIgnoreCase( methods[i].getName() ) 
                         || mMethodName.get().equalsIgnoreCase( methods[i].getLongName() ) )
                    {
                        changeMethod(methods[i]);
                    }
                }
            }
            
            classBuffer = cl.toBytecode();
        }
        catch (Exception e ) 
        {
            e.printStackTrace();
        }
        finally 
        {
            if (cl != null) 
            {
                cl.detach();
            }
        }
        return classBuffer;
  }
 
    private void changeMethod( CtBehavior method ) throws NotFoundException, CannotCompileException 
    {
        // debug statement 
        System.out.println( "Inserting profile statement to " + method.getLongName() );
        
        method.insertBefore( "System.out.println(\" [ \" + java.time.Instant.now() + \"] invoked with \" + java.util.Arrays.toString( $args ) );");
        method.insertAfter( "System.out.println(\" [ \" + java.time.Instant.now() + \"] invocation complete. Was invoked with \" + java.util.Arrays.toString( $args ) );");
        
    }
  }