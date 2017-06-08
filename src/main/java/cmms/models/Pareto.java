/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * @author gpatitakis
 */
public class Pareto {

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    private String label;

    private Double delay;

    private Double percent;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    public Pareto(String label, Long delay) {
        this.label = label;
        this.delay = new BigDecimal(delay / 3600.00).setScale(2, RoundingMode.CEILING).doubleValue();
    }

    // Getter and setter methods
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Double getDelay() {
        return delay;
    }

    public void setDelay(Double delay) {
        this.delay = delay;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "Pareto{"
                + "label=" + label
                + ", delay=" + delay
                + ", percent=" + percent + '}';
    }
} // class Pareto
