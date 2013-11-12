package org.liubin.consolemonitor;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

/**
 * interface that client's code should implements to communicate with server.
 * @author liubin
 *
 */
public interface ConsoleResponder {

    // respond to a client command
    public void action(String command, IoSession session);

    // action that when session idle ,for example auto push message to client.
    public void onSessionIdle(IoSession session, IdleStatus arg1);

    // how to do at session open event, for example :auto push message to client.
    public void onSessionOpened(IoSession session);
}
