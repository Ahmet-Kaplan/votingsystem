package org.votingsystem.util;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class MetaInfMsg {

    public static String getOKMsg(String methodName, String ... args) {
        String result = methodName + "_OK";
        if(args != null && args.length > 0) {
            for(String arg: args) result = result.concat("_" + arg);
        }
        return result;
    }

    public static String getErrorMsg(String methodName, String ... args) {
        String result = methodName + "_ERROR";
        if(args != null && args.length > 0) {
            for(String arg: args) result = result.concat("_" + arg);
        }
        return result;
    }

    public static String getExceptionMsg(String methodName, Exception ex, String... args) {
        String result = methodName + "_EXCEPTION_" + ex.getClass().getSimpleName();
        if(args != null && args.length > 0) {
            for(String arg: args) result = result.concat("_" + arg);
        }
        return result;
    }

}