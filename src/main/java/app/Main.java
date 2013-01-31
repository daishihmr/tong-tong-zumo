package app;

import jp.dev7.ourbeats.MainServer;

public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8080;
        for (int i = 0; i < args.length; i++) {
            // port
            if (args[i].equals("--port") || args[i].equals("-p")) {
                port = Integer.parseInt(args[i + 1]);
            }
        }

        MainServer.run(port);
    }
}
