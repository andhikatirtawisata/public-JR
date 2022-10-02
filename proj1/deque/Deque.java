package deque;

public interface Deque<T> {
    /** addFirst */
    public void addFirst(T item);
    /** addLast */
    public void addLast(T item);
    /** check if deque is empty */
    default boolean isEmpty() {
        return size() == 0;
    }
    /** return size of deque */
    public int size();
    /** printDeque */
    public void printDeque();
    /** remove item from front */
    public T removeFirst();
    /** remove item from back */
    public T removeLast();
    /** get item at index */
    public T get(int index);
    /** check if dequeue are equal */
    public boolean equals(Object o);
}
