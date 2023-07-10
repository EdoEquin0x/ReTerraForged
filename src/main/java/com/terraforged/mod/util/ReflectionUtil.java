/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Predicate;

import org.apache.commons.lang3.ArrayUtils;

public class ReflectionUtil {
	
    public static MethodHandle getter(Class<?> owner, Class<?> type) {
        try {
            Field field = getField(owner, type);
            return MethodHandles.lookup().in(owner).unreflectGetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static MethodHandle setter(Class<?> owner, Class<?> type) {
        try {
            Field field = getField(owner, type);
            return MethodHandles.lookup().in(owner).unreflectSetter(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static MethodHandle method(Class<?> owner, Class<?>[] params, String... names) {
        try {
        	Method method = getMethod(owner, params, f -> ArrayUtils.contains(names, f.getName()));
            return MethodHandles.lookup().in(owner).unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getField(Class<?> owner, Class<?> fieldType) {
        return accessMember(owner, owner.getDeclaredFields(), ((f) -> f.getType().equals(fieldType)));
    }
    
    public static Method getMethod(Class<?> owner, Class<?>[] paramTypes, Predicate<Method> predicate) {
        return accessMember(owner, owner.getDeclaredMethods(), predicate.and((m) -> Arrays.equals(m.getParameterTypes(), paramTypes)));
    }

	public static <T extends AccessibleObject & Member> T accessMember(Class<?> owner, T[] members, Predicate<T> predicate) {
        for (T member : members) {
            if (predicate.test(member)) {
                member.setAccessible(true);
                return member;
            }
        }
        throw new IllegalStateException("Unable to find matching member in class " + owner);
    }
}
