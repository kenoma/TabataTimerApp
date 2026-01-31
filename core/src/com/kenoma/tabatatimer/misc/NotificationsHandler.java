package com.kenoma.tabatatimer.misc;

import java.util.List;

public interface NotificationsHandler
{
    public void prepareNotifications(List<NotificationsInstructions> instructions);

    public void cancelAllNotifications();
}