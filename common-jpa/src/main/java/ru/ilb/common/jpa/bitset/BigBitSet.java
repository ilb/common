/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.ilb.common.jpa.bitset;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.BitSet;
import java.util.stream.Collectors;

/**
 * Store Sets as bit field
 *
 * To use, need to define class, extending BitSet: public class CreateOptionsSet
 * extends BitSet&lt;CreateOptions> {
 *
 * public CreateOptionsSet() { }
 *
 * public CreateOptionsSet(Long value) { super(value); }
 *
 * public CreateOptionsSet(Collection&lt;CreateOptions> items) { super(items); }
 * } define AttributeConverter and include in persistance.xml:
 * <pre>
 * {@code
 * @Converter(autoApply = true)
 * public class CreateOptionsConverter implements AttributeConverter<CreateOptionsSet, Long> {
 *
 *     @Override
 *     public Long convertToDatabaseColumn(CreateOptionsSet attribute) {
 *         return attribute == null ? null : attribute.getValue();
 *     }
 *
 *     @Override
 *     public CreateOptionsSet convertToEntityAttribute(Long dbData) {
 *         return dbData == null ? null : new CreateOptionsSet(dbData);
 *     }
 *
 * }
 * }
 * </pre>
 *
 * @author slavb
 * @param <T> stored object type
 */
public class BigBitSet<T> implements Serializable {

//    protected int MAX_BIT_LENGTH = 128;
    protected BitSet value;

    private final BitAccessor accessor;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BigBitSet<?> other = (BigBitSet<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    public BigBitSet() {
        Class<T> clazz = getParamClass(0);

        accessor = clazz.isEnum() ? new EnumBitAccessor() : new EntityBitAccessor(clazz);
    }

    public BigBitSet(BitSet value) {
        this();
        this.value = value;
    }
    public BigBitSet(byte[] value) {
        this();
        this.value = BitSet.valueOf(value);
    }
    

    public BigBitSet(Collection<T> items) {
        this();
        addAll(items);
    }

    /**
     * Получить параметры generic-класса
     *
     * @param pos
     * @return Class
     */
    private Class<T> getParamClass(int pos) {
        return (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[pos];
    }

    public BitSet getValue() {
        return this.value;
    }
    
    public byte[] toByteArray() {
        return value!=null ? value.toByteArray() : null;
    }
    

    public void setValue(BitSet value) {
        this.value = value;
    }

    public boolean contains(T item) {
        return value != null && value.get(accessor.getBitNum(item));
    }

    public boolean containsAll(Collection<T> items) {
        Boolean res = items.stream().map(this::contains).reduce((c1, c2) -> c1 && c2).orElse(Boolean.FALSE);
        return res;
    }

    public boolean remove(T item) {

        boolean res = false;
        if (value != null) {
            int bitNum = accessor.getBitNum(item);
            res = value.get(bitNum);
            value.clear(bitNum);
        }
        return res;
    }

    public void add(T item) {
        if (value == null) {
            value = new BitSet();
        }
        int bitNum = accessor.getBitNum(item);
        value.set(bitNum);
    }

    final public void addAll(Collection<T> items) {
        items.forEach((item) -> add(item));
    }


    /**
     * Список включенных битов
     *
     * @return List
     */
    public List<Integer> getSetBits() {
        //List<Integer> res = new ArrayList<>();
        List<Integer> res = value!=null ? value.stream().boxed().collect(Collectors.toList()): new ArrayList<>();
        return res;
        //return res;
    }

}
