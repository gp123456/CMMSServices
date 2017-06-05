/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmms.models;

/**
 *
 * @author gpatitakis
 */
public class Pareto {

    // ------------------------
    // PRIVATE FIELDS
    // ------------------------
    private String label;

    private Long damages;

    // ------------------------
    // PUBLIC METHODS
    // ------------------------
    public Pareto(String label, Long damages) {
        this.label = label;
        this.damages = damages / 60;
    }

    // Getter and setter methods
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Long getDamages() {
        return damages;
    }

    public void setDamages(Long damages) {
        this.damages = damages;
    }

    @Override
    public String toString() {
        return "Pareto{"
                + "label=" + label
                + ", damages=" + damages + '}';
    }
} // class Pareto
