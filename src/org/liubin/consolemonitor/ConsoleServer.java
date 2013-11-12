package org.liubin.consolemonitor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

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

    public ConsoleResponder getConsoleResponder() {
        return consoleResponder;
    }

    public ConsoleServer(ConsoleResponder consoleResponder) {
        this.consoleResponder = consoleResponder;
    }

    /**
     * start listen on host:port
     * @param host
     * @param port
     * @throws Exception 
     */
    public void startServer(String host, int port) throws Exception {

        acceptor = new NioSocketAcceptor();

        acceptor.getFilterChain().addLast("logger", new LoggingFilter());
        acceptor.getFilterChain().addLast(
                "codec",
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset
                        .forName("UTF-8"))));

        acceptor.setHandler(new ConsoleServerHandler(this.consoleResponder));

        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 60);

        logger.debug("listen to " + host + ":" + port);
        try {
            acceptor.bind(new InetSocketAddress(host, port));
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
/*
            for (IoSession ss : acceptor.getManagedSessions().values()) {
                ss.write("server is stopping, bye");
                ss.close(false);
                ss.getService().dispose();
            }
            */
            // TODO : how to close a server acceptor?
            acceptor.unbind();
            acceptor.dispose();
        }
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

        consoleResponder.action(msg, session);

    }

    @Override
    public void messageSent(IoSession session, Object arg1) throws Exception {

    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        logger.warn("sessionClosed: " + session.toString());
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        logger.warn("sessionCreated: " + session.toString());

    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus arg1)
            throws Exception {
        logger.warn("sessionIdle: " + session.toString());
        this.consoleResponder.onSessionIdle(session, arg1);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        logger.warn("sessionOpened: " + session.toString());
        this.consoleResponder.onSessionOpened(session);

    }

}