/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.helpers;

import java.util.List;

/**
 *
 * @author gpatitakis
 */
public class _String {

    static public String concantList(List<?> values) {
        String concat = "";

        concat = values.stream().map((value) -> value.toString() + ",").reduce(concat, String::concat);

        return concat.substring(0, concat.lastIndexOf(","));
    }
}
