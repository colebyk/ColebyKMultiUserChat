package com.muc;

/**
 * provides the abstraction of methods needed for listening for messages
 */
public interface MessageListener {
    public void onMessage(String fromLogin, String msgBody);
}
