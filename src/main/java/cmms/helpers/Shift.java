/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpatitakis
 */
public class Shift {

    static public long SHIFT_DURATION_MSEC = 8 * 3600 * 1000;
    
    static public long SHIFT_DURATION_MIN = 8 * 60;

    static public String getStart(final Date current) {
        String start;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        long morning = new GregorianCalendar(current.getYear() + 1900, current.getMonth(), current.getDate(), 6, 0, 0).getTime().getTime();
        long evening = new GregorianCalendar(current.getYear() + 1900, current.getMonth(), current.getDate(), 14, 0, 0).getTime().getTime();
        long nighting = new GregorianCalendar(current.getYear() + 1900, current.getMonth(), current.getDate(), 22, 0, 0).getTime().getTime();
        long prevDate = new GregorianCalendar(current.getYear() + 1900, current.getMonth(), current.getDate() - 1, 22, 0, 0).getTime().getTime();
        long currentTime = current.getTime();

        if (morning <= currentTime && currentTime <= evening) {
            start = sdf.format(new Date(morning));
        } else if (evening <= currentTime && currentTime <= nighting) {
            start = sdf.format(new Date(evening));
        } else if (current.getHours() <= 24) {
            start = sdf.format(new Date(nighting));
        } else {
            start = sdf.format(new Date(prevDate));
        }

        return start;
    }

    static public String getEnd(final String start) {
        String end = "";

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long endShift = sdf.parse(start).getTime() + SHIFT_DURATION_MSEC;

            end = sdf.format(new Date(endShift));
        } catch (ParseException ex) {
            Logger.getLogger(Shift.class.getName()).log(Level.SEVERE, null, ex);
        }

        return end;
    }
}
