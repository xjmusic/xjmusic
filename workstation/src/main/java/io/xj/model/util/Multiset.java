package io.xj.model.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 A class representing a Mutable Multiset
 <p>
 Thanks to https://www.techiedelight.com/multiset-implementation-java/

 @param <E> The type of elements in this multiset */
public class Multiset<E> {
  /* List to store distinct values */
  private final List<E> values;

  /* List to store counts of distinct values */
  private final List<Integer> frequency;

  private final String ERROR_MSG = "Count cannot be negative: ";

  /* Constructor */
  public Multiset() {
    values = new ArrayList<>();
    frequency = new ArrayList<>();
  }

  /**
   Adds an element to this multiset specified number of times

   @param element The element to be added
   @param count   The number of times
   */
  public void add(E element, int count) {
    if (count < 0) {
      throw new IllegalArgumentException(ERROR_MSG + count);
    }

    int index = values.indexOf(element);
    int prevCount;

    if (index != -1) {
      prevCount = frequency.get(index);
      frequency.set(index, prevCount + count);
    } else if (count != 0) {
      values.add(element);
      frequency.add(count);
    }
  }

  /**
   Adds specified element to this multiset

   @param element The element to be added
   */
  public void add(E element) {
    add(element, 1);
  }

  /**
   Adds all elements in the specified collection to this multiset

   @param c Collection containing elements to be added
   */
  public void addAll(Collection<? extends E> c) {
    for (E element : c) {
      add(element, 1);
    }
  }

  /**
   Adds all elements in the specified array to this multiset

   @param arr An array containing elements to be added
   */
  @SafeVarargs
  public final void addAll(E... arr) {
    for (E element : arr) {
      add(element, 1);
    }
  }

  /**
   Performs the given action for each element of the Iterable,
   including duplicates

   @param action The action to be performed for each element
   */
  public void forEach(Consumer<? super E> action) {
    List<E> all = new ArrayList<>();

    for (int i = 0; i < values.size(); i++) {
      for (int j = 0; j < frequency.get(i); j++) {
        all.add(values.get(i));
      }
      all.forEach(action);
    }
  }

  /**
   Removes a single occurrence of the specified element from this multiset

   @param element The element to removed
   */
  public void remove(E element) {
    remove(element, 1);
  }

  /**
   Removes a specified number of occurrences of the specified element
   from this multiset

   @param element The element to removed
   @param count   The number of occurrences to be removed
   */
  public void remove(E element, int count) {
    if (count < 0) {
      throw new IllegalArgumentException(ERROR_MSG + count);
    }

    int index = values.indexOf(element);
    if (index == -1) {
      return;
    }

    int prevCount = frequency.get(index);

    if (prevCount > count) {
      frequency.set(index, prevCount - count);
    } else {
      values.remove(index);
      frequency.remove(index);
    }
  }

  /**
   Check if this multiset contains at least one occurrence of the
   specified element

   @param element The element to be checked
   @return true if this multiset contains at least one occurrence
   of the element
   */
  public boolean contains(E element) {
    return values.contains(element);
  }

  /**
   Check if this multiset contains at least one occurrence of each element
   in the specified collection

   @param c The collection of elements to be checked
   @return true if this multiset contains at least one occurrence
   of each element
   */
  public boolean containsAll(Collection<E> c) {
    return new HashSet<>(values).containsAll(c);
  }

  /**
   Find the frequency of an element in this multiset

   @param element The element to be counted
   @return The frequency of the element
   */
  public int count(E element) {
    int index = values.indexOf(element);

    return (index == -1) ? 0 : frequency.get(index);
  }

  /**
   @return A view of the set of distinct elements in this multiset
   */
  public Set<E> elementSet() {
    return new HashSet<>(values);
  }

  /**
   @return true if this multiset is empty
   */
  public boolean isEmpty() {
    return values.size() == 0;
  }

  /**
   @return Total number of elements in this multiset, including duplicates
   */
  public int size() {
    int size = 0;
    for (Integer i : frequency) {
      size += i;
    }
    return size;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[");
    for (int i = 0; i < values.size(); i++) {
      sb.append(values.get(i));

      if (frequency.get(i) > 1) {
        sb.append(" x ").append(frequency.get(i));
      }

      if (i != values.size() - 1) {
        sb.append(", ");
      }
    }

    return sb.append("]").toString();
  }
}
