package jp.dev7.ourbeats;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.dev7.ourbeats.servlet.MainServlet;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class MainServer {

    public MainServer() {
    }

    public void run(int port) throws Exception {
        HandlerList handlerList = new HandlerList();

        ResourceHandler resourceHandler = new ResourceHandler() {
            @Override
            public void handle(String target, Request baseRequest,
                    HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                super.handle(target, baseRequest, request, response);
                response.addHeader("Cache-Control", "no-cache");
                response.addHeader("Pragma", "no-cache");
                response.addHeader("Expires", "Thu, 01 Dec 1994 16:00:00 GMT");
            }
        };
        resourceHandler.setResourceBase("./web");
        handlerList.addHandler(resourceHandler);

        MainServlet waitingServlet = new MainServlet();
        ServletHandler servletHandler = new ServletHandler();
        servletHandler.addServletWithMapping(new ServletHolder(waitingServlet),
                "/ws/init");
        handlerList.addHandler(servletHandler);

        Server server = new Server(port);
        server.setHandler(handlerList);

        server.start();
    }

}
