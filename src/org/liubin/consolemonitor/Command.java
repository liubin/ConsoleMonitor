package org.liubin.consolemonitor;

import org.apache.mina.core.session.IoSession;

@Deprecated
public abstract class Command {

    private String command;
    private CommandResponder commandResponder;

    public Command(String command, CommandResponder commandResponder) {
        this.command = command;
        this.commandResponder = commandResponder;
    }

    public void action(IoSession session) {
        this.commandResponder.action(this.command, session);
    }

    @Override
    public String toString() {
        return "COMMAND : " + this.command + ";COMMANDRESPONDER:"
                + this.commandResponder.toString();
    }
}
