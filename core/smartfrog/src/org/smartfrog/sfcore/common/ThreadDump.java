package org.smartfrog.sfcore.common;

/**
 * Utility class used to dump threads in ordered form based on the article:
 * http://www.javaspecialists.eu/archive/Issue132.html
 */
import java.io.Serializable;
import java.util.*;

public class ThreadDump implements Serializable {
  private final Map<Thread, StackTraceElement[]> traces;

  public ThreadDump() {
    traces = new TreeMap<Thread, StackTraceElement[]>(THREAD_COMP);
    traces.putAll(Thread.getAllStackTraces());
  }

  public Collection<Thread> getThreads() {
    return traces.keySet();
  }

  public Map<Thread, StackTraceElement[]> getTraces() {
    return traces;
  }

  /**
   * Compare the threads by name and id.
   */
  private static final Comparator<Thread> THREAD_COMP = new Comparator<Thread>() {
        public int compare(Thread o1, Thread o2) {
          int result = o1.getName().compareTo(o2.getName());
          if (result == 0) {
            Long id1 = o1.getId();
            Long id2 = o2.getId();
            return id1.compareTo(id2);
          }
          return result;
        }
      };
}
