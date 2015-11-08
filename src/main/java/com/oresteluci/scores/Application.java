package com.oresteluci.scores;

import com.oresteluci.scores.handler.HandlerDispatcher;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.oresteluci.scores.config.ApplicationConfig.HTTP_EXECUTOR_DEFAULT_FIXED_THREAD_POOL_SIZE;
import static com.oresteluci.scores.config.ApplicationConfig.SERVER_DEFAULT_PORT;

/**
 * @author Oreste Luci
 */
public class Application {

    private static final Logger log = Logger.getLogger(Application.class.getName());

    private static final String RUN_PARAMS_PORT_KEY = "port";
    private static final String RUN_PARAMS_EXECUTOR_KEY = "executor";
    private static final String RUN_PARAMS_EXECUTOR_FIXED_SIZE_KEY = "fixed_size";
    private static final String RUN_PARAMS_EXECUTOR_FIXED_TYPE_VALUE = "fixed";
    private static final String RUN_PARAMS_EXECUTOR_CACHED_TYPE_VALUE = "cached";

    /**
     * Contains startup parameters
     */
    Map<String,String> runParameters = new HashMap<>();

    /**
     * Everything starts here. Creates the application and starts the server.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        Application app = new Application();
        app.displayBanner();
        app.loadParameters(args);
        app.runServer(app.getPort(), app.getExecutorService());
    }

    /**
     * Starts the server in the specified port with the given ExecutorService.
     * @param port
     * @param executorService
     * @throws IOException
     */
    public void runServer(int port, ExecutorService executorService) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new HandlerDispatcher());
        server.setExecutor(executorService);
        server.start();

        log.info("Server us UP");
    }

    /**
     * Determines the server port by reading the startup parameters.
     * By default it uses the @see com.oresteluci.scores.config.ApplicationConfig.SERVER_DEFAULT_PORT
     * @return
     * @throws Exception
     */
    private int getPort() throws Exception {

        int port = SERVER_DEFAULT_PORT;

        String p = runParameters.get(Application.RUN_PARAMS_PORT_KEY);

        if (p != null) {
            port = Integer.parseInt(p);
        }

        log.info("Using port: " + port);

        return port;
    }

    /**
     * Determines which ExecutorService to use by reading startup parameters.
     * By default is uses the newCachedThreadPool. If newFixedThreadPool is selected with no pool size, then
     * the default pool size will be obtained from @see com.oresteluci.scores.config.ApplicationConfig.HTTP_EXECUTOR_DEFAULT_FIXED_THREAD_POOL_SIZE
     * @return
     * @throws Exception
     */
    private ExecutorService getExecutorService() throws Exception {

        String executorType = runParameters.get(Application.RUN_PARAMS_EXECUTOR_KEY);

        if (executorType == null) {
            executorType = Application.RUN_PARAMS_EXECUTOR_CACHED_TYPE_VALUE;
        }

        if (executorType.equalsIgnoreCase(Application.RUN_PARAMS_EXECUTOR_CACHED_TYPE_VALUE)) {

            log.info("Using newCachedThreadPool Executor");

            return Executors.newCachedThreadPool();

        } else {

            int poolSize = HTTP_EXECUTOR_DEFAULT_FIXED_THREAD_POOL_SIZE;

            if (runParameters.get(Application.RUN_PARAMS_EXECUTOR_FIXED_SIZE_KEY) != null) {
                poolSize = Integer.parseInt(runParameters.get(Application.RUN_PARAMS_EXECUTOR_FIXED_SIZE_KEY));
            }

            log.info("Using newFixedThreadPool Executor with pool size: " + poolSize);

            return Executors.newFixedThreadPool(poolSize);
        }
    }

    /**
     * Creates a parameter map from arguments passed to the program.
     * @param args
     */
    private void loadParameters(String[] args){

        if (args != null && args.length>0) {

            for (String arg : args) {

                String[] param = arg.split("=");

                if (param.length == 2) {

                    if ("-port".equalsIgnoreCase(param[0])) {

                        runParameters.put(Application.RUN_PARAMS_PORT_KEY, param[1]);

                    } else if ("-executor".equalsIgnoreCase(param[0])) {

                        if ("fixed".equalsIgnoreCase(param[1])) {

                            runParameters.put(Application.RUN_PARAMS_EXECUTOR_KEY, Application.RUN_PARAMS_EXECUTOR_FIXED_TYPE_VALUE);

                        } else {

                            runParameters.put(Application.RUN_PARAMS_EXECUTOR_KEY, Application.RUN_PARAMS_EXECUTOR_CACHED_TYPE_VALUE);
                        }

                    } else if ("-poolSize".equalsIgnoreCase(param[0])) {

                        runParameters.put(Application.RUN_PARAMS_EXECUTOR_FIXED_SIZE_KEY,param[1]);
                    }
                }
            }
        }
    }

    /**
     * Prints startup banner in console.
     */
    private void displayBanner() {
        System.out.println("\n***************************");
        System.out.println("*                         *");
        System.out.println("*     Starting Server     *");
        System.out.println("*                         *");
        System.out.println("***************************");
    }
}
