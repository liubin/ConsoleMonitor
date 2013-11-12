package org.liubin.consolemonitor;

import org.apache.mina.core.session.IoSession;

@Deprecated
public interface CommandResponder {
    public void action(String command, IoSession session);

}
