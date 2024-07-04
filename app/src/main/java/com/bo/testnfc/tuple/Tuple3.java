package com.bo.testnfc.tuple;

/**
 * 三元组
 */
public class Tuple3<A, B, C> extends Tuple<A, B> {

    public final C c;

    public Tuple3(A a, B b, C c) {
        super(a, b);
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple3)) {
            return false;
        }
        Tuple3<?, ?, ?> t = (Tuple3<?, ?, ?>) o;
        return super.equals(o) && equalsEx(t.c, c);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result * 31 + hashCodeEx(c);
        return result;
    }

}
