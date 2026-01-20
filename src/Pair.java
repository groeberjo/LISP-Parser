/* This class is used to store 2 values of type A and B. */
public class Pair<A, B> {
    private final A firstValue;
    private final B secondValue;

    public Pair(A firstValue, B secondValue) {
        this.firstValue = firstValue;
        this.secondValue = secondValue;
    }

    public A getFirst() {return firstValue;}

    public B getSecond() {return secondValue;}
}
