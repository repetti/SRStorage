package org.test;

import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.naming.resources.VirtualDirContext;

import java.io.File;

/**
 * See: https://stackoverflow.com/questions/11669507/embedded-tomcat-7-servlet-3-0-annotations-not-working
 *
 * Https:
 * https://stackoverflow.com/questions/11824148/embed-tomcat-7-to-run-only-in-https
 * https://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html
 *
 *
 * @author repetti
 */
public class TomcatServer2 {
//    private static final String keyBase = "./";
    private static final String appBase = ""; // "/"
    private static final int port = 9080;
    private static final int httpsPort = 9443;
    private static final String MODULE = "mars"; // '.' for no module
    private static final String WEBAPP = "/src/main/webapp";
    private static final String CLASSES = "/build/classes/main";

    public static void main(String[] args) throws Exception {
//        BasicConfigurator.configure();
//        Logger.getRootLogger().setLevel(Level.INFO);

        /* to use only https */
//        Connector defaultConnector = tomcat.getConnector();

        /* https */
        Connector httpsConnector = new Connector();
        httpsConnector.setPort(httpsPort);
        httpsConnector.setSecure(true);
        httpsConnector.setScheme("https");
        // System.getProperty("user.home")+"/.keystore";
        httpsConnector.setAttribute("keystoreFile", System.getProperty("user.dir") + "/.keystore");
        httpsConnector.setAttribute("keystorePass", "changeit");
        httpsConnector.setAttribute("clientAuth", "false");
        httpsConnector.setAttribute("sslProtocol", "TLS");
        httpsConnector.setAttribute("SSLEnabled", true);


        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);

        /* enable https */
        tomcat.getService().addConnector(httpsConnector);
        Connector defaultConnector = tomcat.getConnector();
        defaultConnector.setRedirectPort(8443);

        File f = new File(MODULE + WEBAPP);
        System.err.println(fileInfo(f));
        StandardContext ctx = (StandardContext) tomcat.addWebapp(appBase, f.getAbsolutePath());

        /* to set custom web.xml */
//        ctx.getServletContext().setAttribute(Globals.ALT_DD_ATTR, "/path/to/custom/web.xml");

        /* declare an alternate location for your "WEB-INF/classes" dir: */
        File additionWebInfClasses = new File(MODULE + CLASSES);
        System.err.println(fileInfo(f));
        VirtualDirContext resources = new VirtualDirContext();
        resources.setExtraResourcePaths("/WEB-INF/classes=" + additionWebInfClasses);
        /*
         * In Tomcat 8:
         * WebResourceRoot resources = new StandardRoot(ctx);
         * resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
         */
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();

    }

    private static String fileInfo(File f) {
        return "resource: '" + f.getAbsolutePath() + (f.exists() ? "' exists" : "' not found");
    }
}
