package gr.uom.se.util.event;

import static org.junit.Assert.*;

import org.junit.Test;

public class DefaultEventQueueTest {

   @Test
   public void testTrigger() {
      DefaultEventQueue eventQueue = new DefaultEventQueue();
      DefaultEventListener listenerGlobal = new DefaultEventListener();
      DefaultEventListener listener2 = new DefaultEventListener();
      DefaultEventListener listener3 = new DefaultEventListener();
      DefaultEventListener listener4 = new DefaultEventListener();

      eventQueue.addListener(EventTypeEnum.PROJECT_ADD, listener2);
      eventQueue.addListener(EventTypeEnum.PROJECT_REMOVE, listener3);
      eventQueue.addListener(EventTypeEnum.PROJECT_UPDATE, listener4);
      eventQueue.addListener(listenerGlobal);

      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_ADD,
            new EventInfo()));
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_REMOVE,
            new EventInfo()));
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_UPDATE,
            new EventInfo()));

      assertEquals(3, listenerGlobal.recievedEvents.size());
      boolean asserted = false;
      for (Event ev : listenerGlobal.recievedEvents) {
         EventType type = ev.getType();
         asserted = type.equals(EventTypeEnum.PROJECT_ADD)
               || type.equals(EventTypeEnum.PROJECT_REMOVE)
               || type.equals(EventTypeEnum.PROJECT_UPDATE);
         if(asserted) {
            break;
         }
      }
      assertTrue(asserted);
      
      assertEquals(1, listener2.recievedEvents.size());
      assertEquals(1, listener3.recievedEvents.size());
      assertEquals(1, listener4.recievedEvents.size());
      
      assertEquals(EventTypeEnum.PROJECT_ADD, listener2.recievedEvents.get(0).getType());
      assertEquals(EventTypeEnum.PROJECT_REMOVE, listener3.recievedEvents.get(0).getType());
      assertEquals(EventTypeEnum.PROJECT_UPDATE, listener4.recievedEvents.get(0).getType());
      
   }
   
   @Test
   public void testRemoveListenerEventTypeEventListener() {
      DefaultEventQueue eventQueue = new DefaultEventQueue();
      DefaultEventListener listener = new DefaultEventListener();
      DefaultEventListener listener2 = new DefaultEventListener();
      eventQueue.addListener(EventTypeEnum.PROJECT_ADD, listener);
      eventQueue.addListener(EventTypeEnum.PROJECT_UPDATE, listener2);
      
      eventQueue.removeListener(EventTypeEnum.PROJECT_UPDATE, listener2);
      
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_UPDATE, new EventInfo()));
      
      assertTrue(listener2.recievedEvents.isEmpty());
   }
   

   @Test
   public void testAddListener() {
      DefaultEventQueue eventQueue = new DefaultEventQueue();
      DefaultEventListener listener = new DefaultEventListener();
      DefaultEventListener listener2 = new DefaultEventListener();
      eventQueue.addListener(EventTypeEnum.PROJECT_ADD, listener);
      eventQueue.addListener(EventTypeEnum.PROJECT_UPDATE, listener2);
      
      eventQueue.addListener(listener2);
      
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_UPDATE, new EventInfo()));
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_ADD, new EventInfo()));
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_REMOVE, new EventInfo()));
      
      assertEquals(3, listener2.recievedEvents.size());
      
      assertEquals(1, listener.recievedEvents.size());
   }

   @Test
   public void testRemoveListenerEventListener() {
      DefaultEventQueue eventQueue = new DefaultEventQueue();
      DefaultEventListener listener = new DefaultEventListener();

      eventQueue.addListener(EventTypeEnum.PROJECT_ADD, listener);
      
      eventQueue.addListener(listener);
      
      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_UPDATE, new EventInfo()));
       
      assertEquals(1, listener.recievedEvents.size());
      
      eventQueue.removeListener(listener);

      eventQueue.trigger(new DefaultEvent(EventTypeEnum.PROJECT_UPDATE, new EventInfo()));
      
      assertEquals(1, listener.recievedEvents.size());
   }

}
