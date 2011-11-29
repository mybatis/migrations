package org.apache.ibatis.migration.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

public final class InfoCommand
    implements Command
{

    private final PrintStream out;

    public InfoCommand( PrintStream out )
    {
        this.out = out;
    }

    public void execute( String... params )
    {
        Properties properties = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream( "META-INF/maven/org.mybatis/mybatis-migrations/pom.properties" );

        if ( input != null )
        {
            try
            {
                properties.load( input );
            }
            catch ( IOException e )
            {
                // ignore, just don't load the properties
            }
            finally
            {
                try
                {
                    input.close();
                }
                catch ( IOException e )
                {
                    // close quietly
                }
            }
        }

        out.printf( "%s %s (%s)%n",
                    properties.getProperty( "name" ),
                    properties.getProperty( "version" ),
                    properties.getProperty( "build" ) );
        out.printf( "Java version: %s, vendor: %s%n",
                    System.getProperty( "java.version" ),
                    System.getProperty( "java.vendor" ) );
        out.printf( "Java home: %s%n", System.getProperty( "java.home" ) );
        out.printf( "Default locale: %s_%s, platform encoding: %s%n",
                    System.getProperty( "user.language" ),
                    System.getProperty( "user.country" ),
                    System.getProperty( "sun.jnu.encoding" ) );
        out.printf( "OS name: \"%s\", version: \"%s\", arch: \"%s\", family: \"%s\"%n",
                    System.getProperty( "os.name" ),
                    System.getProperty( "os.version" ),
                    System.getProperty( "os.arch" ),
                    getOsFamily() );
    }

    private static final String getOsFamily()
    {
        String osName = System.getProperty( "os.name" ).toLowerCase();
        String pathSep = System.getProperty( "path.separator" );

        if ( osName.indexOf( "windows" ) != -1 )
        {
            return "windows";
        }
        else if ( osName.indexOf( "os/2" ) != -1 )
        {
            return "os/2";
        }
        else if ( osName.indexOf( "z/os" ) != -1 || osName.indexOf( "os/390" ) != -1 )
        {
            return "z/os";
        }
        else if ( osName.indexOf( "os/400" ) != -1 )
        {
            return "os/400";
        }
        else if ( pathSep.equals( ";" ) )
        {
            return "dos";
        }
        else if ( osName.indexOf( "mac" ) != -1 )
        {
            if ( osName.endsWith( "x" ) )
            {
                return "mac"; // MACOSX
            }
            return "unix";
        }
        else if ( osName.indexOf( "nonstop_kernel" ) != -1 )
        {
            return "tandem";
        }
        else if ( osName.indexOf( "openvms" ) != -1 )
        {
            return "openvms";
        }
        else if ( pathSep.equals( ":" ) )
        {
            return "unix";
        }

        return "undefined";
    }

}
