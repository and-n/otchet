/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.rzd.otchet;

import java.util.Objects;

/**
 *
 * @author ATonevitskiy
 */
public class Pair<L, R>
        extends Object {

    public Pair(L left, R right) {
        l = left;
        r = right;
    }

    private L l;
    private R r;

    public L getL() {
        return l;
    }

    public R getR() {
        return r;
    }

    public void setL(L l) {
        this.l = l;
    }

    public void setR(R r) {
        this.r = r;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.l);
        hash = 79 * hash + Objects.hashCode(this.r);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Pair<?, ?> other = (Pair<?, ?>) obj;
        if (!Objects.equals(this.l, other.l)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Pair{" + "l=" + l + ", r=" + r + '}';
    }

}
