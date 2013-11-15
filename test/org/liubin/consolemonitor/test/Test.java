package org.liubin.consolemonitor.test;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.liubin.consolemonitor.ConsoleResponder;
import org.liubin.consolemonitor.ConsoleServer;

public class Test implements ConsoleResponder {

    private ConsoleServer consoleServer;

    public static void main(String[] args) {
        Test test = new Test();

        test.startConsoleMonitor();

    }

    /**
     * start console server.
     */
    public void startConsoleMonitor() {
        try {
            this.consoleServer = new ConsoleServer(this,"console-monitor.conf");

            this.consoleServer.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void action(String command, IoSession session) {
        if ("time".equals(command)) {
            session.write(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new Date()));
        } else if ("bye".equals(command)) {
            session.write("see you later");
            session.close(false);
        } else if ("stop".equals(command)) {
            this.stop();
        } else {
            session.write("command list:\n" + "  time show now time.\n"
                    + "  bye quit.");
        }
    }

    private void stop() {
        this.consoleServer.shutdown();
    }

    @Override
    public void onSessionIdle(IoSession session, IdleStatus arg1) {

    }

    @Override
    public void onSessionOpened(IoSession session) {

    }

}
