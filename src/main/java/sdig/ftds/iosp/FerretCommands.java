package sdig.ftds.iosp;

import java.util.Locale;

public class FerretCommands {
    static final String forbidden_command[] = {
            "SPAWN"
    };
    static final String command[] = {
            "LETDEQ1",                 // the special alias for LAS data source requests...
            "ALIAS ",
            "CANCEL ",
            "CONTOUR ",
            "DEFINE ",
            "ELIF ",
            "ELSE ",
            "ENDIF ",
            "EXIT ",
            "FILE ",
            "FILL ",
            "FRAME ",
            "GO ",
            "HELP ",
            "IF ",
            "LABEL ",
            "LET ",
            "LIST ",
            "LOAD ",
            "MESSAGE ",
            "PALETTE ",
            "PATTERN ",
            "PAUSE ",
            "PLOT ",
            "POLYGON ",
            "PPLUS ",
            "QUERY ",
            "QUIT ",
            "REPEAT ",
            "SAVE ",
            "SAY ",
            "SET ",
            "SHADE ",
            "SHOW ",
            "SPAWN ",
            "STATISTICS ",
            "UNALIAS ",
            "USE ",
            "USER ",
            "VECTOR ",
            "WHERE ",
            "WIRE "};
    /**
     * Test if a string contains a Ferret command.
     * @param string - the string to test
     * @return true if the string contains a Ferret command; false if it does not.
     */
    static public boolean containsCommand(String string) {
        if ( string.length() == 0 ) return false;
        for ( int i=0; i<command.length; i++ ) {
            if ( string.toUpperCase(Locale.ENGLISH).contains(command[i]))  {
                return true;
            }
        }
        return false;
    }
    /**
     * Test if a string contains a Ferret command.
     * @param string - the string to test
     * @return true if the string contains a Ferret command; false if it does not.
     */
    static public boolean containsForbiddenCommand(String string) {
        for ( int i=0; i<forbidden_command.length; i++ ) {
            if ( string.toUpperCase(Locale.ENGLISH).contains(forbidden_command[i]))  {
                return true;
            }
        }
        return false;
    }
}
