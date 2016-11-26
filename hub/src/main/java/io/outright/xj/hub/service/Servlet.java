package io.outright.xj.hub.service;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;

@javax.inject.Singleton
public class Servlet extends HttpServlet {

    @Inject
    private Logger logger;

    @Inject
    private Service service;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.info(req.getMethod() + " " + req.getRequestURI());
        resp.getWriter().write(service.get(req.getRequestURI()));
    }
}
