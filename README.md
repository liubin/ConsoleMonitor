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
```java
class Test implements ConsoleResponder{

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
