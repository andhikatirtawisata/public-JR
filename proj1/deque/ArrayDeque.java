package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** Creating Class */
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {

    /** Array items */
    private T[] items;
    /** number of items */
    private int size;
    /** null before first item */
    private int start;
    /** null after last item */
    private int end;
    /** initialArrayLength */
    private static final int INITIALARRAYLENGTH = 8;
    /** initial start point */
    private static final int INITIALSTART = 7;
    /** arraySizeLimit */
    private static final int LIMIT = 16;
    /** Create a new deque */
    public ArrayDeque() {
        items = (T[]) new Object[INITIALARRAYLENGTH];
        size = 0;
        start = INITIALSTART;
        end = 0;
    }

    /** add item to front */
    public void addFirst(T item) {
        if (size >= items.length - 1) {
            this.resize(items.length * 2);
        }
        if (start < 0) {
            start = items.length - 1;
        }
        items[start] = item;
        start -= 1;
        size += 1;
    }

    /** add item to last */
    public void addLast(T item) {
        if (size == items.length - 1) {
            this.resize(items.length * 2);
        }
        if (end >= items.length) {
            end = 0;
        }
        items[end] = item;
        end += 1;
        size += 1;
    }

    /** number of items */
    public int size() {
        return this.size;
    }

    /** print all items */
    public void printDeque() {
        for (int i = start; i <= end; i += 1) {
            System.out.println(this.items[i]);
        }
    }

    /** remove first item */
    public T removeFirst() {
        if (this.isEmpty()) {
            size = 0;
            return null;
        }
        if ((double) this.size / this.items.length <= 0.25 && size > LIMIT) {
            resize(items.length / 2);
        }
        if (start == items.length - 1) {
            T temp = items[0];
            items[0] = null;
            size -= 1;
            start = 0;
            return temp;
        } else {
            T temp = items[start + 1];
            items[start + 1] = null;
            size -= 1;
            start += 1;
            return temp;
        }
    }

    /** remove last item */
    public T removeLast() {
        if (this.isEmpty()) {
            size = 0;
            return null;
        }
        if ((double) this.size / this.items.length <= 0.25 && size > LIMIT) {
            resize(items.length / 2);
        }
        if (end == 0) {
            T temp = items[items.length - 1];
            items[items.length - 1] = null;
            size -= 1;
            end = items.length - 1;
            return temp;
        } else {
            T temp = items[end - 1];
            items[end - 1] = null;
            size -= 1;
            end -= 1;
            return temp;
        }
    }

    /** get item at index */
    public T get(int index) {
        return items[(start + index + 1) % this.items.length];
    }

    /** Adjust size of items array */
    private void resize(int newSize) {
        T[] newArray = (T[]) new Object[newSize];
        for (int i = 0; i < this.size; i += 1) {
            newArray[i] = this.get(i);
        }
        start = newArray.length - 1;
        end = this.size;
        this.items = newArray;
    }

    /** Equals method */
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

    /** WrapIndex - pretty useful so we don't have to write cases for going out of bounds */
    /** private int wrapIndex(int index) {
     * return (index + items.length) % items.length; //if index < 0: index += n
     * }
     * Does not work on negative numbers
     */


    /** iterator method */
    @Override
    public Iterator<T> iterator() {
        return new ArrayDeque.ArrayIterator();
    }

    /** Iterator class */
    private class ArrayIterator implements Iterator<T> {
        /** current */
        private int currentPos;
        private int count;

        /** Instantiate new iterator */
        public ArrayIterator() {
            currentPos = start;
            count = 0;
        }

        /** check if there is a next item */
        public boolean hasNext() {
            return count < size;
        }

        /** iterate to next item */
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (currentPos == items.length - 1) {
                T returnItem = items[0];
                currentPos = 0;
                count += 1;
                return returnItem;
            }
            T returnItem = items[currentPos + 1];
            currentPos += 1;
            count += 1;
            return returnItem;
        }
    }


}


