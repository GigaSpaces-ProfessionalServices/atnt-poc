package com.gigaspaces.atntdemo.modifier;

import com.gigaspaces.atntdemo.model.Person;
import com.gigaspaces.client.ReadModifiers;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@RestController
public class ReadModifiedController {
    @GetMapping("/")
    public String indexPage() {
        String systemipaddress = "localhost";
        //get dynamic ip, uncomment if you are not using local server
        /*try
        {
            URL url_name = new URL("http://bot.whatismyipaddress.com");

            BufferedReader sc =
                    new BufferedReader(new InputStreamReader(url_name.openStream()));

            // reads system IPAddress
            systemipaddress = sc.readLine().trim();
        }
        catch (Exception e)
        {
            System.out.println("Cannot Execute Properly");
            systemipaddress = "localhost";
        }*/
        String result = "<div style='text-align:center;'><u><b>Available endpoints</b></u> : <br><br>";
        result += "<a href='http://" + systemipaddress + ":12000/gigaspaces/runConcurrentReadAndWrite' target='_blank'>Concurrent Read And Write</a><br><br>";
        result += "<a href='http://" + systemipaddress + ":12000/gigaspaces/runConcurrentReadAndWriteWithSleep' target='_blank'>Concurrent Read And Write With Sleep</a><br><br>";
        result += "<a href='http://" + systemipaddress + ":12000/gigaspaces/runMultipleWriteTransaction' target='_blank'>Multiple Write with Transaction</a><br><br>";
        return result;
    }

    @GetMapping("/runConcurrentReadAndWrite")
    public String runConcurrentReadAndWrite() {
        int noOfThreads = 2;
        System.out.println("Start runConcurrentReadAndWrite");
        /*
        5 thread read id=1 READ_COMMITTED
        5 thread write id=1 transaction repeatable_read
        expected: error while writing
         */

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);

            PlatformTransactionManager ptmRC = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRC = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo").lookupGroups("xap-15.8.0"))
                    .transactionManager(ptmRC)
                    .defaultReadModifiers(ReadModifiers.READ_COMMITTED).create();

            PlatformTransactionManager ptmRR = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRR = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo").lookupGroups("xap-15.8.0"))
                    .transactionManager(ptmRR)
                    .defaultReadModifiers(ReadModifiers.REPEATABLE_READ).create();


            // READ threads
            for (int i = 0; i < noOfThreads; i++) {
                if (i % 2 == 0) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Person template = new Person();
                                template.setId(1L);

                                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                                //configure the transaction definition
                                definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
                                TransactionStatus status = ptmRC.getTransaction(definition);
                                try { //do things with the GigaSpace instance...
                                    Person p1 = gigaSpaceRC.read(template);
                                    System.out.println("Read - " + p1);
                                } catch (Exception e) {
                                    System.out.printf("Start Error during the read");
                                    e.printStackTrace();
                                    System.out.printf("End Error during the read");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Person p = new Person(1l, "firtName", "lastName", 50);
                                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                                //configure the transaction definition
                                definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
                                TransactionStatus status = ptmRR.getTransaction(definition);
                                try { //do things with the GigaSpace instance...
                                    gigaSpaceRR.write(p);
                                    System.out.println("Trying to Write - " + p);
                                    ptmRR.commit(status);
                                    System.out.println("Write committed");
                                } catch (Exception e) {
                                    ptmRR.rollback(status);
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
            System.out.println("before shutdown");
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("after shutdown");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End runConcurrentReadAndWrite");
        return "SUCCESS";
    }

    @GetMapping("/runConcurrentReadAndWriteWithSleep")
    public String runConcurrentReadAndWriteWithSleep() {
        int noOfThreads = 4;
        System.out.println("Start runConcurrentReadAndWriteWithSleep");
        /*
            5 thread read id=1 READ_COMMITTED
            5 thread write id=1 transaction repeatable_read
            expected: error while writing
         */

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);

            PlatformTransactionManager ptmRC = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRC = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo").lookupGroups("xap-15.8.0"))
                    .transactionManager(ptmRC)
                    .defaultReadModifiers(ReadModifiers.READ_COMMITTED).create();

            PlatformTransactionManager ptmRR = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRR = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo").lookupGroups("xap-15.8.0"))
                    .transactionManager(ptmRR)
                    .defaultReadModifiers(ReadModifiers.REPEATABLE_READ).create();


            // READ threads
            for (int i = 0; i < noOfThreads; i++) {
                if (i % 2 == 0) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Person template = new Person();
                                template.setId(1L);

                                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                                //configure the transaction definition
                                definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
                                TransactionStatus status = ptmRC.getTransaction(definition);
                                try { //do things with the GigaSpace instance...
                                    Person p1 = gigaSpaceRC.read(template);
                                    System.out.println("Read - " + p1);
                                } catch (Exception e) {
                                    System.out.printf("Start Error during the read");
                                    e.printStackTrace();
                                    System.out.printf("End Error during the read");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Person p = new Person(1l, "firtName", "lastName", 50);
                                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                                //configure the transaction definition
                                definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
                                TransactionStatus status = ptmRR.getTransaction(definition);
                                try { //do things with the GigaSpace instance...
                                    gigaSpaceRR.write(p);
                                    System.out.println("Trying to Write - " + p);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    ptmRR.commit(status);
                                    System.out.println("Write committed");
                                } catch (Exception e) {
                                    ptmRR.rollback(status);
                                    System.out.println("Start error from write");
                                    e.printStackTrace();
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

            }
            System.out.println("before shutdown");
            executorService.shutdown();
            try {
                executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("after shutdown");

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End runConcurrentReadAndWriteWithSleep");
        return "SUCCESS";
    }

    @GetMapping("/runMultipleWriteTransaction")
    public String runMultipleWriteTransaction() {
        int noOfObject = 1000;
        System.out.println("Start runMultipleWriteTransaction");

        try {

            PlatformTransactionManager ptmRR = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRR = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo").lookupGroups("xap-15.8.0"))
                    .transactionManager(ptmRR)
                    .defaultReadModifiers(ReadModifiers.REPEATABLE_READ).create();


            List<Person> personList = new ArrayList<Person>();
            // READ threads
            for (long i = 0; i < noOfObject; i++) {
                Person p = new Person(i, "firtName", "lastName", 50);
                personList.add(p);
            }
            try {
                DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
                //configure the transaction definition
                definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
                TransactionStatus status = ptmRR.getTransaction(definition);
                try { //do things with the GigaSpace instance...
                    gigaSpaceRR.writeMultiple(personList.toArray());
                    System.out.println("Trying to Write Multiple - ");
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ptmRR.commit(status);
                    System.out.println("WriteMultiple committed");
                } catch (Exception e) {
                    ptmRR.rollback(status);
                    System.out.println("Start error from write multiple");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("End runMultipleWriteTransaction");
        return "SUCCESS";
    }

    @GetMapping("/repeatableRead")
    public String repeatableRead() {

        return "Success";
    }

    @GetMapping("/dirtyRead")
    public String dirtyRead() {

        return "Success";
    }

    @GetMapping("/readCommitted")
    public String readCommitted() {

        return "Success";
    }

    @GetMapping("/exclusiveReadLock")
    public String exclusiveReadLock() {

        return "Success";
    }


}
