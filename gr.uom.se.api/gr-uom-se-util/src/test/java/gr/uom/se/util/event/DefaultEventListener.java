package gr.uom.se.util.event;

import java.util.ArrayList;

public class DefaultEventListener implements EventListener {

   ArrayList<Event> recievedEvents = new ArrayList<>();
   
   @Override
   public void newEvent(Event event) {
      recievedEvents.add(event);
   }

}
