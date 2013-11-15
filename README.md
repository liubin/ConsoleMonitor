ConsoleMonitor
==============

Java library provide a infterface monitor and control with application

## 1. features
you can integrate this library into your Java applications to communite with outside through telnet protocol like a web based admin interface, for example:

 - stop the application
 - get the application's running status
 - change configrurations dynamically
 - and other text-based communications


## 2. use example

please see Test.java in the test folder.

```java
class Test implements ConsoleResponder{

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
    public void onSessionIdle(IoSession session, IdleStatus arg1) {
    }

    @Override
    public void onSessionOpened(IoSession session) {
        session.write(this.showHelpMsg());
    }

    @Override
    public void action(String command, IoSession session) {
        if (command.equals("quit")) {
            session.write("Bye");
            session.close(false);
        } else if (command.equals("status")) {
            session.write(this.getStatus());
        } else if (command.equals("stop")) {
            stop();
        } else {
            // ...
        }
    }
}
```
## 3. config file(ACL)

```
# listen ip address
console_monitor.listen_ip=0.0.0.0

# listen port
console_monitor.listen_port=6666

# which ip can access this server,seprated by colon ":"
console_monitor.allow_ip=127.0.0.1:192.168.1.*

# set it to false to use only ip based ACL
# if set to true, user must do auth by input AUTH:XXXX
console_monitor.need_auth=false

# user secret.
console_monitor.access_key=XXXXX

```

## 4. user auth

please see how to config user secret upon.

and when you telent to server:6666 ,you will must do auth by input:

  AUTH:XXXX

where AUTH must be in upper case and XXXX is what you set in console_monitor.access_key
