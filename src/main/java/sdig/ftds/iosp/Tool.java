package sdig.ftds.iosp;

/**
 * Tool class which defines a method for getting path to the resources this tool uses.
 *
 * @author Roland Schweitzer
 */
public class Tool {
    /**
     * Resolve the full path name for the location of the resource used by this tool.
     *
     * @param resource The
     * @return the fully qualified path of the requested resource
     */
    public String getResourcePath(String resource) {

        return JDOM2Utils.getResourcePath(this, resource);

    }
}
