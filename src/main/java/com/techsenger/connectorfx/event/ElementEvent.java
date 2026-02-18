package com.techsenger.connectorfx.event;

import com.techsenger.connectorfx.scenegraph.Element;

/**
 * Signifies that the event has been triggered by an element and provides access to that element.
 */
public interface ElementEvent {

    Element getElement();
}
