/*
 * Copyright (c) 2008-2016, GigaSpaces Technologies, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gigaspaces.atntdemo.write;

import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.springframework.transaction.PlatformTransactionManager;

public class Program {
    public static void main(String[] args) {
        GigaSpace gigaSpace = getOrCreateSpace(args.length == 0 ? "demo" : args[0]);
        System.out.println("Connected to space " + gigaSpace.getName());

        // Your code goes here, for example:
        System.out.println("Entries in space: " + gigaSpace.count(null));

        System.out.println("Program completed successfully");
        System.exit(0);
    }

    public static GigaSpace getOrCreateSpace(String spaceName) {
        return getOrCreateSpace(spaceName, null);
    }

    public static GigaSpace getOrCreateSpace(String spaceName, PlatformTransactionManager ptm) {
        if (ptm != null) {
            return new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo")).transactionManager(ptm).create();
        } else {
            return new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo")).create();
        }

        //return new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://3.129.211.152/*/demo").lookupGroups("xap-15.8.1")).clustered(true).create();
        //.credentials("gs-admin","e0c1e047-d425-d408-1dad-be27e8fc7645")
        //return new GigaSpaceConfigurer(new UrlSpaceConfigurer("jini://3.96.54.225/*/demo").lookupGroups("xap-15.5.1")).create();
            /*return new GigaSpaceConfigurer(new SpaceProxyConfigurer(spaceName)
                    .lookupGroups("xap-15.8.0")
                    .lookupLocators("3.96.54.225:4174"))
                    .clustered(true)
                    .create();*/
    }
}
