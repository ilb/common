/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.bitset;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author slavb
 * @param <T> тип хранимого значения
 */
public class BitSet<T> implements Serializable {

    protected Long value;
    private final BitAccessor accessor;

    public BitSet() {
        accessor = new BitAccessor(getParamClass(0));
    }

    public BitSet(Long value) {
        accessor = new BitAccessor(getParamClass(0));
        this.value = value;
    }

    /**
     * Получить параметры generic-класса
     * @param pos
     * @return 
     */
    private Class<T> getParamClass(int pos) {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[pos];
    }

    public Long getValue() {
        return this.value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public boolean contains(T item) {
        return isSetBit(accessor.getBitNum(item));
    }
//    public boolean remove(T item) {
//        Long bitNum = accessor.getBitNum(item);
//        boolean res = isSetBit(bitNum);
//        //TODO
//        return res;
//    }

    public void add(T item) {
        setBit(accessor.getBitNum(item));
    }

    private Long setBit(long pos) {
        if (value == null) {
            value = 0L;
        }
        return value | (1 << pos);
    }

    private boolean isSetBit(long pos) {
        return value != null && ((value >> pos) & 1) != 0L;
    }

    private List<Long> getSetBits() {
        List<Long> res = new ArrayList<>();
        for (long pos = 0; pos < 64; pos++) {
            if (isSetBit(pos)) {
                res.add(pos);
            }
        }
        return res;
    }

}
