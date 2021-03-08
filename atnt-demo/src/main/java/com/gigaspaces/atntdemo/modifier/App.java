package com.gigaspaces.atntdemo.modifier;

import com.gigaspaces.atntdemo.model.Person;
import com.gigaspaces.atntdemo.write.Program;
import com.gigaspaces.client.ReadModifiers;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.UnknownTransactionException;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.SpaceProxyConfigurer;
import org.openspaces.core.transaction.manager.DistributedJiniTxManagerConfigurer;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {

    public static void main(String[] args) {
        //runScenario1();
        runConcurrentReadAndWrite(2);
        runConcurrentReadAndWriteWithSleep(4);
        System.exit(1);
        //runScenarioDirtyRead();
        //writeMultipleWithTransaction();
    }

    public static void runScenarioDirtyRead() {
        try {
            Person p = new Person(1l, "firtName", "lastName", 70);
            PlatformTransactionManager ptm = new DistributedJiniTxManagerConfigurer().transactionManager();
            //Transaction t = TransactionFactory.create(, 100L);
            //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
            GigaSpace gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo")).transactionManager(ptm).create(); //BillBuddy-space
            DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
            //configure the transaction definition
            definition.setPropagationBehavior(Propagation.REQUIRES_NEW.ordinal());
            TransactionStatus status = ptm.getTransaction(definition);
            try { //do things with the GigaSpace instance...
                gigaSpace.write(p);
                ptm.commit(status);
            } catch (Exception e) {
                ptm.rollback(status);
                throw e;
            }


            /*Transaction t1 = gigaSpace.getCurrentTransaction();
            System.out.println("Tx Provider: "+gigaSpace.getTxProvider());
            System.out.println("Cur Trans: "+gigaSpace.getTxProvider().getCurrentTransaction());
            System.out.println("Iso level: "+gigaSpace.getTxProvider().getCurrentTransactionIsolationLevel());
            System.out.println("Trans: "+t1);*/
            //PlatformTransactionManager ptm = gigaSpace.getTxProvider();

            //gigaSpace.getSpace().write(p,t1, Lease.FOREVER);
            //gigaSpace.write(p);
            //t1.commit();

            Transaction t2 = gigaSpace.getCurrentTransaction();
            p = new Person(1l, "firtName", "lastName", 80);
            gigaSpace.write(p);


            //DIRTY_READ
            GigaSpace gigaSpace2 = new GigaSpaceConfigurer(new SpaceProxyConfigurer("BillBuddy-space"))
                    .defaultReadModifiers(ReadModifiers.DIRTY_READ).create();

            Person template = new Person();
            template.setId(1L);
            //Person p1 = gigaSpace2.read(template,null,ReadModifiers.DIRTY_READ);
            Person p1 = gigaSpace2.read(template);
            System.out.println("Age - " + p1.getAge());


        } catch (UnknownTransactionException e) {
            e.printStackTrace();
        } catch (CannotCommitException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void runScenario1() {
        try {
            // Write to space
            GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
            Person p = new Person(1l, "firtName", "lastName", 50);
            gigaSpace.write(p);

            // uncommitted transaction
            PlatformTransactionManager ptm = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpace1 = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo")).transactionManager(ptm).create();
            p.setAge(80);
            gigaSpace1.write(p);
            //ptm.commit();

            int noOfThreads = 10;
            ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);

            for (int i = 0; i < noOfThreads; i++) {
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
                        GigaSpace gigaSpace = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo"))
                                .defaultReadModifiers(ReadModifiers.READ_COMMITTED).create();
                        Person template = new Person();
                        template.setId(1L);
                        Person p1 = gigaSpace.read(template);
                        System.out.println("ExecutorService - " + p1);
                    }
                });
            }

            executorService.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runConcurrentReadAndWrite(int noOfThreads) {
        System.out.println("Start runConcurrentReadAndWrite");
        /*
        5 thread read id=1 READ_COMMITTED
        5 thread write id=1 transaction repeatable_read
        expected: error while writing

         */

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);

            PlatformTransactionManager ptmRC = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRC = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo"))
                    .transactionManager(ptmRC)
                    .defaultReadModifiers(ReadModifiers.READ_COMMITTED).create();

            PlatformTransactionManager ptmRR = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRR = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo"))
                    .transactionManager(ptmRR)
                    .defaultReadModifiers(ReadModifiers.REPEATABLE_READ).create();


            // READ threads
            for (int i = 0; i < noOfThreads; i++) {
                if (i % 2 == 0) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
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
                                //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
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

    }

    public static void runConcurrentReadAndWriteWithSleep(int noOfThreads) {
        System.out.println("Start runConcurrentReadAndWriteWithSleep");
        /*
        5 thread read id=1 READ_COMMITTED
        5 thread write id=1 transaction repeatable_read
        expected: error while writing

         */

        try {

            ExecutorService executorService = Executors.newFixedThreadPool(noOfThreads);

            PlatformTransactionManager ptmRC = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRC = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo"))
                    .transactionManager(ptmRC)
                    .defaultReadModifiers(ReadModifiers.READ_COMMITTED).create();

            PlatformTransactionManager ptmRR = new DistributedJiniTxManagerConfigurer().transactionManager();
            GigaSpace gigaSpaceRR = new GigaSpaceConfigurer(new SpaceProxyConfigurer("demo"))
                    .transactionManager(ptmRR)
                    .defaultReadModifiers(ReadModifiers.REPEATABLE_READ).create();


            // READ threads
            for (int i = 0; i < noOfThreads; i++) {
                if (i % 2 == 0) {
                    executorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
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
                                //GigaSpace gigaSpace = Program.getOrCreateSpace("demo");
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

    }
}
