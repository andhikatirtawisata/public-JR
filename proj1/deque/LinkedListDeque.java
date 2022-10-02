package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    /** Creating Class */
    private class ListNode<T> {
        /** List Node Item*/
        private final T item;
        /** Next ListNode */
        private ListNode next;
        /** Prev ListNode */
        private ListNode prev;

        /** Create one ListNode */
        private ListNode(ListNode p, T i, ListNode n) {
            item = i;
            next = n;
            prev = p;
        }
    }

    /** Size of List */
    private int size;

    /** Sentinel Node*/
    private final ListNode sentinel;

    /** Create a new Deque */
    public LinkedListDeque() {
        sentinel = new ListNode(null, null, null);
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    /** Add to the beginning (after sentinel */
    public void addFirst(T item) {
        ListNode temp = sentinel.next;
        sentinel.next = new ListNode<>(sentinel, item, temp);
        sentinel.next.next.prev = sentinel.next;
        size += 1;
    }

    /** Add Last */
    public void addLast(T item) {
        ListNode temp = sentinel.prev;
        sentinel.prev = new ListNode<>(temp, item, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        size += 1;
    }

    /** Returns size of deque */
    public int size() {
        return size;
    }

    /** Print all items in Deque */
    public void printDeque() {
        ListNode pointer = sentinel;
        while (pointer.next != sentinel) {
            System.out.println(pointer.next);
            pointer = pointer.next;
        }
    }

    /** Remove node after sentinel */
    public T removeFirst() {
        T temp = (T) sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        if (size > 0) {
            size -= 1;
        }
        return temp;
    }

    /** Remove node before sentinel */
    public T removeLast() {
        T temp = (T) sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        if (size > 0) {
            size -= 1;
        }
        return temp;
    }

    /** Get index item */
    public T get(int i) {
        int count = 0;
        ListNode pointer = sentinel.next;
        while (count < i) {
            pointer = pointer.next;
            count += 1;
        }
        return (T) pointer.item;
    }

    /** Recursive get */
    public T getRecursive(int i) {
        if (i >= this.size) {
            return null;
        }
        ListNode pointer = sentinel.next;
        return getRecursiveHelper(pointer, i);
    }

    /** Helper to use recursive get */
    private T getRecursiveHelper(ListNode n, int i) {
        if (i == 0) {
            return (T) n.item;
        }
        return (T) getRecursiveHelper(n.next, i - 1);
    }

    /** private T getRecursive(Node, p, int index) {
     * if (index == 0) {
     *     return p.item;
     * }
     * return getRecursive(
     }
     *
     * */

    public boolean equals(Object o) {
        if (!(o instanceof Deque<?>)) {
            return false;
        }
        if (((Deque<?>) o).size() != size()) {
            return false;
        }
        for (int i = 0; i < size(); i += 1) {
            if (!(get(i).equals(((Deque<?>) o).get(i)))) {
                return false;
            }
        }
        return true;
    }

    /** iterator */
    @Override
    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    /** Iterator class */
    private class LinkedListIterator implements Iterator<T> {
        /** current node */
        private ListNode currentPos;
        /** count */
        private int count;
        /** create new instance */
        public LinkedListIterator() {
            currentPos = sentinel.next;
            count = 0;
        }
        /** check if there is a next */
        public boolean hasNext() {
            return count < size;
        }
        /** iterate to next item */
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T returnItem = (T) currentPos.item;
            currentPos = currentPos.next;
            count += 1;
            return returnItem;
        }
    }



}
