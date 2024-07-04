package com.bo.testnfc.tuple;

/**
 * 四元组
 */
public class Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
    public final D d;

    public Tuple4(A a, B b, C c, D d) {
        super(a, b, c);
        this.d = d;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple4)) {
            return false;
        }
        Tuple4<?, ?, ?, ?> t = (Tuple4<?, ?, ?, ?>) o;
        return super.equals(o) && equalsEx(t.d, d);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result * 31 + hashCodeEx(d);
        return result;
    }
}
