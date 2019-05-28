package com.muc;

/**
 * provides the abstraction of methods necessary for listening for connecting users
 */
public interface UserStatusListener {
    public void online(String login);
    public void offline(String login);
}
