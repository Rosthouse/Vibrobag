/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rosthouse.vibrobag;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.support.v4.content.LocalBroadcastManager;
import android.view.accessibility.AccessibilityEvent;

/**
 * This class listens to all notifications that Android sends
 *
 * @author Patrick
 */
public class NotificationListener extends AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected(); //To change body of generated methods, choose Tools | Templates.
        AccessibilityServiceInfo info = this.getServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_HAPTIC;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        final int eventType = event.getEventType();
        String eventText = null;
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Notification note = (Notification) event.getParcelableData();

        }
        eventText = eventText + event.getContentDescription();
        LocalBroadcastManager.getInstance(this);

        // Do something nifty with this text, like speak the composed string
        // back to the user.
//    speakToUser(eventText);
    }

    @Override
    public void onInterrupt() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
