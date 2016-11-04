/**************************************************
 * Android Web Server
 * Based on JavaLittleWebServer (2008)
 * <p/>
 * Copyright (c) Piotr Polak 2008-2016
 **************************************************/

package ro.polak.webserver;

import java.io.IOException;
import java.net.Socket;

import ro.polak.webserver.controller.MainController;
import ro.polak.webserver.error.HttpError403;
import ro.polak.webserver.error.HttpError404;
import ro.polak.webserver.error.HttpError405;
import ro.polak.webserver.resource.provider.ResourceProvider;
import ro.polak.webserver.servlet.HttpRequestWrapper;
import ro.polak.webserver.servlet.HttpRequestWrapperFactory;
import ro.polak.webserver.servlet.HttpResponseWrapper;

/**
 * Server thread.
 *
 * @author Piotr Polak piotr [at] polak [dot] ro
 * @since 200802
 */
public class ServerRunnable implements Runnable {

    private Socket socket;
    private WebServer webServer;
    private static HttpRequestWrapperFactory requestFactory;

    static {
        requestFactory = new HttpRequestWrapperFactory(MainController.getInstance().getWebServer().getServerConfig().getTempPath());
    }

    /**
     * Default constructor.
     *
     * @param socket
     */
    public ServerRunnable(Socket socket, WebServer webServer) {
        this.socket = socket;
        this.webServer = webServer;
    }

    @Override
    public void run() {
        try {
            HttpRequestWrapper request = requestFactory.createFromSocket(socket);
            HttpResponseWrapper response = HttpResponseWrapper.createFromSocket(socket);
            String path = request.getRequestURI();

            if (isPathIllegal(path)) {
                (new HttpError403()).serve(response);
                return;
            }

            setDefaultResponseHeaders(request, response);

            if (isMethodSupported(request.getMethod())) {
                boolean isResourceLoaded = loadResourceByPath(request, response, path);
                if (!isResourceLoaded) {
                    isResourceLoaded = loadDirectoryIndexResource(request, response, path);
                }
                if (!isResourceLoaded) {
                    (new HttpError404()).serve(response);
                }
            } else {
                serveMethodNotAllowed(response);
            }

            socket.close();
        } catch (IOException e) {
        }
    }

    /**
     * Sets default response headers.
     *
     * @param request
     * @param response
     */
    private void setDefaultResponseHeaders(HttpRequestWrapper request, HttpResponseWrapper response) {

        boolean isKeepAlive = false;
        if (request.getHeaders().containsHeader(Headers.HEADER_CONNECTION)) {
            isKeepAlive = request.getHeaders().getHeader(Headers.HEADER_CONNECTION).toLowerCase().equals("keep-alive");
        }

        response.setKeepAlive(isKeepAlive && webServer.getServerConfig().isKeepAlive());
        response.getHeaders().setHeader(Headers.HEADER_SERVER, WebServer.SIGNATURE);
    }

    /**
     * Attempts to load resource by directory path.
     *
     * @param request
     * @param response
     * @param path
     * @return
     * @throws IOException
     */
    private boolean loadDirectoryIndexResource(HttpRequestWrapper request, HttpResponseWrapper response, String path) throws IOException {
        path = getNormalizedDirectoryPath(path);
        for (String index : webServer.getServerConfig().getDirectoryIndex()) {
            if (loadResourceByPath(request, response, path + index)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Server Method Not Allowed error page.
     *
     * @param response
     * @throws IOException
     */
    private void serveMethodNotAllowed(HttpResponseWrapper response) throws IOException {
        StringBuilder sb = new StringBuilder();
        String[] supportedMethods = webServer.getSupportedMethods();
        for (int i = 0; i < supportedMethods.length; i++) {
            sb.append(supportedMethods[i]);
            if (i != supportedMethods.length - 1) {
                sb.append(", ");
            }
        }

        response.getHeaders().setHeader(Headers.HEADER_ALLOW, sb.toString());
        (new HttpError405()).serve(response);
    }

    /**
     * Loads resource by path.
     *
     * @param request
     * @param response
     * @param path
     * @return
     * @throws IOException
     */
    private boolean loadResourceByPath(HttpRequestWrapper request, HttpResponseWrapper response, String path) throws IOException {
        ResourceProvider[] rl = webServer.getResourceProviders();
        for (int i = 0; i < rl.length; i++) {
            if (rl[i].load(path, request, response)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether the given path contains illegal expressions.
     *
     * @param path
     * @return
     */
    private boolean isPathIllegal(String path) {
        return path == null || path.startsWith("../") || path.indexOf("/../") != -1;
    }

    /**
     * Makes sure the last character is a slash.
     *
     * @param path
     * @return
     */
    private String getNormalizedDirectoryPath(String path) {
        if (path.length() > 0) {
            if (path.charAt(path.length() - 1) != '/') {
                path += "/";
            }
        }
        return path;
    }

    /**
     * Tells whether the given HTTP method is supported.
     *
     * @param method
     * @return
     */
    private boolean isMethodSupported(String method) {
        for (String aMethod : webServer.getSupportedMethods()) {
            if (aMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns associated socket.
     *
     * @return
     */
    protected Socket getSocket() {
        return socket;
    }
}