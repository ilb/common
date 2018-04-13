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
import java.util.stream.Stream;

/**
 * Store Sets as bit field
 *
 * To use, need to define class, extending BitSet: public class CreateOptionsSet
 * extends BitSet&lt;CreateOptions> {

 public CreateOptionsSet() { }

 public CreateOptionsSet(Long bitSet) { super(bitSet); }

 public CreateOptionsSet(Collection&lt;CreateOptions> items) { super(items); }
 * } define AttributeConverter and include in persistance.xml:
 * <pre>
 * {@code
 * @Converter(autoApply = true)
 * public class ApplicationFactConverter implements AttributeConverter<ApplicationFactSet, byte[]> {
 * 
 *     @Override
 *     public byte[] convertToDatabaseColumn(ApplicationFactSet attribute) {
 *         return attribute == null ? null : attribute.toByteArray();
 *     }
 * 
 *     @Override
 *     public ApplicationFactSet convertToEntityAttribute(byte[] dbData) {
 *         return dbData == null ? null : new ApplicationFactSet(dbData);
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
    protected BitSet bitSet;

    private final BitAccessor accessor;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.bitSet);
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
        if (!Objects.equals(this.bitSet, other.bitSet)) {
            return false;
        }
        return true;
    }

    public BigBitSet() {
        Class<T> clazz = getParamClass(0);

        accessor = clazz.isEnum() ? new EnumBitAccessor() : new EntityBitAccessor(clazz);
    }

    public BigBitSet(BitSet bitSet) {
        this();
        this.bitSet = bitSet;
    }
    public BigBitSet(byte[] value) {
        this();
        this.bitSet = BitSet.valueOf(value);
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

    public BitSet getBitSet() {
        return this.bitSet;
    }
    
    public byte[] toByteArray() {
        return bitSet!=null ? bitSet.toByteArray() : null;
    }
    

    public void setBitSet(BitSet bitSet) {
        this.bitSet = bitSet;
    }

    public boolean contains(T item) {
        return bitSet != null && bitSet.get(accessor.getBitNum(item));
    }

    public boolean containsAll(Collection<T> items) {
        Boolean res = items.stream().map(this::contains).reduce((c1, c2) -> c1 && c2).orElse(Boolean.FALSE);
        return res;
    }
    public boolean containsAll(T... items) {
        //optimize this
        return  Stream.of(items).map(this::contains).reduce((c1, c2) -> c1 && c2).orElse(Boolean.FALSE);
    }    
    public boolean containsAny(Collection<T> items) {
        //optimize this
        return items.stream().map(this::contains).reduce((c1, c2) -> c1 || c2).orElse(Boolean.FALSE);
    }
    public boolean containsAny(T... items) {
        //optimize this
        return  Stream.of(items).map(this::contains).reduce((c1, c2) -> c1 || c2).orElse(Boolean.FALSE);
    }    

    public boolean remove(T item) {

        boolean res = false;
        if (bitSet != null) {
            int bitNum = accessor.getBitNum(item);
            res = bitSet.get(bitNum);
            bitSet.clear(bitNum);
        }
        return res;
    }

    public void add(T item) {
        if (bitSet == null) {
            bitSet = new BitSet();
        }
        int bitNum = accessor.getBitNum(item);
        bitSet.set(bitNum);
    }
    public void set(T item, boolean value) {
        if (bitSet == null) {
            bitSet = new BitSet();
        }
        int bitNum = accessor.getBitNum(item);
        bitSet.set(bitNum, value);
    }

    final public void addAll(Collection<T> items) {
        items.forEach(this::add);
    }

    public void removeAll(Collection<T> items) {
        items.forEach(this::remove);
    }

    /**
     * Список включенных битов
     *
     * @return List
     */
    public List<Integer> getSetBits() {
        //List<Integer> res = new ArrayList<>();
        List<Integer> res = bitSet!=null ? bitSet.stream().boxed().collect(Collectors.toList()): new ArrayList<>();
        return res;
        //return res;
    }

}
