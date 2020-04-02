/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bangmaple.bangmaplecoronatracker.dao;

import com.bangmaple.bangmaplecoronatracker.dto.Corona;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author bangmaple
 */
public class CoronaDAO implements Serializable {

    private final List<Corona> list;

    public CoronaDAO() {
        list = new ArrayList<>();
    }

    public final Corona getEntity(final String code) {

        for (int i = 0; i < list.size(); i++) {
            if (code.equals(list.get(i).getCode())) {
                return list.get(i);
            }
        }
        return null;
    }
    
    public final List<Corona> getCoronaByLikeCountryName(final String search) {
        List<Corona> tmpList = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().toUpperCase().contains(search)) {
                tmpList.add(list.get(i));
            }
        }
        return tmpList;
    }

    public final void clear() {
        list.clear();
    }

    public final boolean add(Corona c) {
        return list.add(c);
    }

    public final void sort() {
        Collections.sort(list, (o1, o2) -> {
            if (o1.getLatest_data().getConfirmed() < o2.getLatest_data().getConfirmed()) {
                return 1;
            } else if (o1.getLatest_data().getConfirmed() > o2.getLatest_data().getConfirmed()) {
                return -1;
            }
            return 0;
        });
    }

    public final List<Corona> get() {
        return this.list;
    }

    @Override
    public String toString() {
        return list.toString();
    }

}
