/**
 * 
 */
package gr.uom.se.util.event;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultEventQueue implements EventQueue {

   private ConcurrentHashMap<EventType, LinkedList<EventListener>> eventTypeListeners;
   private ReentrantReadWriteLock listenersTypeLock = new ReentrantReadWriteLock();

   private LinkedList<EventListener> eventListeners;
   private ReentrantReadWriteLock listenersLock = new ReentrantReadWriteLock();

   public DefaultEventQueue(){
      eventTypeListeners = new ConcurrentHashMap<>();
      eventListeners = new LinkedList<>();
   }
   
/** {@inheritDoc)
    * @see gr.uom.se.util.event.EventQueue#trigger(gr.uom.se.util.event.Event)
    */
   @Override
   public void trigger(Event event) {
      ArgsCheck.notNull("event", event);
      ArgsCheck.notNull("eventType", event.getType());
      listenersTypeLock.readLock().lock();
      try{
         LinkedList<EventListener> listeners = eventTypeListeners.get(event.getType());
         if(listeners != null) {
            for(EventListener listener : listeners) {
               listener.newEvent(event);
            }
         }
      }
      finally{
         listenersTypeLock.readLock().unlock();
      }
      triggerGlobalListeners(event);
   }
   
   private void triggerGlobalListeners(Event event){
      listenersLock.readLock().lock();
      try{
         for(EventListener li : eventListeners) {
            li.newEvent(event);
         }
      }
      finally{
         listenersLock.readLock().unlock();
      }
   }

/** {@inheritDoc)
    * @see gr.uom.se.util.event.EventQueue#addListerner(gr.uom.se.util.event.EventType, gr.uom.se.util.event.EventListener)
    */
   @Override
   public void addListener(EventType type, EventListener listener) {
      ArgsCheck.notNull("type", type);
      ArgsCheck.notNull("listener", listener);
      listenersTypeLock.writeLock().lock();
      try {
         LinkedList<EventListener> listeners = eventTypeListeners.get(type);
         if (listeners == null) {
            listeners = new LinkedList<>();
            eventTypeListeners.put(type, listeners);
         }
         if (!listeners.contains(listener)) {
            listeners.add(listener);
         }
      } finally {
         listenersTypeLock.writeLock().unlock();
      }

   }

/** {@inheritDoc)
    * @see gr.uom.se.util.event.EventQueue#removeListener(gr.uom.se.util.event.EventType, gr.uom.se.util.event.EventListener)
    */
   @Override
   public void removeListener(EventType type, EventListener listener) {
      ArgsCheck.notNull("type", type);
      ArgsCheck.notNull("listener", listener);
      listenersTypeLock.writeLock().lock();
      try {
         LinkedList<EventListener> listeners = eventTypeListeners.get(type);
         if (listeners != null) {
            listeners.remove(listener);
         }
      } finally {
         listenersTypeLock.writeLock().unlock();
      }
   }

/** {@inheritDoc)
    * @see gr.uom.se.util.event.EventQueue#addListener(gr.uom.se.util.event.EventListener)
    */
   @Override
   public void addListener(EventListener listener) {
      ArgsCheck.notNull("listener", listener);
      listenersLock.writeLock().lock();

      try {
         if (!eventListeners.contains(listener)) {

            listenersTypeLock.writeLock().lock();
            try {
               // Remove the listener from any type event
               // if it is present
               Iterator<EventType> it = eventTypeListeners.keySet().iterator();
               while (it.hasNext()) {
                  LinkedList<EventListener> list = eventTypeListeners.get(it
                        .next());
                  if (list != null) {
                     list.remove(listener);
                  }
               }
            } finally {
               listenersTypeLock.writeLock().unlock();
            }

            // Add to global event listeners
            if (!eventListeners.contains(listener)) {
               eventListeners.add(listener);
            }
         }
      } finally {

         listenersLock.writeLock().unlock();
      }
   }

/** {@inheritDoc)
    * @see gr.uom.se.util.event.EventQueue#removeListener(gr.uom.se.util.event.EventListener)
    */
   @Override
   public void removeListener(EventListener listener) {
      ArgsCheck.notNull("listener", listener);
      listenersLock.writeLock().lock();
      try {
         eventListeners.remove(listener);
      } finally {
         listenersLock.writeLock().unlock();
      }
   }

}
