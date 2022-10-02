package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {

    /** default comparator */
    private Comparator<T> defaultComparator;

    /** instantiate MaxArrayDeque */
    public MaxArrayDeque(Comparator<T> comparator) {
        defaultComparator = comparator;
    }

    /** default max method */
    public T max() {
        if (this.isEmpty()) {
            return null;
        }
        int currMax = 0;
        for (int i = 0; i < size(); i += 1) {
            if (defaultComparator.compare(this.get(currMax), this.get(i)) <= 0) {
                currMax = i;
            }
        }
        return (T) this.get(currMax);

    }

    /** max using other comparator */
    public T max(Comparator<T> c) {
        if (this.isEmpty()) {
            return null;
        }
        int currMax = 0;
        for (int i = 0; i < size(); i += 1) {
            if (c.compare(this.get(currMax), this.get(i)) <= 0) {
                currMax = i;
            }
        }
        return (T) this.get(currMax);
    }
}
