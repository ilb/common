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

/**
 * Store Sets as bit field
 * 
 * To use, need to define class, extending BitSet:
 * public class CreateOptionsSet extends BitSet&lt;CreateOptions> {
 * 
 *     public CreateOptionsSet() {
 *     }
 * 
 *     public CreateOptionsSet(Long value) {
 *         super(value);
 *     }
 * 
 *     public CreateOptionsSet(Collection&lt;CreateOptions> items) {
 *         super(items);
 *     }
 * }
 * define AttributeConverter and include in persistance.xml:
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
 * @author slavb
 * @param <T> stored object type
 */
public class BitSet<T> implements Serializable {

    protected Long value;

    private final BitAccessor accessor;

    public BitSet() {
        Class<T> clazz = getParamClass(0);
        
        accessor = clazz.isEnum() ? new EnumBitAccessor() : new EntityBitAccessor(clazz);
    }

    public BitSet(Long value) {
        this();
        this.value = value;
    }

    public BitSet(Collection<T> items) {
        this();
        addAll(items);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.value);
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
        final BitSet<?> other = (BitSet<?>) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
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

    final public void addAll(Collection<T> items) {
        items.forEach((item) -> {
            setBit(accessor.getBitNum(item));
        });
    }

    /**
     * Включение конкретного бита
     *
     * @param pos
     * @return
     */
    private Long setBit(int pos) {
        if (value == null) {
            value = 0L;
        }
        value = value | (1L << pos);

        return value;
    }

    /**
     * Проверить на вхождения бита
     *
     * @param pos
     * @return boolean
     */
    private boolean isSetBit(int pos) {
        return value != null && ((value >> pos) & 1) != 0L;
    }

    /**
     * Список включенных битов
     *
     * @return List
     */
    public List<Integer> getSetBits() {
        List<Integer> res = new ArrayList<>();
        for (int pos = 0; pos < 64; pos++) {
            if (isSetBit(pos)) {
                res.add(pos);
            }
        }

        return res;
    }

}
