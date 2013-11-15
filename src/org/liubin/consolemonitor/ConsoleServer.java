package org.liubin.consolemonitor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

public class ConsoleServer {

    private static Logger logger = LogManager.getLogger(ConsoleServer.class
            .getName());

    // used for future operation,close,etc.
    private IoAcceptor acceptor;

    private ConsoleResponder consoleResponder;

    // config file path
    private String configFile;

    // server listen port
    private int listenPort;

    // server listen ip,can be 0.0.0.0 to listen all interfaces.
    private String listenIp;

    // ip list that can connect this server
    private static String[] allowIp;

    // user auth key. no need of username and password
    private static String accessKey;

    // if not need auth, you should use allow ip list to make you server secure.
    public static boolean needAuth;

    public ConsoleResponder getConsoleResponder() {
        return consoleResponder;
    }

    public ConsoleServer(ConsoleResponder consoleResponder, String configFile)
            throws ConfigurationException {
        this.consoleResponder = consoleResponder;
        this.configFile = configFile;
        readConfig();
    }

    private void readConfig() throws ConfigurationException {
        if (configFile == null)
            configFile = "service.properties";

        Configuration config = new PropertiesConfiguration(configFile);

        // server listen port.
        this.listenPort = config.getInt("console_monitor.listen_port", 8888);
        this.listenIp = config.getString("console_monitor.listen_ip");
        allowIp = config.getString("console_monitor.allow_ip").split(":");
        accessKey = config.getString("console_monitor.access_key");
        needAuth = config.getBoolean("console_monitor.need_auth", true);
    }

    /**
     * start listen on host:port
     * @param host
     * @param port
     * @throws Exception 
     */
    public void startServer() throws Exception {

        acceptor = new NioSocketAcceptor();

        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast(
                "codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset
                        .forName("UTF-8"))));

        acceptor.setHandler(new ConsoleServerHandler(this.consoleResponder));

        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);

        logger.debug("listen to " + this.listenIp + ":" + this.listenPort);
        boolean bind = false;
        while (!bind)
            try {
                acceptor.bind(new InetSocketAddress(this.listenIp,
                        this.listenPort));
                bind = true;
            } catch (Exception e) {
                throw new Exception("bind error");
            }
    }

    /**
     * shutdown the server.
     */
    public void shutdown() {
        if (acceptor != null) {
            logger.debug("begin shutdown ... ");

            //            for (IoSession ss : acceptor.getManagedSessions().values()) {
            //                ss.write("server is stopping, bye");
            //
            //                System.out.println("okokok 333");
            //
            //                ss.close(false).awaitUninterruptibly();
            //                System.out.println("okokok 444");
            //
            //                //ss.getService().dispose();
            //            }
            logger.debug("begin unbind and dispose");
            // TODO : how to close a server acceptor?
            acceptor.unbind();
            acceptor.dispose();
            logger.debug("complete unbind and dispose");

        }
    }

    /**
     * to see if the given ip is in the allowed ip list.
     * @param remoteAddress
     * @return
     */
    public static boolean isAllowedIp(InetSocketAddress remoteAddress) {
        String ip = remoteAddress.getAddress().getHostAddress();

        for (String s : allowIp) {

            Pattern pattern = Pattern.compile(s);

            Matcher matcher = pattern.matcher(ip);
            if (matcher.find()) {
                return true;
            }

        }
        logger.debug("not a valid ip:" + ip);

        return false;
    }

    public static boolean auth(String key) {
        return key != null && accessKey != null && key.equals(accessKey);
    }

}

/**
 * handler used to interact with client.
 * @author liubin
 *
 */
class ConsoleServerHandler implements IoHandler {

    private static Logger logger = LogManager
            .getLogger(ConsoleServerHandler.class.getName());

    private ConsoleResponder consoleResponder;

    public ConsoleServerHandler(ConsoleResponder consoleResponder) {
        this.consoleResponder = consoleResponder;
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable e)
            throws Exception {
        logger.warn("exception caught", e);
        session.close(false);
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {

        String msg = (String) message;

        if (ConsoleServer.needAuth) {
            Boolean login = (Boolean) session.getAttribute("login");

            if (login != null && login.booleanValue()) {
                consoleResponder.action(msg, session);
            } else if (msg.startsWith("AUTH:")) {
                String key = msg.split(":")[1];
                if (!ConsoleServer.auth(key)) {
                    logger.debug("user auth error ");
                    session.write("today is too late,see you tomorrow.");
                    session.close(false);
                } else {
                    logger.debug("user auth ok ");
                    session.setAttribute("login", true);
                }
            } else {
                logger.debug("not a authed user and not a auth command ");

                session.write("today is too late,see you tomorrow.");
                session.close(false);

            }
        } else {
            consoleResponder.action(msg, session);
        }
    }

    @Override
    public void messageSent(IoSession session, Object arg1) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.info("sessionClosed: " + session.toString());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        logger.info("sessionCreated: " + session.toString());

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus arg1)
            throws Exception {
        logger.info("sessionIdle: " + session.toString());
        this.consoleResponder.onSessionIdle(session, arg1);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.info("sessionOpened: " + session.toString());

        // if ip is not allowed.
        if (!ConsoleServer.isAllowedIp((InetSocketAddress) session
                .getRemoteAddress())) {
            session.write("today is too late,see you tomorrow.");
            session.close(false);

            logger.debug("not allowed ip ,close");

            return;
        }
        this.consoleResponder.onSessionOpened(session);

    }

}