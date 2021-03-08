package com.gigaspaces.atntdemo.util;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.CannotFindSpaceException;
import org.openspaces.core.space.EmbeddedSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

public class GetSpace {
    public static GigaSpace getOrCreateSpace(String spaceName, String hostName) {
        if (spaceName == null) {
            System.out.println("Space name not provided - creating an embedded space...");
            // return new GigaSpaceConfigurer(new EmbeddedSpaceConfigurer("demo-3")).create();
            return new GigaSpaceConfigurer(new EmbeddedSpaceConfigurer("space")).create();
        } else {
            System.out.printf("Connecting to space %s...%n", spaceName);
            try {
//                return new GigaSpaceConfigurer((new UrlSpaceConfigurer("jini://"+hostName+"/*/"+spaceName)).lookupGroups("xap-15.8.1").credentials("gs-admin","e0c1e047-d425-d408-1dad-be27e8fc7645").secured(true)).clustered(true).gigaSpace();
                return new GigaSpaceConfigurer((new UrlSpaceConfigurer("jini://3.96.54.225/*/demo")).lookupTimeout(5000).lookupGroups("xap-15.5.1")).gigaSpace();
                // return new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://"+hostName+"/*/"+spaceName)).create();
                //  return new GigaSpaceConfigurer(new EmbeddedSpaceConfigurer("demo").lookupGroups("xap-15.5.1").lookupLocators("3.96.54.225:4174")).create();
            } catch (CannotFindSpaceException e) {
                System.err.println("Failed to find space: " + e.getMessage());
                throw e;
            }
        }
    }
}
